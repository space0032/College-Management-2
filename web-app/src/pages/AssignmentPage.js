import React, { useState, useEffect } from 'react';
import {
    getAssignments, createAssignment, getSubmissions,
    getStudentSubmission, submitAssignment, gradeSubmission
} from '../services/assignmentService';
import { getAllCourses } from '../services/courseService';

const AssignmentPage = () => {
    const [activeTab, setActiveTab] = useState('browse'); // browse, create, grade, submit
    const [assignments, setAssignments] = useState([]);
    const [submissions, setSubmissions] = useState([]);
    const [selectedAssignment, setSelectedAssignment] = useState(null);

    // Forms
    const [assignmentForm, setAssignmentForm] = useState({ title: '', description: '', dueDate: '', courseId: '', semester: 1 });
    const [submissionText, setSubmissionText] = useState('');
    const [gradingForm, setGradingForm] = useState({ grade: '', feedback: '' });
    const [selectedSubmission, setSelectedSubmission] = useState(null);
    const [courses, setCourses] = useState([]);

    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : { id: null, role: 'STUDENT' };
    const userId = user.id;
    const userRole = user.role;

    useEffect(() => {
        loadAssignments();
        if (userRole === 'FACULTY' || userRole === 'ADMIN') {
            getAllCourses().then(res => setCourses(res.data || [])).catch(() => { });
        }
        // eslint-disable-next-line
    }, [activeTab]);

    const loadAssignments = async () => {
        try {
            const res = await getAssignments(userRole, userId);
            setAssignments(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const handleCreateAssignment = async (e) => {
        e.preventDefault();
        if (!assignmentForm.courseId) { alert('Please select a course.'); return; }
        const today = new Date().toISOString().split('T')[0];
        if (assignmentForm.dueDate && assignmentForm.dueDate < today) { alert('Due date cannot be in the past.'); return; }
        try {
            await createAssignment({
                ...assignmentForm,
                courseId: parseInt(assignmentForm.courseId),
                semester: parseInt(assignmentForm.semester),
                createdBy: userId
            });
            setAssignmentForm({ title: '', description: '', dueDate: '', courseId: '1', semester: 1 });
            setActiveTab('browse');
        } catch (err) {
            alert('Failed to create assignment');
        }
    };

    const handleOpenSubmit = async (a) => {
        setSelectedAssignment(a);
        try {
            const res = await getStudentSubmission(a.id, userId);
            if (res.data) {
                setSubmissionText(res.data.submissionText);
                setSelectedSubmission(res.data); // Already submitted
            } else {
                setSubmissionText('');
                setSelectedSubmission(null);
            }
            setActiveTab('submit');
        } catch (err) {
            alert('Failed to load submission state');
        }
    };

    const handleSubmitWork = async (e) => {
        e.preventDefault();
        try {
            await submitAssignment(selectedAssignment.id, {
                studentId: userId,
                submissionText: submissionText
            });
            alert('Assignment submitted successfully');
            setActiveTab('browse');
        } catch (err) {
            alert('Failed to submit assignment');
        }
    };

    const handleOpenGrade = async (a) => {
        setSelectedAssignment(a);
        try {
            const res = await getSubmissions(a.id);
            setSubmissions(res.data || []);
            setActiveTab('grade');
        } catch (err) {
            alert('Failed to fetch submissions');
        }
    };

    const submitGrade = async (e) => {
        e.preventDefault();
        if (!selectedSubmission) return;
        try {
            await gradeSubmission(selectedSubmission.id, {
                grade: parseFloat(gradingForm.grade),
                feedback: gradingForm.feedback
            });
            setGradingForm({ grade: '', feedback: '' });
            setSelectedSubmission(null);
            const res = await getSubmissions(selectedAssignment.id);
            setSubmissions(res.data || []);
        } catch (err) {
            alert('Failed to save grade');
        }
    };

    // Stats
    const totalAssignments = assignments.length;
    const dueSoon = assignments.filter(a => {
        const diff = new Date(a.dueDate).getTime() - Date.now();
        return diff > 0 && diff < 86400000 * 3; // 3 days
    }).length;

    // Simulating pending grades for faculty
    const pendingGrades = userRole !== 'STUDENT' ? assignments.length * 2 : 0;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">📝 Course Assignments</h1>
                    <p className="page-subtitle">Track, submit, and grade academic assignments across your courses</p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('browse')}>
                        All Assignments
                    </button>
                    {(userRole === 'FACULTY' || userRole === 'ADMIN') && (
                        <button className={`btn ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('create')}>
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
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3182ce' }}>{assignments.length - 2}</div>
                    </div>
                ) : (
                    <div className="stat-card">
                        <div style={{ fontSize: '0.9rem', color: '#666' }}>Pending Grading</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3182ce' }}>{pendingGrades}</div>
                    </div>
                )}
                <div className="stat-card">
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>Average Score</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#48bb78' }}>82%</div>
                </div>
            </div>

            {activeTab === 'browse' && (
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
                                                {a.courseName || `Course ID ${a.courseId}`}
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

            {activeTab === 'create' && (userRole === 'FACULTY' || userRole === 'ADMIN') && (
                <div style={{ maxWidth: '800px', margin: '0 auto' }}>
                    <div className="stat-card" style={{ padding: '30px' }}>
                        <h3 style={{ borderBottom: '1px solid #edf2f7', paddingBottom: '15px', marginBottom: '25px' }}>🚀 Publish New Assignment</h3>
                        <form className="form-grid" onSubmit={handleCreateAssignment}>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Assignment Title *</label>
                                <input required type="text" className="form-control" value={assignmentForm.title} onChange={e => setAssignmentForm({ ...assignmentForm, title: e.target.value })} placeholder="e.g. Mid-term Project Phase 1" />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Description & Learning Objectives *</label>
                                <textarea required rows="6" className="form-control" value={assignmentForm.description} onChange={e => setAssignmentForm({ ...assignmentForm, description: e.target.value })} placeholder="Detail the requirements, constraints, and submission format..."></textarea>
                            </div>
                            <div className="form-group">
                                <label>Submission Deadline *</label>
                                <input required type="date" className="form-control" value={assignmentForm.dueDate} onChange={e => setAssignmentForm({ ...assignmentForm, dueDate: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Target Course *</label>
                                <select required className="form-control" value={assignmentForm.courseId}
                                    onChange={e => setAssignmentForm({ ...assignmentForm, courseId: e.target.value })}>
                                    <option value="">-- Select Course --</option>
                                    {courses.map(c => (
                                        <option key={c.id} value={c.id}>{c.name || c.courseName} (ID: {c.id})</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '10px' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '12px', fontWeight: 'bold', fontSize: '1rem' }}>Launch Assignment</button>
                                <button type="button" className="btn btn-secondary" style={{ width: '100%', marginTop: '10px' }} onClick={() => setActiveTab('browse')}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {activeTab === 'submit' && selectedAssignment && (
                <div style={{ maxWidth: '900px', margin: '0 auto' }}>
                    <div className="stat-card" style={{ padding: '30px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <button className="btn btn-secondary btn-sm" onClick={() => setActiveTab('browse')}>&larr; Back to List</button>
                            <span className="badge badge-warning">Deadline: {new Date(selectedAssignment.dueDate).toLocaleString()}</span>
                        </div>

                        <h2 style={{ fontSize: '1.8rem', color: '#2d3748', margin: '0 0 10px 0' }}>{selectedAssignment.title}</h2>

                        <div style={{
                            marginTop: '20px', padding: '20px', backgroundColor: '#f7fafc',
                            borderRadius: '12px', border: '1px solid #edf2f7', lineHeight: '1.6', color: '#4a5568'
                        }}>
                            <h4 style={{ marginTop: 0, color: '#2d3748' }}>Instructions:</h4>
                            {selectedAssignment.description}
                        </div>

                        <div style={{ margin: '30px 0', borderTop: '2px dashed #edf2f7' }} />

                        {selectedSubmission && selectedSubmission.isGraded ? (
                            <div style={{ padding: '25px', backgroundColor: '#f0fff4', border: '1px solid #c6f6d5', borderRadius: '12px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                                    <h3 style={{ color: '#2f855a', margin: 0 }}>✓ Assignment Graded</h3>
                                    <div style={{
                                        fontSize: '2rem', fontWeight: 'bold', color: '#2f855a',
                                        background: 'white', padding: '10px 20px', borderRadius: '12px', border: '2px solid #c6f6d5'
                                    }}>
                                        {selectedSubmission.grade}%
                                    </div>
                                </div>
                                <div style={{ marginTop: '15px' }}>
                                    <strong style={{ color: '#2d3748' }}>Feedback:</strong>
                                    <p style={{ marginTop: '8px', fontStyle: 'italic' }}>"{selectedSubmission.feedback || 'Excellent work! No specific feedback provided.'}"</p>
                                </div>
                                <div style={{ marginTop: '20px', padding: '15px', background: 'rgba(255,255,255,0.5)', borderRadius: '8px', fontSize: '0.9rem' }}>
                                    <strong>Your Final Submission:</strong><br />
                                    {selectedSubmission.submissionText}
                                </div>
                            </div>
                        ) : (
                            <form onSubmit={handleSubmitWork}>
                                <div className="form-group">
                                    <label style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 'bold' }}>
                                        <span>Your Work Submission *</span>
                                        {selectedSubmission && <span style={{ color: '#48bb78' }}>✓ Submitted on {new Date(selectedSubmission.submissionDate).toLocaleDateString()}</span>}
                                    </label>
                                    <textarea
                                        rows="10"
                                        required
                                        className="form-control"
                                        readOnly={!!selectedSubmission}
                                        placeholder="Paste your link (GitHub/Dropbox) or enter your summary here..."
                                        value={submissionText}
                                        onChange={e => setSubmissionText(e.target.value)}
                                        style={{ fontSize: '1rem', padding: '15px' }}
                                    ></textarea>
                                </div>

                                {!selectedSubmission ? (
                                    <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '15px', fontWeight: 'bold', fontSize: '1.1rem' }}>
                                        🚀 Turn In Assignment
                                    </button>
                                ) : (
                                    <div style={{
                                        textAlign: 'center', padding: '20px', borderRadius: '12px',
                                        background: '#ebf8ff', color: '#2b6cb0', border: '1px solid #bee3f8',
                                        fontWeight: 'bold'
                                    }}>
                                        ⏳ Awaiting Faculty Review & Grading
                                    </div>
                                )}
                            </form>
                        )}
                    </div>
                </div>
            )}

            {activeTab === 'grade' && selectedAssignment && (
                <div style={{ display: 'flex', gap: '25px', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                        <div className="stat-card" style={{ marginBottom: '20px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <button className="btn btn-secondary btn-sm" onClick={() => setActiveTab('browse')}>&larr; Back to Dashboard</button>
                                <div className="badge badge-primary">{submissions.length} Submissions</div>
                            </div>
                            <h3 style={{ marginTop: '15px', marginBottom: 0 }}>{selectedAssignment.title}</h3>
                        </div>

                        <div className="data-table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Student ID</th>
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
                                                <td><strong>Student #{sub.studentId}</strong></td>
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
                                <h3 style={{ margin: 0 }}>Grading: Student #{selectedSubmission.studentId}</h3>
                                <button style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.2rem' }} onClick={() => setSelectedSubmission(null)}>✕</button>
                            </div>

                            <div style={{
                                margin: '20px 0', padding: '20px', backgroundColor: '#f8fafc',
                                borderRadius: '12px', border: '1px solid #edf2f7', fontStyle: 'italic', fontSize: '0.95rem'
                            }}>
                                <strong style={{ color: '#64748b', fontSize: '0.75rem', textTransform: 'uppercase' }}>Submission Content:</strong><br /><br />
                                "{selectedSubmission.submissionText}"
                            </div>

                            <form onSubmit={submitGrade} style={{ marginTop: '25px' }}>
                                <div className="form-group">
                                    <label style={{ fontWeight: 'bold' }}>Assign Grade (0 - 100) *</label>
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
                                    <label style={{ fontWeight: 'bold' }}>Faculty Feedback</label>
                                    <textarea
                                        rows="4"
                                        className="form-control"
                                        value={gradingForm.feedback}
                                        onChange={e => setGradingForm({ ...gradingForm, feedback: e.target.value })}
                                        placeholder="What did the student do well? What can be improved?"
                                    ></textarea>
                                </div>
                                <button type="submit" className="btn btn-success" style={{ width: '100%', padding: '12px', fontWeight: 'bold' }}>
                                    {selectedSubmission.isGraded ? 'Update Grade & Notify' : 'Release Grade'}
                                </button>
                            </form>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default AssignmentPage;
