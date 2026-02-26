import React, { useState, useEffect } from 'react';
import {
    getAllGrades, getStudentGrades, getStudentCGPA, saveGrade
} from '../services/gradeService';
import { getAllCourses } from '../services/courseService';
import { getAllStudents } from '../services/studentService';
import { exportToCSV } from '../utils/exportUtils';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import jsPDF from 'jspdf';
import 'jspdf-autotable';
import SessionManager from '../utils/SessionManager';

const GRADES = ['A', 'B', 'C', 'D', 'E', 'F'];
const EXAM_TYPES = ['MID TERM', 'END TERM', 'ASSIGNMENT', 'PRACTICAL'];

const GradesPage = () => {
    const [activeTab, setActiveTab] = useState('view');

    // View State
    const [grades, setGrades] = useState([]);
    const [cgpa, setCgpa] = useState(null);

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

    const user = SessionManager.getUser() || {};

    useEffect(() => {
        if (activeTab === 'view') {
            if (user.role === 'STUDENT') {
                loadStudentGrades(user.id);
            } else {
                loadAllGrades();
            }
        } else if (activeTab === 'manage' || activeTab === 'bulk') {
            loadFormData();
        }
        // eslint-disable-next-line
    }, [activeTab, user.id, user.role]);

    const loadStudentGrades = async (studentId) => {
        try {
            const res = await getStudentGrades(studentId);
            setGrades(res.data || []);
            const cgpaRes = await getStudentCGPA(studentId);
            setCgpa(cgpaRes.data?.cgpa);
        } catch (err) {
            console.error(err);
        }
    };

    const loadAllGrades = async () => {
        try {
            const res = await getAllGrades();
            setGrades(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const loadFormData = async () => {
        try {
            const pStudents = getAllStudents();
            const pCourses = getAllCourses();
            const [rStud, rCour] = await Promise.all([pStudents, pCourses]);
            setStudents(rStud.data || []);
            setCourses(rCour.data || []);
        } catch (err) {
            console.error('Failed to load form data', err);
        }
    };

    const handleSaveGrade = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                studentId: parseInt(formData.studentId),
                courseId: parseInt(formData.courseId),
                examType: formData.examType,
                marksObtained: parseFloat(formData.marksObtained),
                grade: formData.grade
            };
            await saveGrade(payload);
            alert('Grade saved successfully!');
            setFormData({ ...formData, marksObtained: '', grade: 'A' });
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to save grade');
        }
    };

    // Auto-load students into bulk table when course is chosen
    const handleBulkCourseSelect = (courseId) => {
        setBulkCourseId(courseId);
        setBulkResult(null);
        if (!courseId) { setBulkEntries([]); return; }
        setBulkEntries(
            students.map(s => ({
                studentId: s.id,
                studentName: s.name,
                enrollmentNumber: s.enrollmentNumber,
                marks: '',
                grade: 'A'
            }))
        );
    };

    const handleBulkEntryChange = (studentId, field, value) => {
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

    const handleBulkMarksChange = (studentId, marks) => {
        setBulkEntries(prev => prev.map(e =>
            e.studentId === studentId
                ? { ...e, marks, grade: autoGrade(marks) }
                : e
        ));
    };

    const handleBulkSubmit = async () => {
        const filled = bulkEntries.filter(e => e.marks !== '' && !isNaN(parseFloat(e.marks)));
        if (filled.length === 0) { alert('Please enter marks for at least one student.'); return; }
        setBulkSaving(true);
        setBulkResult(null);
        let saved = 0; let failed = 0;
        for (const entry of filled) {
            try {
                await saveGrade({
                    studentId: entry.studentId,
                    courseId: parseInt(bulkCourseId),
                    examType: bulkExamType,
                    marksObtained: parseFloat(entry.marks),
                    grade: entry.grade
                });
                saved++;
            } catch { failed++; }
        }
        setBulkSaving(false);
        setBulkResult({ saved, failed, total: filled.length });
    };

    const generateTranscript = () => {
        if (!grades.length) return;
        const studentName = user.role === 'STUDENT' ? (user.name || 'Student') : grades[0].studentName || 'Student';
        const enrollmentNo = user.role === 'STUDENT' ? (user.enrollmentNumber || 'N/A') : grades[0].enrollmentNumber || 'N/A';

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
        const tableRows = [];

        grades.forEach(g => {
            tableRows.push([
                g.courseName,
                g.credits,
                g.examType,
                g.marksObtained,
                g.grade
            ]);
        });

        doc.autoTable({
            head: [tableColumn],
            body: tableRows,
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
                    {(SessionManager.hasRole('ADMIN') || user.role === 'FACULTY') && (
                        <button
                            className={`btn ${activeTab === 'manage' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setActiveTab('manage')}
                        >
                            Enter/Edit Grades
                        </button>
                    )}
                    {(SessionManager.hasRole('ADMIN') || user.role === 'FACULTY') && (
                        <button
                            className={`btn ${activeTab === 'bulk' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setActiveTab('bulk')}
                        >
                            📋 Bulk Entry
                        </button>
                    )}
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    {activeTab === 'view' && grades.length > 0 && (
                        <button className="btn btn-secondary" onClick={() => exportToCSV(
                            user.role !== 'STUDENT'
                                ? ['Student', 'Enrollment No', 'Course', 'Credits', 'Exam Type', 'Marks (%)', 'Grade']
                                : ['Course', 'Credits', 'Exam Type', 'Marks (%)', 'Grade'],
                            grades.map(g => user.role !== 'STUDENT'
                                ? [g.studentName, g.enrollmentNumber, g.courseName, g.credits, g.examType, g.marksObtained, g.grade]
                                : [g.courseName, g.credits, g.examType, g.marksObtained, g.grade]
                            ),
                            'grades_export'
                        )}>⬇ Export CSV</button>
                    )}
                    {activeTab === 'view' && grades.length > 0 && user.role === 'STUDENT' && (
                        <button className="btn btn-primary" onClick={generateTranscript}>
                            📄 Download Transcript
                        </button>
                    )}
                </div>
            </div>

            {activeTab === 'view' && (
                <>
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
                                </tr>
                            </thead>
                            <tbody>
                                {grades.length === 0 ? (
                                    <tr>
                                        <td colSpan={user.role !== 'STUDENT' ? 7 : 5} style={{ textAlign: 'center' }}>No grades found.</td>
                                    </tr>
                                ) : (
                                    grades.map(g => (
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
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </>
            )}

            {activeTab === 'manage' && (
                <div className="stat-card">
                    <h3>Record Student Grade</h3>
                    <form className="form-grid" onSubmit={handleSaveGrade} style={{ marginTop: '20px' }}>
                        <div className="form-group">
                            <label>Student *</label>
                            <select
                                required
                                value={formData.studentId}
                                onChange={e => setFormData({ ...formData, studentId: e.target.value })}
                            >
                                <option value="">-- Select Student --</option>
                                {students.map(s => <option key={s.id} value={s.id}>{s.name} ({s.enrollmentNumber})</option>)}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Course *</label>
                            <select
                                required
                                value={formData.courseId}
                                onChange={e => setFormData({ ...formData, courseId: e.target.value })}
                            >
                                <option value="">-- Select Course --</option>
                                {courses.map(c => <option key={c.id} value={c.id}>{c.name} ({c.code})</option>)}
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Exam Type *</label>
                            <select
                                required
                                value={formData.examType}
                                onChange={e => setFormData({ ...formData, examType: e.target.value })}
                            >
                                <option value="MID TERM">Mid Term</option>
                                <option value="END TERM">End Term</option>
                                <option value="ASSIGNMENT">Assignment</option>
                                <option value="PRACTICAL">Practical</option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Marks Obtained (%) *</label>
                            <input
                                type="number"
                                step="0.1"
                                min="0"
                                max="100"
                                required
                                value={formData.marksObtained}
                                onChange={e => setFormData({ ...formData, marksObtained: e.target.value })}
                            />
                        </div>

                        <div className="form-group">
                            <label>Letter Grade *</label>
                            <select
                                required
                                value={formData.grade}
                                onChange={e => setFormData({ ...formData, grade: e.target.value })}
                            >
                                <option value="A">A</option>
                                <option value="B">B</option>
                                <option value="C">C</option>
                                <option value="D">D</option>
                                <option value="E">E</option>
                                <option value="F">F</option>
                            </select>
                        </div>

                        <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end' }}>
                            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Save Grade</button>
                        </div>
                    </form>
                </div>
            )}

            {/* ===== BULK GRADE ENTRY TAB ===== */}
            {activeTab === 'bulk' && (
                <div>
                    {/* Config row */}
                    <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '18px', marginBottom: '20px', display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                        <div className="form-group" style={{ margin: 0, flex: '2 1 200px' }}>
                            <label>Course *</label>
                            <select value={bulkCourseId} onChange={e => handleBulkCourseSelect(e.target.value)}>
                                <option value="">-- Select Course --</option>
                                {courses.map(c => <option key={c.id} value={c.id}>{c.name} ({c.code})</option>)}
                            </select>
                        </div>
                        <div className="form-group" style={{ margin: 0, flex: '1 1 180px' }}>
                            <label>Exam Type *</label>
                            <select value={bulkExamType} onChange={e => setBulkExamType(e.target.value)}>
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
                                        {bulkEntries.map(entry => (
                                            <tr key={entry.studentId} style={{ background: entry.marks !== '' ? '#f7fffe' : 'white' }}>
                                                <td style={{ fontWeight: '500' }}>{entry.studentName}</td>
                                                <td style={{ color: '#718096', fontSize: '0.85rem' }}>{entry.enrollmentNumber || '—'}</td>
                                                <td>
                                                    <input
                                                        type="number"
                                                        min="0" max="100" step="0.5"
                                                        placeholder="—"
                                                        value={entry.marks}
                                                        onChange={e => handleBulkMarksChange(entry.studentId, e.target.value)}
                                                        style={{
                                                            width: '100%', padding: '5px 8px', border: '1px solid #e2e8f0',
                                                            borderRadius: '6px', outline: 'none', fontSize: '0.9rem',
                                                            background: entry.marks !== '' ? '#ebf8ff' : 'white'
                                                        }}
                                                    />
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
                                        ))}
                                    </tbody>
                                </table>
                            </div>

                            <div style={{ marginTop: '16px', textAlign: 'right' }}>
                                <button
                                    className="btn btn-primary"
                                    onClick={handleBulkSubmit}
                                    disabled={bulkSaving}
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
