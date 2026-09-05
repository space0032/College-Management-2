import React, { useState, useEffect } from 'react';
import {
    getAssignments, createAssignment, getSubmissions,
    getStudentSubmission, submitAssignment, gradeSubmission
} from '../services/assignmentService';
import { getAllCourses } from '../services/courseService';
import { searchStudents, getStudentCourses } from '../services/studentService';
import SessionManager from '../utils/SessionManager';
import AiAssistModal from '../components/AiAssistModal';
import Modal from '../components/Modal';
import { toast } from '../components/Toast';
import { getErrorMessage, getSuccessRefId } from '../utils/error';
import { SkeletonCards } from '../components/Skeleton';

const AssignmentPage = () => {
    const [, setActiveTab] = useState('browse'); // browse kept for legacy navigation state
    const [assignments, setAssignments] = useState([]);
    const [submissions, setSubmissions] = useState([]);
    const [selectedAssignment, setSelectedAssignment] = useState(null);

    // Forms
    const [assignmentForm, setAssignmentForm] = useState({ title: '', description: '', dueDate: '', courseId: '', semester: 1 });
    const [submissionText, setSubmissionText] = useState('');
    const [gradingForm, setGradingForm] = useState({ grade: '', feedback: '' });
    const [selectedSubmission, setSelectedSubmission] = useState(null);
    const [courses, setCourses] = useState([]);
    const [enrolledCourses, setEnrolledCourses] = useState([]);
    const [studentId, setStudentId] = useState(null);
    const [aiOpen, setAiOpen] = useState(false);
    const [createOpen, setCreateOpen] = useState(false);
    const [submitOpen, setSubmitOpen] = useState(false);
    const [gradeOpen, setGradeOpen] = useState(false);
    const [listLoading, setListLoading] = useState(true);
    const [listError, setListError] = useState('');
    const [saving, setSaving] = useState(false);

    const user = SessionManager.getUser() || { id: null, username: '', role: 'STUDENT' };
    const userId = user.id;
    const username = user.username;
    const userRole = user.role;

    const resolveStudentId = React.useCallback(async () => {
        if (userRole !== 'STUDENT') return null;
        if (studentId) return studentId;
        try {
            const res = await searchStudents(username);
            const match = (res.data || []).find(s => s.username === username || s.enrollmentId === username) || (res.data || [])[0];
            if (match) {
                setStudentId(match.id);
                return match.id;
            }
        } catch { /* ignore */ }
        return null;
    }, [userRole, username, studentId]);

    const loadAssignments = React.useCallback(async (signal) => {
        setListLoading(true);
        setListError('');
        try {
            if (userRole === 'STUDENT') {
                const sid = await resolveStudentId();
                if (signal?.aborted) return;
                const res = await getAssignments(userRole, userId, sid || undefined, signal);
                setAssignments(res.data || []);
                if (sid) {
                    try {
                        const cRes = await getStudentCourses(sid, signal);
                        if (!signal?.aborted) setEnrolledCourses(cRes.data || []);
                    } catch { /* ignore */ }
                }
            } else {
                const res = await getAssignments(userRole, userId, undefined, signal);
                if (!signal?.aborted) setAssignments(res.data || []);
            }
        } catch (err) {
            if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
            setListError(err?.response?.data?.error || 'Could not load assignments.');
        } finally {
            if (!signal?.aborted) setListLoading(false);
        }
    }, [userId, userRole, resolveStudentId]);

    useEffect(() => {
        const controller = new AbortController();
        loadAssignments(controller.signal);
        if (userRole === 'FACULTY' || userRole === 'ADMIN') {
            getAllCourses(1, 500, controller.signal).then(res => setCourses(res.data || [])).catch(() => { });
        }
        return () => controller.abort();
    }, [loadAssignments, userRole]);

    const handleCreateAssignment = async () => {
        if (!assignmentForm.courseId) { toast.error('Please select a subject.'); return; }
        const today = new Date().toISOString().split('T')[0];
        if (assignmentForm.dueDate && assignmentForm.dueDate < today) { toast.error('Due date cannot be in the past.'); return; }
        const selected = courses.find(c => String(c.id) === String(assignmentForm.courseId));
        setSaving(true);
        try {
            const refId = getSuccessRefId();
            await createAssignment({
                ...assignmentForm,
                courseId: parseInt(assignmentForm.courseId),
                semester: selected?.semester ? parseInt(selected.semester) : parseInt(assignmentForm.semester),
                createdBy: userId
            });
            setAssignmentForm({ title: '', description: '', dueDate: '', courseId: '', semester: 1 });
            setCreateOpen(false);
            toast.success('Assignment published.', { refId });
            loadAssignments();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not publish this assignment.');
            toast.error(message, { refId, details: { status } });
        } finally { setSaving(false); }
    };

    const handleOpenSubmit = async (a) => {
        setSelectedAssignment(a);
        try {
            const res = await getStudentSubmission(a.id, username);
            if (res.data) {
                setSubmissionText(res.data.submissionText);
                setSelectedSubmission(res.data);
            } else {
                setSubmissionText('');
                setSelectedSubmission(null);
            }
            setSubmitOpen(true);
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not load submission state.');
            toast.error(message, { refId });
        }
    };

    const handleSubmitWork = async () => {
        setSaving(true);
        try {
            const refId = getSuccessRefId();
            await submitAssignment(selectedAssignment.id, {
                enrollmentId: username,
                submissionText: submissionText
            });
            toast.success('Assignment submitted successfully.', { refId });
            setSubmitOpen(false);
            setActiveTab('browse');
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not submit this assignment.');
            toast.error(message, { refId, details: { status } });
        } finally { setSaving(false); }
    };

    const handleOpenGrade = async (a) => {
        setSelectedAssignment(a);
        try {
            const res = await getSubmissions(a.id);
            setSubmissions(res.data || []);
            setGradeOpen(true);
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not load submissions.');
            toast.error(message, { refId });
        }
    };

    const submitGrade = async () => {
        if (!selectedSubmission) return;
        const grade = Number(gradingForm.grade);
        if (!Number.isFinite(grade) || grade < 0 || grade > 100) {
            toast.error('Grade must be between 0 and 100.');
            return;
        }
        setSaving(true);
        try {
            await gradeSubmission(selectedSubmission.id, {
                grade,
                feedback: gradingForm.feedback
            });
            setGradingForm({ grade: '', feedback: '' });
            setSelectedSubmission(null);
            toast.success('Grade saved.', { refId: getSuccessRefId() });
            const res = await getSubmissions(selectedAssignment.id);
            setSubmissions(res.data || []);
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not save this grade.');
            toast.error(message, { refId, details: { status } });
        } finally { setSaving(false); }
    };

    // F1: insert an AI-generated draft into the create form for human review.
    // A "Title: ..." line fills the title when it is still empty.
    const handleAiInsert = (draft) => {
        const lines = draft.split('\n');
        const titleLine = lines.find(l => /^title\s*:/i.test(l.trim()));
        const title = titleLine ? titleLine.replace(/^title\s*:/i, '').trim().slice(0, 120) : '';
        setAssignmentForm(prev => ({
            ...prev,
            title: prev.title || title,
            description: draft
        }));
    };

    const selectedCourseName = (() => {
        const c = courses.find(c => String(c.id) === String(assignmentForm.courseId));
        return c ? (c.name || c.courseName || '') : '';
    })();

    // Stats
    const totalAssignments = assignments.length;
    const dueSoon = assignments.filter(a => {
        const diff = new Date(a.dueDate).getTime() - Date.now();
        return diff > 0 && diff < 86400000 * 3; // 3 days
    }).length;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">📝 Subject Assignments</h1>
                    <p className="page-subtitle">Track, submit, and grade academic assignments across your enrolled subjects{userRole === 'STUDENT' && enrolledCourses.length > 0 ? ` (${enrolledCourses.length} enrolled)` : ''}</p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <span className="badge badge-primary">All records: {assignments.length}</span>
                    {(userRole === 'FACULTY' || userRole === 'ADMIN') && (
                        <button className="btn btn-primary" onClick={() => setCreateOpen(true)}>
                            + New Assignment
                        </button>
                    )}
                </div>
            </div>

            {/* Stats Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '25px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #f6ad55 0%, #ed8936 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.9rem', opacity: 0.8 }}>Total Assignments</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>{totalAssignments}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>Due within 72h</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#e53e3e' }}>{dueSoon}</div>
                </div>
                {userRole === 'STUDENT' ? (
                    <div className="stat-card">
                        <div style={{ fontSize: '0.9rem', color: '#666' }}>Pending Submissions</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3182ce' }}>N/A</div>
                    </div>
                ) : (
                    <div className="stat-card">
                        <div style={{ fontSize: '0.9rem', color: '#666' }}>Pending Grading</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3182ce' }}>N/A</div>
                    </div>
                )}
                <div className="stat-card">
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>Average Score</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#48bb78' }}>N/A</div>
                </div>
            </div>

            {listError && (
                <div className="retry-bar" role="alert" style={{ marginBottom: '16px' }}>
                    <span>{listError}</span>
                    <button className="btn btn-secondary btn-sm" onClick={() => loadAssignments()}>Retry</button>
                </div>
            )}
            {listLoading ? (
                <SkeletonCards count={6} />
            ) : (
                <div className="card-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
                    {assignments.length === 0 ? (
                        <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
                            <div style={{ fontSize: '3rem', marginBottom: '15px' }}>📝</div>
                            <p>No assignments found. Sit back and relax!</p>
                        </div>
                    ) : (
                        assignments.map(a => {
                            const dueDate = new Date(a.dueDate);
                            const isOverdue = dueDate < new Date();
                            const isDueSoon = !isOverdue && (dueDate.getTime() - Date.now() < 86400000 * 3);

                            return (
                                <div key={a.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column', border: '1px solid #edf2f7', transition: 'transform 0.2s' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '15px' }}>
                                        <div>
                                            <h3 style={{ margin: 0, fontSize: '1.2rem', color: '#2d3748' }}>{a.title}</h3>
                                            <div style={{ fontSize: '0.8rem', color: '#718096', marginTop: '4px' }}>
                                                {a.courseName || `Subject ID ${a.courseId}`}
                                            </div>
                                        </div>
                                        <span className={`badge ${isOverdue ? 'badge-danger' : isDueSoon ? 'badge-warning' : 'badge-success'}`}>
                                            {isOverdue ? 'Overdue' : isDueSoon ? 'Due Soon' : 'Active'}
                                        </span>
                                    </div>

                                    <p style={{ margin: '0 0 20px 0', color: '#4a5568', fontSize: '0.9rem', flex: 1, lineHeight: '1.5' }}>
                                        {a.description.length > 150 ? a.description.substring(0, 150) + '...' : a.description}
                                    </p>

                                    <div style={{
                                        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                                        padding: '12px 15px', background: '#f8fafc', borderRadius: '8px', marginBottom: '15px'
                                    }}>
                                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>
                                            🗓️ Due: <strong>{dueDate.toLocaleDateString()}</strong>
                                        </div>
                                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>
                                            👤 By: <strong>{a.facultyName || 'Staff'}</strong>
                                        </div>
                                    </div>

                                    <div style={{ display: 'flex', gap: '10px' }}>
                                        {userRole === 'STUDENT' && (
                                            <button className="btn btn-primary" style={{ flex: 1, fontWeight: 'bold' }} onClick={() => handleOpenSubmit(a)}>
                                                {isOverdue ? 'View & Submit Late' : 'Open Assignment'}
                                            </button>
                                        )}
                                        {(userRole === 'FACULTY' || userRole === 'ADMIN') && (
                                            <button className="btn btn-secondary" style={{ flex: 1, fontWeight: 'bold' }} onClick={() => handleOpenGrade(a)}>
                                                Manage Submissions
                                            </button>
                                        )}
                                    </div>
                                </div>
                            );
                        })
                    )}
                </div>
            )}

            <Modal
                isOpen={createOpen}
                title="🚀 Publish New Assignment"
                onClose={() => setCreateOpen(false)}
                onSubmit={handleCreateAssignment}
                submitLabel="Launch Assignment"
                submitting={saving}
                isDirty={Boolean(assignmentForm.title || assignmentForm.description || assignmentForm.courseId)}
                size="large"
            >
                <form className="form-grid" onSubmit={(e) => { e.preventDefault(); handleCreateAssignment(); }}>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Assignment Title *</label>
                        <input required type="text" className="form-control" value={assignmentForm.title} onChange={e => setAssignmentForm({ ...assignmentForm, title: e.target.value })} placeholder="e.g. Mid-term Project Phase 1" />
                    </div>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Description & Learning Objectives *</label>
                        <textarea required rows="5" className="form-control" value={assignmentForm.description} onChange={e => setAssignmentForm({ ...assignmentForm, description: e.target.value })} placeholder="Detail the requirements, constraints, and submission format..."></textarea>
                        <button type="button" className="btn btn-secondary btn-sm" style={{ marginTop: '8px' }} onClick={() => setAiOpen(true)}>✨ Generate Questions with AI</button>
                    </div>
                    <div className="form-group">
                        <label className="form-label">Submission Deadline *</label>
                        <input required type="date" min={new Date().toISOString().split('T')[0]} className="form-control" value={assignmentForm.dueDate} onChange={e => setAssignmentForm({ ...assignmentForm, dueDate: e.target.value })} />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Target Subject *</label>
                        <select required className="form-control" value={assignmentForm.courseId}
                            onChange={e => setAssignmentForm({ ...assignmentForm, courseId: e.target.value })}>
                            <option value="">-- Select Subject --</option>
                            {courses.map(c => (
                                <option key={c.id} value={c.id}>{c.code} — {c.name || c.courseName}{c.specialization ? ` [${c.specialization}]` : ''} (Sem {c.semester})</option>
                            ))}
                        </select>
                    </div>
                </form>
            </Modal>

            <Modal
                isOpen={submitOpen}
                title={selectedAssignment ? `Submit — ${selectedAssignment.title}` : 'Submit assignment'}
                onClose={() => setSubmitOpen(false)}
                onSubmit={selectedSubmission && !selectedSubmission.isGraded ? undefined : handleSubmitWork}
                submitLabel="Turn In Assignment"
                submitting={saving}
                submitDisabled={Boolean(selectedSubmission) || !submissionText.trim()}
                isDirty={!selectedSubmission && Boolean(submissionText.trim())}
                size="large"
            >
                {selectedAssignment && (
                    <>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
                            <span className="badge badge-warning">Deadline: {new Date(selectedAssignment.dueDate).toLocaleString()}</span>
                        </div>
                        <div style={{ padding: '14px', backgroundColor: '#f7fafc', borderRadius: '10px', border: '1px solid #edf2f7', fontSize: '.85rem', color: '#4a5568', marginBottom: '14px' }}>
                            <strong>Instructions:</strong><br />{selectedAssignment.description}
                        </div>
                        {selectedSubmission && selectedSubmission.isGraded ? (
                            <div style={{ padding: '16px', backgroundColor: '#f0fff4', border: '1px solid #c6f6d5', borderRadius: '10px' }}>
                                <strong>✓ Graded: {selectedSubmission.grade}%</strong>
                                <p style={{ fontStyle: 'italic' }}>"{selectedSubmission.feedback || 'Excellent work!'}"</p>
                            </div>
                        ) : selectedSubmission ? (
                            <div className="alert" role="status">⏳ Submitted — awaiting faculty review & grading.</div>
                        ) : (
                            <form onSubmit={(e) => { e.preventDefault(); handleSubmitWork(); }}>
                                <div className="form-group">
                                    <label className="form-label">Your Work Submission *</label>
                                    <textarea
                                        rows="7"
                                        required
                                        className="form-control"
                                        placeholder="Paste your link (GitHub/Dropbox) or enter your summary here..."
                                        value={submissionText}
                                        onChange={e => setSubmissionText(e.target.value)}
                                    ></textarea>
                                </div>
                            </form>
                        )}
                    </>
                )}
            </Modal>

            <Modal
                isOpen={gradeOpen}
                title={selectedAssignment ? `Grade — ${selectedAssignment.title} (${submissions.length})` : 'Grade submissions'}
                onClose={() => { setGradeOpen(false); setSelectedSubmission(null); }}
                size="xl"
            >
                <div style={{ display: 'flex', gap: '25px', alignItems: 'flex-start', flexWrap: 'wrap' }}>
                    <div style={{ flex: 1, minWidth: '280px' }}>

                        <div className="data-table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Enrollment No.</th>
                                        <th>Date</th>
                                        <th>Plagiarism</th>
                                        <th>Status</th>
                                        <th style={{ textAlign: 'right' }}>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {submissions.length === 0 ? (
                                        <tr><td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: '#a0aec0' }}>No submissions received yet.</td></tr>
                                    ) : (
                                        submissions.map(sub => (
                                            <tr key={sub.id} style={selectedSubmission?.id === sub.id ? { backgroundColor: '#f7fafc' } : {}}>
                                                <td><strong style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#2d3748' }}>{sub.studentEnrollmentId || sub.enrollmentNumber || sub.enrollmentId || sub.studentId || 'N/A'}</strong></td>
                                                <td style={{ fontSize: '0.85rem' }}>{new Date(sub.submissionDate).toLocaleDateString()}</td>
                                                <td>
                                                    <span className={`badge ${sub.plagiarismScore > 30 ? 'badge-danger' : sub.plagiarismScore > 10 ? 'badge-warning' : 'badge-success'}`}>
                                                        {sub.plagiarismScore || 0}%
                                                    </span>
                                                </td>
                                                <td>
                                                    {sub.isGraded ? (
                                                        <span className="badge badge-success">Graded ({sub.grade}%)</span>
                                                    ) : (
                                                        <span className="badge badge-warning">Pending</span>
                                                    )}
                                                </td>
                                                <td style={{ textAlign: 'right' }}>
                                                    <button
                                                        className={`btn btn-sm ${selectedSubmission?.id === sub.id ? 'btn-secondary' : 'btn-primary'}`}
                                                        onClick={() => setSelectedSubmission(sub)}
                                                    >
                                                        {sub.isGraded ? 'Update' : 'Grade'}
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    {selectedSubmission && (
                        <div className="stat-card" style={{ flex: 0.8, position: 'sticky', top: '20px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #edf2f7', paddingBottom: '15px', marginBottom: '20px' }}>
                                <h3 style={{ margin: 0 }}>Grading: <span style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#2d3748' }}>{selectedSubmission.studentEnrollmentId || selectedSubmission.enrollmentNumber || selectedSubmission.enrollmentId || selectedSubmission.studentId || 'N/A'}</span></h3>
                                <button style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.2rem' }} onClick={() => setSelectedSubmission(null)}>✕</button>
                            </div>

                            <div style={{
                                margin: '20px 0', padding: '20px', backgroundColor: '#f8fafc',
                                borderRadius: '12px', border: '1px solid #edf2f7', fontStyle: 'italic', fontSize: '0.95rem'
                            }}>
                                <strong style={{ color: '#64748b', fontSize: '0.75rem', textTransform: 'uppercase' }}>Submission Content:</strong><br /><br />
                                "{selectedSubmission.submissionText}"
                            </div>

                            <form onSubmit={(e) => { e.preventDefault(); submitGrade(); }} style={{ marginTop: '16px' }}>
                                <div className="form-group">
                                    <label className="form-label">Assign Grade (0 - 100) *</label>
                                    <input
                                        required
                                        type="number"
                                        className="form-control"
                                        min="0" max="100"
                                        step="0.1"
                                        value={gradingForm.grade}
                                        onChange={e => setGradingForm({ ...gradingForm, grade: e.target.value })}
                                        placeholder="Enter percentage..."
                                    />
                                </div>
                                <div className="form-group">
                                    <label className="form-label">Faculty Feedback</label>
                                    <textarea
                                        rows="3"
                                        className="form-control"
                                        value={gradingForm.feedback}
                                        onChange={e => setGradingForm({ ...gradingForm, feedback: e.target.value })}
                                        placeholder="What did the student do well? What can be improved?"
                                    ></textarea>
                                </div>
                                <button type="submit" className="btn btn-success" disabled={saving} style={{ width: '100%', padding: '12px', fontWeight: 'bold' }}>
                                    {saving ? 'Saving…' : selectedSubmission.isGraded ? 'Update Grade & Notify' : 'Release Grade'}
                                </button>
                            </form>
                        </div>
                    )}
                </div>
            </Modal>
            <AiAssistModal
                isOpen={aiOpen}
                onClose={() => setAiOpen(false)}
                onInsert={handleAiInsert}
                feature="assignment"
                defaults={{ courseName: selectedCourseName }}
            />
        </div>
    );
};

export default AssignmentPage;
