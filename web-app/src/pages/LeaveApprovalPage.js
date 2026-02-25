import React, { useState, useEffect } from 'react';
import { getStaffLeaves, getStudentLeaves, getAllPendingLeaves, createStaffLeave, createStudentLeave, updateStaffLeaveStatus, updateStudentLeaveStatus } from '../services/leaveService';

const LeaveApprovalPage = () => {
    const [personalLeaves, setPersonalLeaves] = useState([]);
    const [pendingLeaves, setPendingLeaves] = useState({ staff: [], students: [] });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeTab, setActiveTab] = useState('MY_LEAVES'); // 'MY_LEAVES', 'APPROVALS'
    const [showModal, setShowModal] = useState(false);

    const [formData, setFormData] = useState({
        leaveType: 'SICK',
        startDate: '',
        endDate: '',
        reason: ''
    });

    const currentUser = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();

    const isStudent = currentUser.role === 'STUDENT';
    const canApprove = currentUser.role === 'ADMIN' || currentUser.role === 'FACULTY';

    useEffect(() => {
        fetchLeaves();
    }, [activeTab]);

    const fetchLeaves = async () => {
        setLoading(true);
        setError(null);
        try {
            if (activeTab === 'MY_LEAVES') {
                if (isStudent) {
                    // Requires mapping studentId. Assuming we have studentId in user or we fetch it.
                    // For MVP, if it's a student, we assume their ID maps 1:1 or they have a student profile.
                    // The real app would fetch the profile. We use user.id for now as index.
                    const res = await getStudentLeaves(currentUser.id);
                    setPersonalLeaves(res.data || []);
                } else {
                    const res = await getStaffLeaves(currentUser.id);
                    setPersonalLeaves(res.data || []);
                }
            } else if (activeTab === 'APPROVALS' && canApprove) {
                const res = await getAllPendingLeaves();
                setPendingLeaves(res.data || { staff: [], students: [] });
            }
        } catch (err) {
            console.error(err);
            setError('Failed to fetch leave records.');
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmitRequest = async (e) => {
        e.preventDefault();
        try {
            if (isStudent) {
                await createStudentLeave({ ...formData, studentId: currentUser.id });
            } else {
                await createStaffLeave({ ...formData, userId: currentUser.id });
            }
            setShowModal(false);
            fetchLeaves();
        } catch (err) {
            alert('Failed to submit leave request.');
        }
    };

    const handleApproval = async (type, id, status) => {
        try {
            if (type === 'STAFF') {
                const comments = prompt("Optional comments for " + status + " status:") || "";
                await updateStaffLeaveStatus(id, { status, approvedBy: currentUser.id, comments });
            } else {
                await updateStudentLeaveStatus(id, { status, approvedBy: currentUser.id });
            }
            fetchLeaves();
        } catch (err) {
            alert('Failed to update leave status.');
        }
    };

    const getStatusBadge = (status) => {
        switch (status) {
            case 'APPROVED': return <span className="status-badge status-active">APPROVED</span>;
            case 'REJECTED': return <span className="status-badge status-rejected">REJECTED</span>;
            default: return <span className="status-badge status-pending">PENDING</span>;
        }
    };

    if (loading && !showModal) return <div className="page-container">Loading...</div>;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>📅 Leave Management</h2>
                    <p className="text-muted">Apply for leaves and manage staff/student approvals.</p>
                </div>
                <button className="btn btn-primary" onClick={() => {
                    setFormData({ leaveType: 'SICK', startDate: '', endDate: '', reason: '' });
                    setShowModal(true);
                }}>
                    + Request Leave
                </button>
            </div>

            <div className="tabs" style={{ marginBottom: '20px' }}>
                <button className={`tab ${activeTab === 'MY_LEAVES' ? 'active' : ''}`} onClick={() => setActiveTab('MY_LEAVES')}>My Leaves</button>
                {canApprove && (
                    <button className={`tab ${activeTab === 'APPROVALS' ? 'active' : ''}`} onClick={() => setActiveTab('APPROVALS')}>Pending Approvals</button>
                )}
            </div>

            {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

            {activeTab === 'MY_LEAVES' ? (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Type</th>
                                <th>Start Date</th>
                                <th>End Date</th>
                                <th>Reason</th>
                                <th>Comments</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {personalLeaves.length === 0 ? (
                                <tr><td colSpan="6" style={{ textAlign: 'center' }}>No leave history found.</td></tr>
                            ) : (
                                personalLeaves.map(leave => (
                                    <tr key={leave.id}>
                                        <td style={{ fontWeight: '500' }}>{leave.leaveType}</td>
                                        <td>{leave.startDate}</td>
                                        <td>{leave.endDate}</td>
                                        <td>{leave.reason}</td>
                                        <td className="text-muted">{leave.comments || '-'}</td>
                                        <td>{getStatusBadge(leave.status)}</td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div>
                    {currentUser.role === 'ADMIN' && (
                        <>
                            <h3>Staff Leave Requests</h3>
                            <div className="data-table-container" style={{ marginBottom: '30px' }}>
                                <table className="data-table">
                                    <thead>
                                        <tr>
                                            <th>Staff Member</th>
                                            <th>Type</th>
                                            <th>Duration</th>
                                            <th>Reason</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {pendingLeaves.staff.length === 0 ? <tr><td colSpan="5">No pending staff leaves.</td></tr> : pendingLeaves.staff.map(leave => (
                                            <tr key={leave.id}>
                                                <td style={{ fontWeight: '500' }}>{leave.staffName}</td>
                                                <td>{leave.leaveType}</td>
                                                <td>{leave.startDate} to {leave.endDate}</td>
                                                <td>{leave.reason}</td>
                                                <td>
                                                    <div style={{ display: 'flex', gap: '8px' }}>
                                                        <button className="btn btn-primary" onClick={() => handleApproval('STAFF', leave.id, 'APPROVED')} style={{ backgroundColor: '#4caf50', padding: '5px 10px' }}>Approve</button>
                                                        <button className="btn btn-danger" onClick={() => handleApproval('STAFF', leave.id, 'REJECTED')} style={{ padding: '5px 10px' }}>Reject</button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </>
                    )}

                    <h3>Student Leave Requests</h3>
                    <div className="data-table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Student</th>
                                    <th>Type</th>
                                    <th>Duration</th>
                                    <th>Reason</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {pendingLeaves.students.length === 0 ? <tr><td colSpan="5">No pending student leaves.</td></tr> : pendingLeaves.students.map(leave => (
                                    <tr key={leave.id}>
                                        <td style={{ fontWeight: '500' }}>{leave.studentName}</td>
                                        <td>{leave.leaveType}</td>
                                        <td>{leave.startDate} to {leave.endDate}</td>
                                        <td>{leave.reason}</td>
                                        <td>
                                            <div style={{ display: 'flex', gap: '8px' }}>
                                                <button className="btn btn-primary" onClick={() => handleApproval('STUDENT', leave.id, 'APPROVED')} style={{ backgroundColor: '#4caf50', padding: '5px 10px' }}>Approve</button>
                                                <button className="btn btn-danger" onClick={() => handleApproval('STUDENT', leave.id, 'REJECTED')} style={{ padding: '5px 10px' }}>Reject</button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '500px' }}>
                        <div className="modal-header">
                            <h2>Request Leave</h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
                        </div>
                        <form onSubmit={handleSubmitRequest} className="form-grid">
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Leave Type</label>
                                <select required name="leaveType" value={formData.leaveType} onChange={handleInputChange}>
                                    <option value="SICK">Sick / Medical Leave</option>
                                    <option value="CASUAL">Casual Leave</option>
                                    <option value="EMERGENCY">Emergency Leave</option>
                                    {!isStudent && <option value="VACATION">Vacation (Annual Leave)</option>}
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Start Date</label>
                                <input required type="date" name="startDate" value={formData.startDate} onChange={handleInputChange} min={new Date().toISOString().split('T')[0]} />
                            </div>

                            <div className="form-group">
                                <label>End Date</label>
                                <input required type="date" name="endDate" value={formData.endDate} onChange={handleInputChange} min={formData.startDate || new Date().toISOString().split('T')[0]} />
                            </div>

                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Detailed Reason</label>
                                <textarea required name="reason" value={formData.reason} onChange={handleInputChange} rows="3" placeholder="Provide reason for absence..."></textarea>
                            </div>

                            <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '10px' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Submit Request</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LeaveApprovalPage;
