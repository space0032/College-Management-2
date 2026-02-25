import React, { useState, useEffect } from 'react';
import {
    getAssignments, createAssignment, getSubmissions,
    getStudentSubmission, submitAssignment, gradeSubmission
} from '../services/assignmentService';

const AssignmentPage = () => {
    const [activeTab, setActiveTab] = useState('browse'); // browse, create, grade, submit
    const [assignments, setAssignments] = useState([]);
    const [submissions, setSubmissions] = useState([]);
    const [selectedAssignment, setSelectedAssignment] = useState(null);

    // Forms
    const [assignmentForm, setAssignmentForm] = useState({ title: '', description: '', dueDate: '', courseId: '1', semester: 1 });
    const [submissionText, setSubmissionText] = useState('');
    const [gradingForm, setGradingForm] = useState({ grade: '', feedback: '' });
    const [selectedSubmission, setSelectedSubmission] = useState(null);

    // Simulating logged-in user
    const userId = parseInt(localStorage.getItem('userId') || '1');
    const userRole = localStorage.getItem('userRole') || 'STUDENT'; // STUDENT, FACULTY, ADMIN

    useEffect(() => {
        loadAssignments();
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
            // Refresh submissions
            const res = await getSubmissions(selectedAssignment.id);
            setSubmissions(res.data || []);
        } catch (err) {
            alert('Failed to save grade');
        }
    };

    return (
        <div className="page-container">
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between' }}>
                <h2>Course Assignments</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('browse')}>
                        All Assignments
                    </button>

                    {(userRole === 'FACULTY' || userRole === 'ADMIN') && (
                        <button className={`btn ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('create')}>
                            Create Assignment
                        </button>
                    )}

                    {activeTab === 'grade' && (
                        <button className="btn btn-primary">Grading: {selectedAssignment?.title}</button>
                    )}
                    {activeTab === 'submit' && (
                        <button className="btn btn-primary">Submit: {selectedAssignment?.title}</button>
                    )}
                </div>
            </div>

            {activeTab === 'browse' && (
                <div className="card-grid">
                    {assignments.length === 0 ? <p>No assignments found.</p> : assignments.map(a => (
                        <div key={a.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <h3 style={{ margin: 0, color: 'var(--primary-color)' }}>{a.title}</h3>
                                <span className="badge badge-warning">Due: {new Date(a.dueDate).toLocaleDateString()}</span>
                            </div>
                            <p style={{ margin: '15px 0', color: 'var(--text-muted)', flex: 1 }}>{a.description}</p>
                            <div style={{ fontSize: '0.9rem', color: '#666', borderTop: '1px solid #eee', paddingTop: '10px' }}>
                                Course: {a.courseName || `Course ID ${a.courseId}`} <br />
                                Faculty: {a.facultyName || `User ID ${a.createdBy}`}
                            </div>

                            <div style={{ marginTop: '15px', display: 'flex', gap: '10px' }}>
                                {userRole === 'STUDENT' && (
                                    <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => handleOpenSubmit(a)}>View & Submit</button>
                                )}
                                {(userRole === 'FACULTY' || userRole === 'ADMIN') && (
                                    <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => handleOpenGrade(a)}>View Submissions</button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {activeTab === 'create' && (userRole === 'FACULTY' || userRole === 'ADMIN') && (
                <div className="form-container" style={{ maxWidth: '600px', margin: '0 auto' }}>
                    <div className="stat-card">
                        <h3>Publish New Assignment</h3>
                        <form className="form-grid" onSubmit={handleCreateAssignment} style={{ marginTop: '20px' }}>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Assignment Title *</label>
                                <input required type="text" value={assignmentForm.title} onChange={e => setAssignmentForm({ ...assignmentForm, title: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Description & Instructions *</label>
                                <textarea required rows="4" value={assignmentForm.description} onChange={e => setAssignmentForm({ ...assignmentForm, description: e.target.value })}></textarea>
                            </div>
                            <div className="form-group">
                                <label>Due Date *</label>
                                <input required type="date" value={assignmentForm.dueDate} onChange={e => setAssignmentForm({ ...assignmentForm, dueDate: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Course ID *</label>
                                <input required type="number" min="1" value={assignmentForm.courseId} onChange={e => setAssignmentForm({ ...assignmentForm, courseId: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Create Assignment</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {activeTab === 'submit' && selectedAssignment && (
                <div className="form-container" style={{ maxWidth: '800px', margin: '0 auto' }}>
                    <div className="stat-card">
                        <button className="btn btn-secondary btn-sm" onClick={() => setActiveTab('browse')} style={{ marginBottom: '15px' }}>&larr; Back to Assigments</button>
                        <h2>{selectedAssignment.title}</h2>
                        <p className="badge badge-warning">Due: {new Date(selectedAssignment.dueDate).toLocaleString()}</p>
                        <div style={{ marginTop: '15px', padding: '15px', backgroundColor: 'var(--secondary-bg)', borderRadius: 'var(--border-radius)' }}>
                            {selectedAssignment.description}
                        </div>

                        <hr style={{ margin: '20px 0', borderColor: 'var(--border-color)' }} />

                        {selectedSubmission && selectedSubmission.isGraded ? (
                            <div style={{ padding: '20px', backgroundColor: '#e8f5e9', border: '1px solid #c8e6c9', borderRadius: '8px' }}>
                                <h3 style={{ color: '#2e7d32', margin: '0 0 10px 0' }}>Graded (Score: {selectedSubmission.grade}%)</h3>
                                <p><strong>Feedback:</strong> {selectedSubmission.feedback || 'No feedback provided.'}</p>
                                <p style={{ marginTop: '10px' }}><strong>Your Submission:</strong> {selectedSubmission.submissionText}</p>
                            </div>
                        ) : (
                            <form onSubmit={handleSubmitWork}>
                                <div className="form-group">
                                    <label>Your Submission (Text or Link) *</label>
                                    <textarea
                                        rows="6"
                                        required
                                        readOnly={!!selectedSubmission}
                                        placeholder="Enter your answers or paste a link to your project repository..."
                                        value={submissionText}
                                        onChange={e => setSubmissionText(e.target.value)}
                                    ></textarea>
                                </div>
                                {selectedSubmission ? (
                                    <div className="badge badge-success" style={{ padding: '10px', fontSize: '1rem', width: '100%', textAlign: 'center', display: 'block' }}>
                                        Successfully Submitted on {new Date(selectedSubmission.submissionDate).toLocaleString()} (Awaiting Grade)
                                    </div>
                                ) : (
                                    <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '12px' }}>Turn In Assignment</button>
                                )}
                            </form>
                        )}
                    </div>
                </div>
            )}

            {activeTab === 'grade' && selectedAssignment && (
                <div style={{ display: 'flex', gap: '20px' }}>
                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '20px' }}>
                        <div className="stat-card">
                            <button className="btn btn-secondary btn-sm" onClick={() => setActiveTab('browse')} style={{ marginBottom: '15px' }}>&larr; Back</button>
                            <h3>Submissions for: {selectedAssignment.title}</h3>
                        </div>

                        <div className="data-table-container">
                            <table className="data-table">
                                <thead><tr><th>Student ID</th><th>Submitted At</th><th>Plagiarism</th><th>Status</th><th>Action</th></tr></thead>
                                <tbody>
                                    {submissions.length === 0 ? <tr><td colSpan="5" style={{ textAlign: 'center' }}>No submissions yet</td></tr> :
                                        submissions.map(sub => (
                                            <tr key={sub.id}>
                                                <td>{sub.studentId}</td>
                                                <td>{new Date(sub.submissionDate).toLocaleDateString()}</td>
                                                <td>
                                                    {sub.plagiarismScore > 0 ? (
                                                        <span className={`badge ${sub.plagiarismScore > 30 ? 'badge-danger' : 'badge-warning'}`}>{sub.plagiarismScore}%</span>
                                                    ) : '0%'}
                                                </td>
                                                <td>{sub.isGraded ? <span className="badge badge-success">Graded ({sub.grade})</span> : <span className="badge badge-warning">Pending</span>}</td>
                                                <td>
                                                    <button className="btn btn-primary btn-sm" onClick={() => setSelectedSubmission(sub)}>View & Grade</button>
                                                </td>
                                            </tr>
                                        ))
                                    }
                                </tbody>
                            </table>
                        </div>
                    </div>

                    {selectedSubmission && (
                        <div className="stat-card" style={{ flex: 1, height: 'fit-content' }}>
                            <h3 style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '10px' }}>Grading Panel (Student ID: {selectedSubmission.studentId})</h3>

                            <div style={{ margin: '20px 0', padding: '15px', backgroundColor: 'var(--secondary-bg)', borderRadius: 'var(--border-radius)', minHeight: '100px', whiteSpace: 'pre-wrap' }}>
                                <strong>Submission Text:</strong><br /><br />
                                {selectedSubmission.submissionText}
                            </div>

                            {selectedSubmission.isGraded && (
                                <div style={{ marginBottom: '15px', padding: '10px', backgroundColor: '#e3f2fd', border: '1px solid #bbdefb', borderRadius: '4px' }}>
                                    <strong>Current Grade:</strong> {selectedSubmission.grade}%<br />
                                    <strong>Feedback:</strong> {selectedSubmission.feedback || 'None'}
                                </div>
                            )}

                            <form onSubmit={submitGrade}>
                                <div className="form-group">
                                    <label>Assign Score (0-100) *</label>
                                    <input required type="number" min="0" max="100" step="0.1" value={gradingForm.grade} onChange={e => setGradingForm({ ...gradingForm, grade: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Feedback for Student</label>
                                    <textarea rows="3" value={gradingForm.feedback} onChange={e => setGradingForm({ ...gradingForm, feedback: e.target.value })}></textarea>
                                </div>
                                <button type="submit" className="btn btn-success" style={{ width: '100%' }}>{selectedSubmission.isGraded ? 'Update Grade' : 'Save Grade'}</button>
                            </form>
                        </div>
                    )}
                </div>
            )}

        </div>
    );
};

export default AssignmentPage;
