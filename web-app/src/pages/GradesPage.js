import React, { useState, useEffect, useRef } from 'react';
import {
    getAllGrades, getStudentGrades, getFacultyGrades, getStudentCGPA, saveGrade, bulkSaveGrade
} from '../services/gradeService';
import { getMyProfile } from '../services/facultyService';
import { getEnrolledStudents } from '../services/featureService';
import { safeParseFloat } from '../utils/validationUtils';
import { getAllCourses } from '../services/courseService';
import { getAllStudents } from '../services/studentService';
import { exportToCSV, exportToExcel } from '../utils/exportUtils';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import jsPDF from 'jspdf';
import 'jspdf-autotable';
import SessionManager from '../utils/SessionManager';
import Modal from '../components/Modal';
import { toast } from '../components/Toast';
import { getErrorMessage, getSuccessRefId } from '../utils/error';
import { SkeletonTable } from '../components/Skeleton';

const GRADES = ['A', 'B', 'C', 'D', 'E', 'F'];
const EXAM_TYPES = ['MID TERM', 'END TERM', 'ASSIGNMENT', 'PRACTICAL'];

const GradesPage = () => {
    const [activeTab, setActiveTab] = useState('view');

    // View State
    const [grades, setGrades] = useState([]);
    const [viewFilter, setViewFilter] = useState('');
    const [cgpa, setCgpa] = useState(null);
    const [facultyId, setFacultyId] = useState(null);

    // Manage State (Admin/Faculty)
    const [students, setStudents] = useState([]);
    const [courses, setCourses] = useState([]);
    const [formData, setFormData] = useState({
        studentId: '',
        courseId: '',
        examType: 'MID TERM',
        marksObtained: '',
        grade: 'A'
    });

    // Bulk Grade Entry state
    const [bulkCourseId, setBulkCourseId] = useState('');
    const [bulkExamType, setBulkExamType] = useState('MID TERM');
    const [bulkEntries, setBulkEntries] = useState([]); // [{studentId, studentName, marks, grade}]
    const [bulkSaving, setBulkSaving] = useState(false);
    const [bulkResult, setBulkResult] = useState(null);
    const [bulkDirty, setBulkDirty] = useState(false);

    // List + form loading / dialog state
    const [listLoading, setListLoading] = useState(true);
    const [listError, setListError] = useState('');
    const [formLoading, setFormLoading] = useState(false);
    const [formError, setFormError] = useState('');
    const [singleOpen, setSingleOpen] = useState(false);
    const [singleSaving, setSingleSaving] = useState(false);
    const [editingGrade, setEditingGrade] = useState(null);
    const [marksError, setMarksError] = useState('');
    const marksRefs = useRef({});

    const user = SessionManager.getUser() || {};
    const canEdit = SessionManager.hasRole('ADMIN') || user.role === 'FACULTY';
    const singleDirty = Boolean(formData.studentId || formData.courseId || formData.marksObtained);

    useEffect(() => {
        const controller = new AbortController();
        if (activeTab === 'view') {
            loadGradesForRole(controller.signal);
        } else if (activeTab === 'manage' || activeTab === 'bulk') {
            loadFormData(controller.signal);
        }
        return () => controller.abort();
    }, [activeTab, user.username, user.role]);

    const resolveFacultyId = async () => {
        try {
            const res = await getMyProfile();
            const id = res.data?.id;
            if (id) setFacultyId(id);
            return id || null;
        } catch (err) {
            return null;
        }
    };

    const loadGradesForRole = async (signal) => {
        setListLoading(true);
        setListError('');
        let fid = facultyId;
        if (user.role === 'FACULTY' && !fid) {
            fid = await resolveFacultyId();
            if (signal?.aborted) return;
        }
        try {
            let res;
            if (user.role === 'STUDENT') {
                res = await getStudentGrades(user.username, signal);
            } else if (user.role === 'FACULTY' && fid) {
                res = await getFacultyGrades(fid, signal);
            } else {
                res = await getAllGrades(signal);
            }
            if (signal?.aborted) return;
            setGrades(res.data || []);
            if (user.role === 'STUDENT') {
                const cgpaRes = await getStudentCGPA(user.username, signal);
                if (!signal?.aborted) setCgpa(cgpaRes.data?.cgpa);
            }
        } catch (err) {
            if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
            setListError(err?.response?.data?.error || 'Could not load grades.');
        } finally {
            if (!signal?.aborted) setListLoading(false);
        }
    };

    const loadFormData = async (signal) => {
        setFormLoading(true);
        setFormError('');
        try {
            const [rStud, rCour] = await Promise.all([getAllStudents(), getAllCourses(1, 500)]);
            if (signal?.aborted) return;
            setStudents(rStud.data || []);
            setCourses(rCour.data || []);
        } catch (err) {
            if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
            setFormError(err?.response?.data?.error || 'Could not load students and subjects.');
        } finally {
            if (!signal?.aborted) setFormLoading(false);
        }
    };

    const suggestedGrade = (marks) => {
        const m = parseFloat(marks);
        if (!Number.isFinite(m)) return '';
        if (m >= 90) return 'A';
        if (m >= 75) return 'B';
        if (m >= 60) return 'C';
        if (m >= 50) return 'D';
        if (m >= 40) return 'E';
        return 'F';
    };

    const openCreateGrade = () => {
        setEditingGrade(null);
        setFormData({ studentId: '', courseId: '', examType: 'MID TERM', marksObtained: '', grade: 'A' });
        setMarksError('');
        setSingleOpen(true);
    };

    const openEditGrade = (g) => {
        setEditingGrade(g);
        setFormData({
            studentId: String(g.studentId || ''),
            courseId: String(g.courseId || ''),
            examType: g.examType || 'MID TERM',
            marksObtained: g.marksObtained !== undefined && g.marksObtained !== null ? String(g.marksObtained) : '',
            grade: g.grade || 'A'
        });
        setMarksError('');
        setSingleOpen(true);
    };

    const refreshView = () => {
        loadGradesForRole();
    };

    const handleSaveGrade = async () => {
        const marks = parseFloat(formData.marksObtained);
        if (!Number.isFinite(marks) || marks < 0 || marks > 100) {
            setMarksError('Marks must be between 0 and 100.');
            return;
        }
        setMarksError('');
        setSingleSaving(true);
        try {
            const payload = {
                ...(editingGrade?.id ? { id: editingGrade.id } : {}),
                studentId: parseInt(formData.studentId),
                courseId: parseInt(formData.courseId),
                examType: formData.examType,
                marksObtained: marks,
                grade: formData.grade
            };
            const refId = getSuccessRefId();
            await saveGrade(payload);
            toast.success(editingGrade ? 'Grade updated.' : 'Grade saved successfully.', { refId });
            setSingleOpen(false);
            setEditingGrade(null);
            setFormData({ studentId: '', courseId: '', examType: 'MID TERM', marksObtained: '', grade: 'A' });
            refreshView();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not save this grade.');
            toast.error(message, { refId, details: { status } });
        } finally {
            setSingleSaving(false);
        }
    };

    // Auto-load students into bulk table when course is chosen
    const handleBulkCourseSelect = async (courseId) => {
        if (bulkDirty && bulkEntries.some(e => e.marks !== '')) {
            // eslint-disable-next-line no-alert
            if (!window.confirm('Switch course? Unsaved bulk marks will be lost.')) return;
        }
        setBulkCourseId(courseId);
        setBulkResult(null);
        setBulkDirty(false);
        if (!courseId) { setBulkEntries([]); return; }
        let roster = [];
        try {
            const res = await getEnrolledStudents(courseId);
            roster = res.data || [];
        } catch (err) {
            roster = [];
        }
        if (roster.length === 0) {
            roster = students;
            toast.info('No enrollments found for this course yet — using the full student list.');
        }
        setBulkEntries(
            roster.map(s => ({
                studentId: s.id,
                studentName: s.name,
                enrollmentNumber: s.username || s.enrollmentId || s.enrollmentNumber,
                marks: '',
                grade: 'A'
            }))
        );
    };

    const handleBulkEntryChange = (studentId, field, value) => {
        setBulkDirty(true);
        setBulkEntries(prev => prev.map(e =>
            e.studentId === studentId ? { ...e, [field]: value } : e
        ));
    };

    // Auto-derive grade from marks
    const autoGrade = (marks) => {
        const m = parseFloat(marks);
        if (isNaN(m)) return 'A';
        if (m >= 90) return 'A';
        if (m >= 75) return 'B';
        if (m >= 60) return 'C';
        if (m >= 50) return 'D';
        if (m >= 40) return 'E';
        return 'F';
    };

    const isValidMarks = (marks) => {
        if (marks === '' || marks === null || marks === undefined) return true;
        const m = parseFloat(marks);
        return Number.isFinite(m) && m >= 0 && m <= 100;
    };

    const handleBulkMarksChange = (studentId, marks) => {
        setBulkDirty(true);
        setBulkEntries(prev => prev.map(e =>
            e.studentId === studentId
                ? { ...e, marks, grade: marks === '' ? e.grade : autoGrade(marks) }
                : e
        ));
    };

    // Keyboard-friendly: Enter / ArrowDown moves to next row, ArrowUp to previous.
    const focusMarks = (studentId) => {
        marksRefs.current[studentId]?.focus?.();
        marksRefs.current[studentId]?.select?.();
    };

    const handleBulkKeyDown = (e, index) => {
        if (e.key === 'Enter' || e.key === 'ArrowDown') {
            e.preventDefault();
            const next = bulkEntries[index + 1];
            if (next) focusMarks(next.studentId);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            const prev = bulkEntries[index - 1];
            if (prev) focusMarks(prev.studentId);
        }
    };

    const handleBulkSubmit = async () => {
        const invalid = bulkEntries.filter(e => e.marks !== '' && !isValidMarks(e.marks));
        if (invalid.length > 0) {
            toast.error(`${invalid.length} row(s) have marks outside 0–100. Fix the highlighted rows.`);
            const first = invalid[0];
            focusMarks(first.studentId);
            return;
        }
        const filled = bulkEntries.filter(e => e.marks !== '' && !isNaN(parseFloat(e.marks)));
        if (filled.length === 0) { toast.error('Please enter marks for at least one student.'); return; }
        if (!bulkCourseId) { toast.error('Select a course before saving.'); return; }

        setBulkSaving(true);
        setBulkResult(null);

        try {
            const payload = filled.map(entry => ({
                studentId: entry.studentId,
                courseId: parseInt(bulkCourseId),
                examType: bulkExamType,
                marksObtained: safeParseFloat(entry.marks),
                grade: entry.grade
            }));

            const res = await bulkSaveGrade(payload);
            const saved = res.data?.saved ?? filled.length;
            setBulkResult({ saved, failed: filled.length - saved, total: filled.length });
            setBulkDirty(false);
            const refId = getSuccessRefId();
            if (filled.length - saved === 0) toast.success(`Saved ${saved} of ${filled.length} grades.`, { refId });
            else toast.info(`Saved ${saved} of ${filled.length} grades. Some rows failed (may be duplicates).`, { refId });
            refreshView();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not bulk save grades.');
            toast.error(message, { refId, details: { status } });
        } finally {
            setBulkSaving(false);
        }
    };

    const generateTranscript = () => {
        const dataToExport = viewFilter ? grades.filter(g =>
            (g.studentName || '').toLowerCase().includes(viewFilter.toLowerCase()) ||
            (g.enrollmentNumber || '').toLowerCase().includes(viewFilter.toLowerCase()) ||
            (g.courseName || '').toLowerCase().includes(viewFilter.toLowerCase())
        ) : grades;

        if (!dataToExport.length) {
            toast.error('No grade data found to generate transcript.');
            return;
        }

        const studentName = user.role === 'STUDENT' ? (user.name || 'Student') : dataToExport[0].studentName || 'Student';
        const enrollmentNo = user.role === 'STUDENT' ? (user.enrollmentNumber || 'N/A') : dataToExport[0].enrollmentNumber || 'N/A';

        const doc = new jsPDF();

        doc.setFontSize(22);
        doc.text("Official Academic Transcript", 14, 20);

        doc.setFontSize(11);
        doc.text(`Student Name: ${studentName}`, 14, 30);
        if (enrollmentNo) doc.text(`Enrollment Number: ${enrollmentNo}`, 14, 35);
        if (cgpa !== null) {
            doc.text(`Cumulative GPA: ${cgpa.toFixed(2)} / 10.0`, 14, 40);
        }

        const tableColumn = ["Course Name", "Credits", "Exam Type", "Marks (%)", "Grade"];
        doc.autoTable({
            head: [tableColumn],
            body: dataToExport.map(g => [g.courseName, g.credits, g.examType, g.marksObtained, g.grade]),
            startY: 45,
            theme: 'striped',
            headStyles: { fillColor: [99, 102, 241] }
        });

        const pageCount = doc.internal.getNumberOfPages();
        for (let i = 1; i <= pageCount; i++) {
            doc.setPage(i);
            doc.setFontSize(10);
            const date = new Date().toLocaleDateString();
            doc.text(`Generated on: ${date}`, 14, doc.internal.pageSize.getHeight() - 10);
            doc.text(`Page ${i} of ${pageCount}`, doc.internal.pageSize.getWidth() - 30, doc.internal.pageSize.getHeight() - 10);
        }

        doc.save(`${studentName.replace(/\s+/g, '_')}_Transcript.pdf`);
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Grades & Transcripts</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        className={`btn ${activeTab === 'view' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('view')}
                    >
                        {user.role === 'STUDENT' ? 'My Grades' : 'All Grades'}
                    </button>
                    {canEdit && (
                        <button
                            className={`btn ${activeTab === 'manage' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setActiveTab('manage')}
                        >
                            Enter/Edit Grades
                        </button>
                    )}
                    {canEdit && (
                        <button
                            className={`btn ${activeTab === 'bulk' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => {
                                if (bulkDirty && bulkEntries.some(e => e.marks !== '')) {
                                    // eslint-disable-next-line no-alert
                                    if (!window.confirm('Leave bulk entry? Unsaved marks will be lost.')) return;
                                }
                                setActiveTab('bulk');
                            }}
                        >
                            📋 Bulk Entry
                        </button>
                    )}
                    {canEdit && activeTab === 'manage' && (
                        <button className="btn btn-primary" onClick={openCreateGrade}>+ Enter Grade</button>
                    )}
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    {activeTab === 'view' && grades.length > 0 && (
                        <button className="btn btn-secondary" onClick={() => exportToCSV(
                            user.role !== 'STUDENT'
                                ? ['Student', 'Enrollment No', 'Subject', 'Credits', 'Exam Type', 'Marks (%)', 'Grade']
                                : ['Subject', 'Credits', 'Exam Type', 'Marks (%)', 'Grade'],
                            grades.map(g => user.role !== 'STUDENT'
                                ? [g.studentName, g.enrollmentNumber, g.courseName, g.credits, g.examType, g.marksObtained, g.grade]
                                : [g.courseName, g.credits, g.examType, g.marksObtained, g.grade]
                            ),
                            'grades_export'
                        )}>⬇ Export CSV</button>
                    )}
                    {activeTab === 'view' && grades.length > 0 && (
                        <button className="btn btn-secondary" onClick={() => exportToExcel(
                            user.role !== 'STUDENT'
                                ? ['Student', 'Enrollment No', 'Subject', 'Credits', 'Exam Type', 'Marks (%)', 'Grade']
                                : ['Subject', 'Credits', 'Exam Type', 'Marks (%)', 'Grade'],
                            grades.map(g => user.role !== 'STUDENT'
                                ? [g.studentName, g.enrollmentNumber, g.courseName, g.credits, g.examType, g.marksObtained, g.grade]
                                : [g.courseName, g.credits, g.examType, g.marksObtained, g.grade]
                            ),
                            'grades_export'
                        )}>⬇ Export Excel</button>
                    )}
                    {activeTab === 'view' && grades.length > 0 && (user.role === 'STUDENT' || SessionManager.hasRole('ADMIN') || user.role === 'FACULTY') && (
                        <button className="btn btn-primary" onClick={generateTranscript}>
                            📄 Download Transcript
                        </button>
                    )}
                </div>
            </div>

            {activeTab === 'view' && listError && (
                <div className="retry-bar" role="alert" style={{ marginBottom: '16px' }}>
                    <span>{listError} (Showing loaded records only.)</span>
                    <button className="btn btn-secondary btn-sm" onClick={() => refreshView()}>Retry</button>
                </div>
            )}
            {activeTab === 'view' && listLoading ? (
                <SkeletonTable rows={6} cols={5} />
            ) : activeTab === 'view' && (
                <>
                    <div style={{ marginBottom: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span className="badge badge-primary">All records: {grades.length}</span>
                    </div>
                    <div style={{ marginBottom: '20px' }}>
                        <input
                            type="text"
                            className="form-control"
                            placeholder="🔍 Filter by student name, enrollment, or course..."
                            value={viewFilter}
                            onChange={e => setViewFilter(e.target.value)}
                        />
                    </div>
                    {user.role === 'STUDENT' && cgpa !== null && (
                        <div className="stat-card" style={{ marginBottom: '20px', background: 'linear-gradient(135deg, #2a0845 0%, #6441A5 100%)', color: 'white' }}>
                            <h3>Cumulative Grade Point Average (CGPA)</h3>
                            <h1 style={{ fontSize: '3rem', margin: '10px 0' }}>{cgpa.toFixed(2)}</h1>
                            <p>Scale: 10.0</p>
                        </div>
                    )}

                    {grades.length > 0 && (
                        <div className="stat-card" style={{ marginBottom: '20px' }}>
                            <h3>📊 Grade Distribution</h3>
                            <div style={{ width: '100%', height: 250, marginTop: '20px' }}>
                                <ResponsiveContainer>
                                    <BarChart data={(() => {
                                        const dist = { 'A': 0, 'B': 0, 'C': 0, 'D': 0, 'E': 0, 'F': 0 };
                                        grades.forEach(g => { if (dist[g.grade] !== undefined) dist[g.grade]++; });
                                        return Object.keys(dist).map(k => ({ grade: k, count: dist[k] }));
                                    })()} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                                        <XAxis dataKey="grade" axisLine={false} tickLine={false} />
                                        <YAxis allowDecimals={false} axisLine={false} tickLine={false} />
                                        <Tooltip cursor={{ fill: '#f1f5f9' }} contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} />
                                        <Bar dataKey="count" fill="#6366f1" radius={[4, 4, 0, 0]} barSize={40} />
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>
                    )}

                    {!listLoading && !listError && grades.length === 0 && (
                        <div style={{ textAlign: 'center', padding: '40px 20px', color: '#94a3b8', background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px' }}>
                            <div style={{ fontSize: '2rem', marginBottom: '8px' }}>📂</div>
                            No grades recorded yet.
                            {canEdit && <div style={{ marginTop: '6px', color: '#64748b' }}>Use “Enter/Edit Grades” or “Bulk Entry” to add the first grade.</div>}
                            {user.role === 'STUDENT' && <div style={{ marginTop: '6px', color: '#64748b' }}>Your grades will appear here once your teachers record them.</div>}
                        </div>
                    )}

                    {grades.length > 0 && (
                    <div className="data-table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    {user.role !== 'STUDENT' && <th>Student Name</th>}
                                    {user.role !== 'STUDENT' && <th>Enrollment No</th>}
                                    <th>Course Name</th>
                                    <th>Credits</th>
                                    <th>Exam Type</th>
                                    <th>Marks (%)</th>
                                    <th>Letter Grade</th>
                                    {canEdit && <th style={{ textAlign: 'right' }}>Actions</th>}
                                </tr>
                            </thead>
                            <tbody>
                                {(() => {
                                    const filtered = grades.filter(g =>
                                        (g.studentName || '').toLowerCase().includes(viewFilter.toLowerCase()) ||
                                        (g.enrollmentNumber || '').toLowerCase().includes(viewFilter.toLowerCase()) ||
                                        (g.courseName || '').toLowerCase().includes(viewFilter.toLowerCase())
                                    );
                                    if (filtered.length === 0) return (
                                        <tr>
                                            <td colSpan={canEdit ? 8 : (user.role !== 'STUDENT' ? 7 : 5)} style={{ textAlign: 'center' }}>No grades found matching your filter.</td>
                                        </tr>
                                    );
                                    return filtered.map(g => (
                                        <tr key={g.id}>
                                            {user.role !== 'STUDENT' && <td>{g.studentName}</td>}
                                            {user.role !== 'STUDENT' && <td>{g.enrollmentNumber || 'N/A'}</td>}
                                            <td>{g.courseName}</td>
                                            <td>{g.credits}</td>
                                            <td>{g.examType}</td>
                                            <td>{g.marksObtained}</td>
                                            <td>
                                                <span className={`badge ${g.grade === 'F' ? 'badge-danger' : (['A', 'B'].includes(g.grade) ? 'badge-success' : 'badge-warning')}`}>
                                                    {g.grade}
                                                </span>
                                            </td>
                                            {canEdit && (
                                                <td style={{ textAlign: 'right' }}>
                                                    <button className="btn btn-sm btn-secondary" onClick={() => openEditGrade(g)}>Edit</button>
                                                </td>
                                            )}
                                        </tr>
                                    ))
                                })()}
                            </tbody>
                        </table>
                    </div>
                    )}
                </>
            )}

            {activeTab === 'manage' && (
                <div className="stat-card">
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
                        <div>
                            <h3 style={{ margin: 0 }}>Record Student Grade</h3>
                            <p style={{ color: '#64748b', fontSize: '.85rem', margin: '6px 0 0' }}>Single entry opens in a dialog with range checks and auto grade suggestion. Use Bulk Entry for a whole class.</p>
                        </div>
                        <button className="btn btn-primary" onClick={openCreateGrade}>+ Enter Grade</button>
                    </div>
                    {formLoading ? (
                        <div style={{ marginTop: '16px' }}><SkeletonTable rows={3} cols={3} /></div>
                    ) : formError ? (
                        <div className="retry-bar" role="alert" style={{ marginTop: '16px' }}>
                            <span>{formError}</span>
                            <button className="btn btn-secondary btn-sm" onClick={() => loadFormData()}>Retry</button>
                        </div>
                    ) : (
                        <div style={{ marginTop: '16px', fontSize: '.85rem', color: '#475569' }}>
                            {students.length} students · {courses.length} subjects loaded. Last saved grades appear under All Grades.
                        </div>
                    )}
                </div>
            )}

            <Modal
                isOpen={singleOpen}
                title={editingGrade ? `Edit Grade — ${editingGrade.studentName || 'Student'}` : 'Enter Grade'}
                onClose={() => { if (!singleSaving) { setSingleOpen(false); setEditingGrade(null); } }}
                onSubmit={handleSaveGrade}
                submitLabel={editingGrade ? 'Update Grade' : 'Save Grade'}
                submitting={singleSaving}
                submitDisabled={!formData.studentId || !formData.courseId || formData.marksObtained === ''}
                isDirty={singleDirty}
                size="medium"
            >
                <form onSubmit={(e) => { e.preventDefault(); handleSaveGrade(); }} className="form-grid">
                    <div className="form-group">
                        <label className="form-label">Student *</label>
                        <select
                            required
                            className="form-control"
                            value={formData.studentId}
                            disabled={Boolean(editingGrade)}
                            onChange={e => setFormData({ ...formData, studentId: e.target.value })}
                        >
                            <option value="">-- Select Student --</option>
                            {students.map(s => <option key={s.id} value={s.id}>{s.name} ({s.username || s.enrollmentId || s.enrollmentNumber || 'N/A'})</option>)}
                        </select>
                    </div>
                    <div className="form-group">
                        <label className="form-label">Subject *</label>
                        <select
                            required
                            className="form-control"
                            value={formData.courseId}
                            disabled={Boolean(editingGrade)}
                            onChange={e => setFormData({ ...formData, courseId: e.target.value })}
                        >
                            <option value="">-- Select Subject --</option>
                            {courses.map(c => <option key={c.id} value={c.id}>{c.code} — {c.name}{c.specialization ? ` [${c.specialization}]` : ''}</option>)}
                        </select>
                    </div>
                    <div className="form-group">
                        <label className="form-label">Exam Type *</label>
                        <select
                            required
                            className="form-control"
                            value={formData.examType}
                            disabled={Boolean(editingGrade)}
                            onChange={e => setFormData({ ...formData, examType: e.target.value })}
                        >
                            <option value="MID TERM">Mid Term</option>
                            <option value="END TERM">End Term</option>
                            <option value="ASSIGNMENT">Assignment</option>
                            <option value="PRACTICAL">Practical</option>
                        </select>
                        {Boolean(editingGrade) &&
                            <span className="field-hint">Exam type can't be changed on edit — it identifies the grade record.</span>}
                    </div>
                    <div className="form-group">
                        <label className="form-label">Marks Obtained (0–100) *</label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            max="100"
                            required
                            className={`form-control${marksError ? ' is-invalid' : ''}`}
                            value={formData.marksObtained}
                            onChange={e => {
                                const v = e.target.value;
                                setFormData(prev => {
                                    const sg = suggestedGrade(v);
                                    return { ...prev, marksObtained: v, grade: v === '' ? prev.grade : (sg || prev.grade) };
                                });
                                const m = parseFloat(v);
                                if (v !== '' && (!Number.isFinite(m) || m < 0 || m > 100)) setMarksError('Marks must be between 0 and 100.');
                                else setMarksError('');
                            }}
                            aria-invalid={Boolean(marksError)}
                        />
                        {marksError
                            ? <span className="field-error" role="alert">{marksError}</span>
                            : <span className="field-hint">Allowed range 0–100. Grade auto-suggests: {suggestedGrade(formData.marksObtained) || '—'} (A ≥ 90, B ≥ 75, C ≥ 60, D ≥ 50, E ≥ 40).</span>}
                    </div>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Letter Grade *</label>
                        <select
                            required
                            className="form-control"
                            value={formData.grade}
                            onChange={e => setFormData({ ...formData, grade: e.target.value })}
                        >
                            {GRADES.map(g => <option key={g} value={g}>{g}{suggestedGrade(formData.marksObtained) === g ? ' (suggested)' : ''}</option>)}
                        </select>
                    </div>
                </form>
            </Modal>

            {/* ===== BULK GRADE ENTRY TAB ===== */}
            {activeTab === 'bulk' && formError && (
                <div className="retry-bar" role="alert" style={{ marginBottom: '16px' }}>
                    <span>{formError}</span>
                    <button className="btn btn-secondary btn-sm" onClick={() => loadFormData()}>Retry</button>
                </div>
            )}
            {activeTab === 'bulk' && (
                <div>
                    <div style={{ marginBottom: '12px', display: 'flex', gap: '8px', alignItems: 'center', flexWrap: 'wrap' }}>
                        <span className="badge badge-primary">Keyboard: Enter/↓ next row, ↑ previous</span>
                        {bulkDirty && <span className="badge badge-warning">Unsaved changes</span>}
                        {bulkSaving && <span className="badge badge-primary">Saving…</span>}
                    </div>
                    {/* Config row */}
                    <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '18px', marginBottom: '20px', display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                        <div className="form-group" style={{ margin: 0, flex: '2 1 200px' }}>
                            <label>Course *</label>
                            <select required value={bulkCourseId} onChange={e => handleBulkCourseSelect(e.target.value)}>
                                <option value="">-- Select Course --</option>
                                {courses.map(c => <option key={c.id} value={c.id}>{c.name} ({c.code})</option>)}
                            </select>
                        </div>
                        <div className="form-group" style={{ margin: 0, flex: '1 1 180px' }}>
                            <label>Exam Type *</label>
                            <select required value={bulkExamType} onChange={e => setBulkExamType(e.target.value)}>
                                {EXAM_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                            </select>
                        </div>
                    </div>

                    {/* Result banner */}
                    {bulkResult && (
                        <div style={{
                            padding: '12px 16px', borderRadius: '8px', marginBottom: '16px',
                            background: bulkResult.failed === 0 ? '#f0fff4' : '#fffaf0',
                            border: `1px solid ${bulkResult.failed === 0 ? '#9ae6b4' : '#fbd38d'}`
                        }}>
                            <strong>{bulkResult.failed === 0 ? '✅' : '⚠️'}</strong>&nbsp;
                            Saved <strong>{bulkResult.saved}</strong> grades.
                            {bulkResult.failed > 0 && <span style={{ color: '#c05621' }}> {bulkResult.failed} failed (may be duplicate entries).</span>}
                        </div>
                    )}

                    {!bulkCourseId ? (
                        <div style={{ textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
                            <div style={{ fontSize: '2.5rem', marginBottom: '12px' }}>📋</div>
                            Select a course above to load the student list.
                        </div>
                    ) : (
                        <>
                            <div style={{ marginBottom: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div style={{ fontSize: '0.85rem', color: '#718096' }}>
                                    {bulkEntries.filter(e => e.marks !== '').length} of {bulkEntries.length} students filled
                                </div>
                                <button
                                    className="btn btn-primary"
                                    onClick={handleBulkSubmit}
                                    disabled={bulkSaving}
                                >
                                    {bulkSaving ? 'Saving…' : `💾 Save All Grades (${bulkEntries.filter(e => e.marks !== '').length})`}
                                </button>
                            </div>

                            <div className="data-table-container">
                                <table className="data-table">
                                    <thead>
                                        <tr>
                                            <th>Student Name</th>
                                            <th>Enrollment No</th>
                                            <th style={{ width: '140px' }}>Marks (0–100)</th>
                                            <th style={{ width: '110px' }}>Grade</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {bulkEntries.map((entry, idx) => {
                                            const invalid = entry.marks !== '' && !isValidMarks(entry.marks);
                                            return (
                                            <tr key={entry.studentId} style={{ background: invalid ? '#fff5f5' : entry.marks !== '' ? '#f7fffe' : 'white' }}>
                                                <td style={{ fontWeight: '500' }}>{entry.studentName}</td>
                                                <td style={{ color: '#718096', fontSize: '0.85rem' }}>{entry.enrollmentNumber || '—'}</td>
                                                <td>
                                                    <input
                                                        ref={(el) => { marksRefs.current[entry.studentId] = el; }}
                                                        type="number"
                                                        min="0" max="100" step="0.5"
                                                        placeholder="—"
                                                        value={entry.marks}
                                                        aria-label={`Marks for ${entry.studentName} (0 to 100)`}
                                                        aria-invalid={invalid}
                                                        onChange={e => handleBulkMarksChange(entry.studentId, e.target.value)}
                                                        onKeyDown={e => handleBulkKeyDown(e, idx)}
                                                        style={{
                                                            width: '100%', padding: '5px 8px', border: `1px solid ${invalid ? '#f04438' : '#e2e8f0'}`,
                                                            borderRadius: '6px', outline: 'none', fontSize: '0.9rem',
                                                            background: invalid ? '#fff5f5' : entry.marks !== '' ? '#ebf8ff' : 'white'
                                                        }}
                                                    />
                                                    {invalid && <div style={{ color: '#b42318', fontSize: '.72rem', marginTop: '2px' }}>0–100 only</div>}
                                                </td>
                                                <td>
                                                    <select
                                                        value={entry.grade}
                                                        onChange={e => handleBulkEntryChange(entry.studentId, 'grade', e.target.value)}
                                                        style={{
                                                            width: '100%', padding: '5px 8px', border: '1px solid #e2e8f0',
                                                            borderRadius: '6px', fontSize: '0.9rem',
                                                            color: entry.grade === 'F' ? '#e53e3e' : entry.grade === 'A' ? '#276749' : '#2d3748',
                                                            fontWeight: '600'
                                                        }}
                                                    >
                                                        {GRADES.map(g => <option key={g} value={g}>{g}</option>)}
                                                    </select>
                                                </td>
                                            </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>

                            <div style={{ marginTop: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px', position: 'sticky', bottom: 0, background: '#fff', padding: '12px 0', borderTop: '1px solid var(--border)' }}>
                                <div style={{ fontSize: '.8rem', color: bulkDirty ? '#b54708' : '#667085' }} role="status">
                                    {bulkSaving ? 'Saving grades…' : bulkDirty ? '● Unsaved changes — review highlighted rows, then save.' : 'All changes saved.'}
                                </div>
                                <button
                                    className="btn btn-primary"
                                    onClick={handleBulkSubmit}
                                    disabled={bulkSaving || !bulkCourseId}
                                    style={{ minWidth: '180px' }}
                                >
                                    {bulkSaving ? 'Saving…' : `💾 Save All Grades`}
                                </button>
                            </div>
                        </>
                    )}
                </div>
            )}
        </div>
    );
};

export default GradesPage;
