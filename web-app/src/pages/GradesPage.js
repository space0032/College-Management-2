import React, { useState, useEffect, useRef, useCallback } from 'react';
import {
    getAllGrades, getStudentGrades, getFacultyGrades, getStudentCGPA, saveGrade, bulkSaveGrade,
    deleteGrade, getGradeAssignmentMatrix, getCourseExamTypes
} from '../services/gradeService';
import { getMyProfile, getMyCourses } from '../services/facultyService';
import { getEnrolledStudents } from '../services/featureService';
import { getDepartments } from '../services/departmentService';
import { safeParseFloat } from '../utils/validationUtils';
import { getAllCourses } from '../services/courseService';
import { getAllStudents } from '../services/studentService';
import { exportToCSV, exportToExcel } from '../utils/exportUtils';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import jsPDF from 'jspdf';
import 'jspdf-autotable';
import SessionManager from '../utils/SessionManager';
import Modal from '../components/Modal';
import SearchableSelect from '../components/SearchableSelect';
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
    const [departments, setDepartments] = useState([]);
    const [deptFilter, setDeptFilter] = useState('');
    const [subjectFilter, setSubjectFilter] = useState('');
    const [formData, setFormData] = useState({
        studentId: '',
        courseId: '',
        examType: 'MID TERM',
        marksObtained: '',
        outOf: '100',
        grade: 'A'
    });

    // Bulk Grade Entry state
    const [bulkCourseId, setBulkCourseId] = useState('');
    const [bulkExamType, setBulkExamType] = useState('MID TERM');
    const [courseExamTypes, setCourseExamTypes] = useState([]);
    const [bulkOutOf, setBulkOutOf] = useState('100');
    const [bulkEntries, setBulkEntries] = useState([]); // [{studentId, studentName, marks, grade, outOf, current?}]
    const [bulkOverwrite, setBulkOverwrite] = useState(true);
    const [bulkSaving, setBulkSaving] = useState(false);
    const [bulkResult, setBulkResult] = useState(null);
    const [bulkDirty, setBulkDirty] = useState(false);
    const [bulkLoading, setBulkLoading] = useState(false);
    const [bulkSearch, setBulkSearch] = useState('');

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

    const resolveFacultyId = useCallback(async () => {
        try {
            const res = await getMyProfile();
            const id = res.data?.id;
            if (id) setFacultyId(id);
            return id || null;
        } catch (err) {
            return null;
        }
    }, []);

    const loadGradesForRole = useCallback(async (signal) => {
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
    }, [user.role, user.username, facultyId, resolveFacultyId]);

    const loadFormData = useCallback(async (signal) => {
        setFormLoading(true);
        setFormError('');
        try {
            const jobs = [getAllStudents(), getDepartments()];
            if (user.role === 'FACULTY') {
                jobs.push(getMyCourses());
            } else {
                jobs.push(getAllCourses(1, 500));
            }
            const [rStud, rDept, rCour] = await Promise.all(jobs);
            if (signal?.aborted) return;
            setStudents(rStud.data || []);
            setDepartments(rDept.data || []);
            setCourses(rCour.data || []);
        } catch (err) {
            if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
            setFormError(err?.response?.data?.error || 'Could not load students and subjects.');
        } finally {
            if (!signal?.aborted) setFormLoading(false);
        }
    }, [user.role]);

    useEffect(() => {
        const controller = new AbortController();
        if (activeTab === 'view') {
            loadGradesForRole(controller.signal);
        } else if (activeTab === 'manage' || activeTab === 'bulk') {
            loadFormData(controller.signal);
        }
        return () => controller.abort();
    }, [activeTab, user.username, user.role, loadGradesForRole, loadFormData]);

    const pctOf = (marks, outOf) => {
        const o = parseFloat(outOf);
        const m = parseFloat(marks);
        if (o > 0 && Number.isFinite(o)) return (m / o) * 100;
        return m;
    };

    const gradeFromPct = (p) => {
        if (!Number.isFinite(p)) return '';
        if (p >= 90) return 'A';
        if (p >= 75) return 'B';
        if (p >= 60) return 'C';
        if (p >= 50) return 'D';
        if (p >= 40) return 'E';
        return 'F';
    };

    const suggestedGrade = (marks, outOf) => gradeFromPct(pctOf(marks, outOf ?? formData.outOf));

    const openCreateGrade = () => {
        setEditingGrade(null);
        setFormData({
            studentId: '',
            courseId: formatCourseId(bulkCourseId),
            examType: bulkExamType,
            marksObtained: '',
            outOf: parseFloat(bulkOutOf) > 0 ? String(bulkOutOf) : '100',
            grade: 'A'
        });
        setMarksError('');
        setSingleOpen(true);
    };

    const formatCourseId = (id) => id ? String(id) : '';

    const openEditGrade = (g) => {
        setEditingGrade(g);
        setFormData({
            studentId: String(g.studentId || ''),
            courseId: String(g.courseId || ''),
            examType: g.examType || 'MID TERM',
            marksObtained: g.marksObtained !== undefined && g.marksObtained !== null ? String(g.marksObtained) : '',
            outOf: g.maxMarks > 0 ? String(g.maxMarks) : '100',
            grade: g.grade || 'A'
        });
        setMarksError('');
        setSingleOpen(true);
    };

    const refreshView = () => {
        loadGradesForRole();
    };

    const handleSaveGrade = async () => {
        const max = parseFloat(formData.outOf);
        const marks = parseFloat(formData.marksObtained);
        if (!Number.isFinite(max) || max <= 0) {
            setMarksError('Out of must be a positive number.');
            return;
        }
        if (!Number.isFinite(marks) || marks < 0 || marks > max) {
            setMarksError(`Marks must be between 0 and ${max}.`);
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
                maxMarks: max,
                grade: formData.grade
            };
            const refId = getSuccessRefId();
            await saveGrade(payload);
            toast.success(editingGrade ? 'Grade updated.' : 'Grade saved successfully.', { refId });
            setSingleOpen(false);
            setEditingGrade(null);
            setFormData({ studentId: '', courseId: formatCourseId(bulkCourseId), examType: bulkExamType, marksObtained: '', outOf: '100', grade: 'A' });
            refreshView();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not save this grade.');
            toast.error(message, { refId, details: { status } });
        } finally {
            setSingleSaving(false);
        }
    };

    // Auto-load students (with current grades) into bulk table when course/exam chosen
    const loadBulkRoster = async (courseId, examType) => {
        if (!courseId || !examType) { setBulkEntries([]); return; }
        setBulkLoading(true);
        try {
            let rows;
            try {
                const res = await getGradeAssignmentMatrix(courseId, examType);
                rows = res.data || [];
            } catch (err) {
                rows = [];
            }
            if (rows.length === 0) {
                const res = await getEnrolledStudents(courseId);
                rows = (res.data || []).map(s => ({
                    studentId: s.id, studentName: s.name,
                    enrollmentNo: s.username || s.enrollmentId || s.enrollmentNumber,
                    marksObtained: null, grade: null, maxMarks: null
                }));
            }
            if (rows.length === 0) {
                rows = students.map(s => ({
                    studentId: s.id, studentName: s.name,
                    enrollmentNo: s.username || s.enrollmentId || s.enrollmentNumber,
                    marksObtained: null, grade: null, maxMarks: null
                }));
                toast.info('No enrollments found for this course yet — using the full student list.');
            }
            const gradedMarks = rows.filter(r => r.maxMarks > 0).map(r => Number(r.maxMarks));
            let defaultOut = parseFloat(bulkOutOf) || 100;
            if (gradedMarks.length > 0) {
                const sorted = [...gradedMarks].sort((a, b) => a - b);
                defaultOut = sorted[Math.floor(sorted.length / 2)];
            }
            if (defaultOut !== parseFloat(bulkOutOf)) setBulkOutOf(String(defaultOut));
            setBulkEntries(rows.map(r => ({
                studentId: r.studentId,
                studentName: r.studentName,
                enrollmentNumber: r.enrollmentNo || r.enrollmentNumber,
                marks: r.marksObtained != null ? String(r.marksObtained) : '',
                hadGrade: r.marksObtained != null,
                currentMarks: r.marksObtained != null ? r.marksObtained : null,
                currentGrade: r.grade || null,
                grade: r.grade || '',
                gradeManual: false,
                outOf: r.maxMarks > 0 ? String(r.maxMarks) : String(defaultOut),
                over: false
            })));
        } catch (err) {
            toast.error('Could not load the student roster.');
        } finally {
            setBulkLoading(false);
        }
    };

    const selectManageTarget = (courseId) => {
        setBulkCourseId(courseId ? String(courseId) : '');
    };

    const handleBulkCourseSelect = async (courseId) => {
        if (bulkDirty && bulkEntries.some(e => e.marks !== '')) {
            // eslint-disable-next-line no-alert
            if (!window.confirm('Switch course? Unsaved bulk marks will be lost.')) return;
        }
        setBulkCourseId(courseId);
        setBulkResult(null);
        setBulkDirty(false);
        if (!courseId) { setBulkEntries([]); setCourseExamTypes([]); return; }
        let types = [];
        try {
            const res = await getCourseExamTypes(courseId);
            types = res.data || [];
        } catch (err) {
            types = [];
        }
        setCourseExamTypes(types);
        const exam = types.includes(bulkExamType) ? bulkExamType : (types[0] || bulkExamType);
        if (exam !== bulkExamType) setBulkExamType(exam);
        await loadBulkRoster(courseId, exam);
    };

    const handleBulkExamTypeChange = (examType) => {
        if (bulkDirty && bulkEntries.some(e => e.marks !== '')) {
            // eslint-disable-next-line no-alert
            if (!window.confirm('Switch exam type? Unsaved bulk marks will be lost.')) return;
        }
        setBulkExamType(examType);
        setBulkResult(null);
        setBulkDirty(false);
        loadBulkRoster(bulkCourseId, examType);
    };

    const handleBulkOutOfChange = (v) => {
        setBulkOutOf(v);
        const o = parseFloat(v);
        if (Number.isFinite(o) && o > 0) {
            setBulkDirty(true);
            setBulkEntries(prev => prev.map(e => {
                const e2 = { ...e, outOf: v };
                if (e2.marks !== '') e2.grade = e2.gradeManual ? e2.grade : autoGrade(e2.marks, o);
                return e2;
            }));
        }
    };

    const handleBulkEntryChange = (studentId, field, value) => {
        setBulkDirty(true);
        setBulkEntries(prev => prev.map(e =>
            e.studentId === studentId
                ? { ...e, [field]: value, gradeManual: field === 'grade' ? (value !== '' ? true : e.gradeManual) : e.gradeManual }
                : e
        ));
    };

    // Auto-derive grade from marks (percentage is marks/outOf*100)
    const autoGrade = (marks, outOf) => {
        const p = pctOf(marks, outOf);
        return Number.isFinite(p) ? gradeFromPct(p) : '';
    };

    const isValidMarks = (marks, outOf) => {
        if (marks === '' || marks === null || marks === undefined) return true;
        const m = parseFloat(marks);
        const o = parseFloat(outOf) || 100;
        return Number.isFinite(m) && m >= 0 && m <= o;
    };

    const handleBulkMarksChange = (studentId, marks) => {
        setBulkDirty(true);
        setBulkEntries(prev => prev.map(e => {
            if (e.studentId !== studentId) return e;
            const outOf = parseFloat(e.outOf) || 100;
            const m = parseFloat(marks);
            const over = marks !== '' && (Number.isNaN(m) || m < 0 || m > outOf);
            const grade = (marks === '' || e.gradeManual) ? e.grade : autoGrade(marks, e.outOf);
            return { ...e, marks, grade, over };
        }));
    };

    // Keyboard-friendly: Enter / ArrowDown moves to next row, ArrowUp to previous.
    const focusMarks = (studentId) => {
        marksRefs.current[studentId]?.focus?.();
        marksRefs.current[studentId]?.select?.();
    };

    const handleBulkKeyDown = (e, index, list = bulkEntries) => {
        if (e.key === 'Enter' || e.key === 'ArrowDown') {
            e.preventDefault();
            const next = list[index + 1];
            if (next) focusMarks(next.studentId);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            const prev = list[index - 1];
            if (prev) focusMarks(prev.studentId);
        }
    };

    const handleBulkSubmit = async () => {
        if (!bulkCourseId) { toast.error('Select a course before saving.'); return; }
        const invalid = bulkEntries.filter(e => e.over || (e.marks !== '' && !isValidMarks(e.marks, e.outOf)));
        if (invalid.length > 0) {
            toast.error(`${invalid.length} row(s) have marks outside their allowed range. Fix the highlighted rows.`);
            const first = invalid[0];
            focusMarks(first.studentId);
            return;
        }
        const filled = bulkEntries.filter(e => e.marks !== '' && !Number.isNaN(parseFloat(e.marks)));
        if (filled.length === 0) { toast.error('Please enter marks for at least one student.'); return; }

        const existing = filled.filter(e => e.hadGrade);
        const overwrittenCount = existing.length;
        let submitted = filled;
        let skipped = 0;
        if (!bulkOverwrite && existing.length > 0) {
            submitted = filled.filter(e => !e.hadGrade);
            skipped = existing.length;
        } else if (existing.length > 0) {
            // eslint-disable-next-line no-alert
            if (!window.confirm(`Overwrite the existing ${existing.length} grade(s) for this subject exam?`)) return;
        }
        if (submitted.length === 0) { toast.info('No rows to save — existing grades are being kept.'); return; }

        setBulkSaving(true);
        setBulkResult(null);
        try {
            const payload = submitted.map(entry => ({
                studentId: entry.studentId,
                courseId: parseInt(bulkCourseId),
                examType: bulkExamType,
                marksObtained: safeParseFloat(entry.marks),
                maxMarks: parseFloat(entry.outOf) || 100,
                grade: entry.grade || autoGrade(entry.marks, entry.outOf)
            }));
            const res = await bulkSaveGrade(payload);
            const saved = res.data?.saved ?? submitted.length;
            const failed = submitted.length - saved;
            setBulkResult({ saved, failed, skipped, overwritten: overwrittenCount, total: filled.length });
            setBulkDirty(false);
            try { localStorage.removeItem('cm_bulk_draft'); } catch (err) { /* ignore */ }
            const refId = getSuccessRefId();
            if (failed === 0 && skipped === 0) toast.success(`Saved ${saved} of ${filled.length} grades.`, { refId });
            else toast.info(`Saved ${saved}; ${overwrittenCount} overwritten, ${skipped} kept as-is.`, { refId });
            refreshView();
            loadBulkRoster(bulkCourseId, bulkExamType);
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not bulk save grades.');
            toast.error(message, { refId, details: { status } });
        } finally {
            setBulkSaving(false);
        }
    };

    const handleDeleteGrade = async (g) => {
        // eslint-disable-next-line no-alert
        if (!window.confirm(`Delete the ${g.examType} grade for ${g.studentName || 'this student'}?`)) return;
        try {
            await deleteGrade(g.id);
            toast.success('Grade deleted.');
            refreshView();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not delete this grade.');
            toast.error(message, { refId, details: { status } });
        }
    };

    // Persist in-progress bulk entries so a refresh doesn't lose work.
    useEffect(() => {
        if (bulkDirty && (bulkCourseId || bulkEntries.length > 0)) {
            try {
                localStorage.setItem('cm_bulk_draft', JSON.stringify({ bulkCourseId, bulkExamType, bulkOutOf, bulkEntries, savedAt: Date.now() }));
            } catch (err) { /* ignore */ }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [bulkEntries, bulkCourseId, bulkExamType, bulkOutOf, bulkDirty]);

    useEffect(() => {
        try {
            const raw = localStorage.getItem('cm_bulk_draft');
            if (raw) {
                const draft = JSON.parse(raw);
                if (draft.bulkCourseId && draft.bulkEntries && draft.bulkEntries.length > 0) {
                    setBulkCourseId(draft.bulkCourseId);
                    setBulkExamType(draft.bulkExamType || 'MID TERM');
                    setBulkOutOf(draft.bulkOutOf || '100');
                    setBulkEntries(draft.bulkEntries);
                    setBulkDirty(true);
                    toast.info('Restored unsaved bulk entry from your last session.');
                }
            }
        } catch (err) { /* ignore */ }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

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

    const filledCount = bulkEntries.filter(e => e.marks !== '').length;
    const existingCount = bulkEntries.filter(e => e.hadGrade).length;
    const passCount = bulkEntries.filter(e => e.marks !== '' && e.grade && e.grade !== 'F').length;
    const pendingCount = bulkEntries.filter(e => e.marks === '').length;
    const visibleCourses = deptFilter
        ? courses.filter(c => String(c.departmentId) === String(deptFilter))
        : courses;
    const fq = subjectFilter.trim().toLowerCase();
    const filteredCourses = fq
        ? visibleCourses.filter(c =>
            (c.name || '').toLowerCase().includes(fq) ||
            (c.code || '').toLowerCase().includes(fq) ||
            (c.specialization || '').toLowerCase().includes(fq))
        : visibleCourses;
    const sq = bulkSearch.trim().toLowerCase();
    const visibleBulkEntries = sq
        ? bulkEntries.filter(e =>
            (e.studentName || '').toLowerCase().includes(sq) ||
            (e.enrollmentNumber || '').toLowerCase().includes(sq))
        : bulkEntries;
    const examTypeOptions = () => (
        courseExamTypes.length
            ? EXAM_TYPES.concat(courseExamTypes.filter(t => !EXAM_TYPES.includes(t)))
            : EXAM_TYPES
    );
    const courseLabel = (c) => `${c.code} — ${c.name}${c.specialization ? ` [${c.specialization}]` : ''}`;
    const withSelectedCourse = (list, selectedId) => {
        const opts = list.map(c => ({ value: c.id, label: courseLabel(c) }));
        const selId = selectedId != null ? String(selectedId) : '';
        if (selId && !list.some(c => String(c.id) === selId)) {
            const sel = courses.find(c => String(c.id) === selId);
            if (sel) opts.unshift({ value: sel.id, label: `${courseLabel(sel)} ✓ (selected)` });
        }
        return opts;
    };
    const selectedCourseName = () => {
        const sel = courses.find(c => String(c.id) === String(bulkCourseId));
        return sel ? courseLabel(sel) : '';
    };
    const clearFilters = () => { setDeptFilter(''); setSubjectFilter(''); };

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
                                                <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                                                    <button className="btn btn-sm btn-secondary" onClick={() => openEditGrade(g)}>Edit</button>{' '}
                                                    <button className="btn btn-sm btn-danger" onClick={() => handleDeleteGrade(g)}>Delete</button>
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
                    {canEdit && (
                        <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '14px 18px', marginBottom: '20px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px', marginBottom: '10px' }}>
                                <span style={{ fontSize: '0.78rem', fontWeight: '700', color: '#475569', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                                    🎯 Assignment Target
                                </span>
                                {(deptFilter || subjectFilter) && (
                                    <button className="btn btn-secondary btn-sm" onClick={clearFilters}>✕ Clear filters</button>
                                )}
                            </div>
                            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'flex-end' }}>
                                <div className="form-group" style={{ margin: 0, flex: '1 1 180px' }}>
                                    <label>Department</label>
                                    <select className="form-control" value={deptFilter} onChange={e => setDeptFilter(e.target.value)}>
                                        <option value="">All Departments</option>
                                        {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                                    </select>
                                </div>
                                <div className="form-group" style={{ margin: 0, flex: '1 1 180px' }}>
                                    <label>Subject search</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder="Name / code / track…"
                                        value={subjectFilter}
                                        onChange={e => setSubjectFilter(e.target.value)}
                                    />
                                </div>
                                <div className="form-group" style={{ margin: 0, flex: '2 1 240px' }}>
                                    <label>Subject → Course</label>
                                    <SearchableSelect
                                        options={withSelectedCourse(filteredCourses, bulkCourseId)}
                                        value={bulkCourseId ? Number(bulkCourseId) : ''}
                                        onChange={v => selectManageTarget(String(v))}
                                        placeholder="Type to pick the assignment target…"
                                    />
                                </div>
                                <div className="form-group" style={{ margin: 0, flex: '1 1 170px' }}>
                                    <label>Exam Type</label>
                                    <select className="form-control" value={bulkExamType} onChange={e => setBulkExamType(e.target.value)}>
                                        {examTypeOptions().map(t => <option key={t} value={t}>{t}</option>)}
                                    </select>
                                </div>
                            </div>
                            <div style={{ marginTop: '10px', fontSize: '0.78rem', color: '#94a3b8', display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                <span>{filteredCourses.length} subject(s) match{fq ? ` “${subjectFilter.trim()}”` : ' the current filters'} ·</span>
                                {bulkCourseId
                                    ? <span><strong style={{ color: '#475569' }}>Target:</strong> {selectedCourseName() || 'selected subject'} · “+ Enter Grade” will prefill it.</span>
                                    : <span>No target selected — “+ Enter Grade” opens empty.</span>}
                            </div>
                        </div>
                    )}
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
                        <SearchableSelect
                            options={students.map(s => ({ value: s.id, label: `${s.name} (${s.username || s.enrollmentId || s.enrollmentNumber || 'N/A'})` }))}
                            value={formData.studentId ? Number(formData.studentId) : ''}
                            onChange={v => setFormData({ ...formData, studentId: String(v) })}
                            placeholder="Search student by name or enrollment…"
                            disabled={Boolean(editingGrade)}
                        />
                        {Boolean(editingGrade) &&
                            <span className="field-hint">Student can't be changed on edit.</span>}
                    </div>
                    <div className="form-group">
                        <label className="form-label">Subject *</label>
                        <SearchableSelect
                            options={withSelectedCourse(visibleCourses, formData.courseId)}
                            value={formData.courseId ? Number(formData.courseId) : ''}
                            onChange={v => setFormData({ ...formData, courseId: String(v) })}
                            placeholder="Search subject by code or name…"
                            disabled={Boolean(editingGrade)}
                        />
                        {Boolean(editingGrade) &&
                            <span className="field-hint">Subject can't be changed on edit.</span>}
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
                            {(courseExamTypes.length ? EXAM_TYPES.concat(courseExamTypes.filter(t => !EXAM_TYPES.includes(t))) : EXAM_TYPES).map(t => <option key={t} value={t}>{t}</option>)}
                        </select>
                        {Boolean(editingGrade) &&
                            <span className="field-hint">Exam type can't be changed on edit — it identifies the grade record.</span>}
                    </div>
                    <div className="form-group">
                        <label className="form-label">Out Of *</label>
                        <input
                            type="number"
                            step="1"
                            min="1"
                            required
                            className="form-control"
                            value={formData.outOf}
                            disabled={Boolean(editingGrade)}
                            onChange={e => setFormData({ ...formData, outOf: e.target.value })}
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Marks Obtained *</label>
                        <input
                            type="number"
                            step="0.1"
                            min="0"
                            max={formData.outOf || 100}
                            required
                            className={`form-control${marksError ? ' is-invalid' : ''}`}
                            value={formData.marksObtained}
                            onChange={e => {
                                const v = e.target.value;
                                const outOf = parseFloat(formData.outOf) || 100;
                                setFormData(prev => {
                                    const sg = suggestedGrade(v, prev.outOf);
                                    return { ...prev, marksObtained: v, grade: v === '' ? prev.grade : (sg || prev.grade) };
                                });
                                const m = parseFloat(v);
                                if (v !== '' && (!Number.isFinite(m) || m < 0 || m > outOf)) setMarksError(`Marks must be between 0 and ${outOf}.`);
                                else setMarksError('');
                            }}
                            aria-invalid={Boolean(marksError)}
                        />
                        {marksError
                            ? <span className="field-error" role="alert">{marksError}</span>
                            : <span className="field-hint">Allowed range 0–{formData.outOf || 100}. Grade auto-suggests: {suggestedGrade(formData.marksObtained, formData.outOf) || '—'} (A ≥ 90, B ≥ 75, C ≥ 60, D ≥ 50, E ≥ 40 percentage).</span>}
                    </div>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Letter Grade *</label>
                        <select
                            required
                            className="form-control"
                            value={formData.grade}
                            onChange={e => setFormData({ ...formData, grade: e.target.value })}
                        >
                            {GRADES.map(g => <option key={g} value={g}>{g}{suggestedGrade(formData.marksObtained, formData.outOf) === g ? ' (suggested)' : ''}</option>)}
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
                        {bulkLoading && <span className="badge badge-primary">Loading roster…</span>}
                    </div>
{/* Config row */}
                    <div style={{ background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '14px 18px', marginBottom: '20px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px', marginBottom: '10px' }}>
                            <span style={{ fontSize: '0.78rem', fontWeight: '700', color: '#475569', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                                📋 Bulk Assignment
                            </span>
                            {(deptFilter || subjectFilter) && (
                                <button className="btn btn-secondary btn-sm" onClick={clearFilters}>✕ Clear filters</button>
                            )}
                        </div>
                        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'flex-end' }}>
                            <div className="form-group" style={{ margin: 0, flex: '1 1 170px' }}>
                                <label>Department</label>
                                <select className="form-control" value={deptFilter} onChange={e => setDeptFilter(e.target.value)}>
                                    <option value="">All Departments</option>
                                    {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                                </select>
                            </div>
                            <div className="form-group" style={{ margin: 0, flex: '1 1 180px' }}>
                                <label>Subject search</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="Name / code / track…"
                                    value={subjectFilter}
                                    onChange={e => setSubjectFilter(e.target.value)}
                                />
                            </div>
                            <div className="form-group" style={{ margin: 0, flex: '2 1 220px' }}>
                                <label>Subject → Course *</label>
                                <SearchableSelect
                                    options={withSelectedCourse(filteredCourses, bulkCourseId)}
                                    value={bulkCourseId ? Number(bulkCourseId) : ''}
                                    onChange={v => handleBulkCourseSelect(String(v))}
                                    placeholder="Type to search subject / course…"
                                />
                            </div>
                            <div className="form-group" style={{ margin: 0, flex: '1 1 170px' }}>
                                <label>Exam Type *</label>
                                <select className="form-control" required value={bulkExamType} onChange={e => handleBulkExamTypeChange(e.target.value)}>
                                    {examTypeOptions().map(t => <option key={t} value={t}>{t}</option>)}
                                </select>
                            </div>
                            <div className="form-group" style={{ margin: 0, flex: '1 1 130px' }}>
                                <label>Out Of (default)</label>
                                <input
                                    type="number"
                                    className="form-control"
                                    min="1"
                                    step="1"
                                    value={bulkOutOf}
                                    onChange={e => handleBulkOutOfChange(e.target.value)}
                                />
                            </div>
                            <label style={{ display: 'flex', alignItems: 'center', flex: '1 1 180px', fontSize: '0.85rem', gap: '6px', color: '#475569', minHeight: '38px' }}>
                                <input
                                    type="checkbox"
                                    checked={bulkOverwrite}
                                    onChange={e => setBulkOverwrite(e.target.checked)}
                                />
                                Overwrite existing grades
                            </label>
                        </div>
                        <div style={{ marginTop: '10px', fontSize: '0.78rem', color: '#94a3b8', display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                            <span>{filteredCourses.length} subject(s) match{fq ? ` “${subjectFilter.trim()}”` : ' the current filters'} ·</span>
                            {bulkCourseId
                                ? <span><strong style={{ color: '#475569' }}>Roster:</strong> {selectedCourseName() || 'selected subject'} · {bulkExamType} · default Out Of {bulkOutOf}</span>
                                : <span>Pick a subject course above to load its roster.</span>}
                        </div>
                    </div>

                    {/* Assignment summary */}
                    {bulkCourseId && !bulkLoading && bulkEntries.length > 0 && (
                        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '16px' }}>
                            <span className="badge badge-primary">{filledCount}/{bulkEntries.length} graded · {pendingCount} pending</span>
                            <span className="badge badge-success">Pass: {passCount}</span>
                            <span className="badge badge-secondary">Existing: {existingCount}</span>
                            {GRADES.map(g => {
                                const c = bulkEntries.filter(e => e.grade === g).length;
                                return c > 0 ? <span key={g} className="badge badge-warning">{g}: {c}</span> : null;
                            })}
                        </div>
                    )}

                    {/* Result banner */}
                    {bulkResult && (
                        <div style={{
                            padding: '12px 16px', borderRadius: '8px', marginBottom: '16px',
                            background: bulkResult.failed === 0 && bulkResult.skipped === 0 ? '#f0fff4' : '#fffaf0',
                            border: `1px solid ${bulkResult.failed === 0 && bulkResult.skipped === 0 ? '#9ae6b4' : '#fbd38d'}`
                        }}>
                            <strong>{bulkResult.failed === 0 && bulkResult.skipped === 0 ? '✅' : '⚠️'}</strong>&nbsp;
                            Saved <strong>{bulkResult.saved}</strong> of <strong>{bulkResult.total}</strong> graded rows.
                            {bulkResult.overwritten > 0 && <span style={{ color: '#b7791f' }}> {bulkResult.overwritten} overwritten.</span>}
                            {bulkResult.skipped > 0 && <span style={{ color: '#c05621' }}> {bulkResult.skipped} skipped (kept existing grades).</span>}
                            {bulkResult.failed > 0 && <span style={{ color: '#c05621' }}> {bulkResult.failed} failed.</span>}
                        </div>
                    )}

                    {!bulkCourseId ? (
                        <div style={{ textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
                            <div style={{ fontSize: '2.5rem', marginBottom: '12px' }}>📋</div>
                            Select a course above to load the student list.
                        </div>
                    ) : (
                        <>
                            <div style={{ marginBottom: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
                                    <span style={{ fontSize: '0.85rem', color: '#718096' }}>
                                        {filledCount} of {bulkEntries.length} students filled{sq ? ` · showing ${visibleBulkEntries.length}` : ''}
                                    </span>
                                    <input
                                        type="text"
                                        placeholder="Filter roster: name / enrollment…"
                                        value={bulkSearch}
                                        onChange={e => setBulkSearch(e.target.value)}
                                        style={{ padding: '6px 8px', border: '1px solid #e2e8f0', borderRadius: '6px', fontSize: '0.85rem', maxWidth: 240 }}
                                    />
                                </div>
                                <button
                                    className="btn btn-primary"
                                    onClick={handleBulkSubmit}
                                    disabled={bulkSaving}
                                >
                                    {bulkSaving ? 'Saving…' : `💾 Save All Grades (${filledCount})`}
                                </button>
                            </div>

{bulkLoading ? (
                            <div style={{ marginTop: '8px' }}><SkeletonTable rows={6} cols={4} /></div>
                        ) : (
                            <div className="data-table-container">
                                <table className="data-table">
                                    <thead>
                                        <tr>
                                            <th>Student Name</th>
                                            <th>Enrollment No</th>
                                            <th style={{ width: '120px' }}>Marks</th>
                                            <th style={{ width: '100px' }}>Out Of</th>
                                            <th style={{ width: '110px' }}>Grade</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {visibleBulkEntries.map((entry, idx) => {
                                            const invalid = entry.over || (entry.marks !== '' && !isValidMarks(entry.marks, entry.outOf));
                                            const rowMax = parseFloat(entry.outOf) || 100;
                                            return (
                                            <tr key={entry.studentId} style={{ background: invalid ? '#fff5f5' : entry.marks !== '' ? '#f7fffe' : 'white' }}>
                                                <td style={{ fontWeight: '500' }}>
                                                    {entry.studentName}
                                                    {entry.hadGrade && <span style={{ marginLeft: '6px', color: '#b7791f', fontSize: '.72rem', background: '#fef3c7', padding: '1px 6px', borderRadius: '4px' }}>existing</span>}
                                                    {entry.gradeManual && entry.grade !== '' && <span style={{ marginLeft: '6px', color: '#6d28d9', fontSize: '.72rem', background: '#ede9fe', padding: '1px 6px', borderRadius: '4px' }}>manual</span>}
                                                </td>
                                                <td style={{ color: '#718096', fontSize: '0.85rem' }}>{entry.enrollmentNumber || '—'}</td>
                                                <td>
                                                    <input
                                                        ref={(el) => { marksRefs.current[entry.studentId] = el; }}
                                                        type="number"
                                                        min="0" max={rowMax} step="0.5"
                                                        placeholder="—"
                                                        value={entry.marks}
                                                        aria-label={`Marks for ${entry.studentName} (0 to ${rowMax})`}
                                                        aria-invalid={invalid}
                                                        onChange={e => handleBulkMarksChange(entry.studentId, e.target.value)}
                                                        onKeyDown={e => handleBulkKeyDown(e, idx, visibleBulkEntries)}
                                                        style={{
                                                            width: '100%', padding: '5px 8px', border: `1px solid ${invalid ? '#f04438' : '#e2e8f0'}`,
                                                            borderRadius: '6px', outline: 'none', fontSize: '0.9rem',
                                                            background: invalid ? '#fff5f5' : entry.marks !== '' ? '#ebf8ff' : 'white'
                                                        }}
                                                    />
                                                    {invalid && <div style={{ color: '#b42318', fontSize: '.72rem', marginTop: '2px' }}>0–{rowMax} only</div>}
                                                </td>
                                                <td>
                                                    <input
                                                        type="number"
                                                        min="1" step="1"
                                                        value={entry.outOf}
                                                        aria-label={`Out of for ${entry.studentName}`}
                                                        onChange={e => handleBulkEntryChange(entry.studentId, 'outOf', e.target.value)}
                                                        style={{
                                                            width: '100%', padding: '5px 8px', border: '1px solid #e2e8f0',
                                                            borderRadius: '6px', outline: 'none', fontSize: '0.9rem'
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
                                                        <option value="">—</option>
                                                        {GRADES.map(g => <option key={g} value={g}>{g}</option>)}
                                                    </select>
                                                </td>
                                            </tr>
                                            );
                                        })}
                                        {visibleBulkEntries.length === 0 && (
                                            <tr>
                                                <td colSpan="5" style={{ textAlign: 'center', color: '#94a3b8', padding: '24px' }}>
                                                    {bulkEntries.length === 0 ? 'No enrolled students found for this subject exam.' : 'No students match your roster filter.'}
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        )}

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
