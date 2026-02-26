import SessionManager from '../utils/SessionManager';
import React, { useState, useEffect } from 'react';
import { getStaffLeaves, getStudentLeaves, getAllPendingLeaves, createStaffLeave, createStudentLeave, updateStaffLeaveStatus, updateStudentLeaveStatus } from '../services/leaveService';

const LEAVE_TYPES = {
    'SICK': { label: 'Sick Leave', icon: '🤒', color: '#ef4444', bg: '#fef2f2' },
    'CASUAL': { label: 'Casual Leave', icon: '🏖️', color: '#3b82f6', bg: '#eff6ff' },
    'EARNED': { label: 'Earned Leave', icon: '⭐', color: '#f59e0b', bg: '#fffbeb' },
    'DUTY': { label: 'On-Duty', icon: '💼', color: '#10b981', bg: '#ecfdf5' }
};

const LeaveApprovalPage = () => {
    const [, setLoading] = useState(true);
    const [, setError] = useState(null);
    const [personalLeaves, setPersonalLeaves] = useState([]);
    const [pendingLeaves, setPendingLeaves] = useState({ staff: [], students: [] });


    const [activeTab, setActiveTab] = useState('MY_LEAVES');
    const [showModal, setShowModal] = useState(false);

    const [formData, setFormData] = useState({ leaveType: 'SICK', startDate: '', endDate: '', reason: '' });

    const currentUser = SessionManager.getUser() || {};
    const userRole = SessionManager.getUserRole() || 'STUDENT';
    const isStudent = userRole === 'STUDENT';
    const canApprove = userRole === 'ADMIN' || userRole === 'FACULTY';

    const fetchLeaves = React.useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            if (activeTab === 'MY_LEAVES') {
                const res = isStudent ? await getStudentLeaves(currentUser.id) : await getStaffLeaves(currentUser.id);
                setPersonalLeaves(res.data || []);
            } else if (activeTab === 'APPROVALS' && canApprove) {
                const res = await getAllPendingLeaves();
                setPendingLeaves(res.data || { staff: [], students: [] });
            }
        } catch (err) { setError('Failed to bridge with service.'); }
        finally { setLoading(false); }
    }, [activeTab, canApprove, currentUser.id, isStudent]);

    useEffect(() => { fetchLeaves(); }, [fetchLeaves]);

    const handleApply = async (e) => {
        e.preventDefault();
        try {
            if (isStudent) await createStudentLeave({ ...formData, studentId: currentUser.id });
            else await createStaffLeave({ ...formData, staffId: currentUser.id });
            setShowModal(false);
            fetchLeaves();
        } catch (err) { alert('Application failed. Check dates.'); }
    };

    const handleAction = async (type, id, status) => {
        try {
            if (type === 'staff') await updateStaffLeaveStatus(id, status);
            else await updateStudentLeaveStatus(id, status);
            fetchLeaves();
        } catch (err) { alert('Action failed'); }
    };

    return (
        <div className="page-container" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                        <h1 className="page-title">🛡️ Leave & Absence</h1>
                        <p className="page-subtitle">Personal absence tracking and institutional approval workflows</p>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <button className={`btn btn-sm ${activeTab === 'MY_LEAVES' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('MY_LEAVES')}>My Timeline</button>
                        {canApprove && <button className={`btn btn-sm ${activeTab === 'APPROVALS' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('APPROVALS')}>Inbox</button>}
                        <button className="btn btn-sm btn-primary" onClick={() => setShowModal(true)}>+ New Application</button>
                    </div>
                </div>
            </div>

            {/* Premium Stats Bar */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '30px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Annual Balance</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>12 Days</div>
                    <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Remaining Eligibility</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Used This Year</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#1e293b', margin: '8px 0' }}>{personalLeaves.filter(l => l.status === 'APPROVED').length} Days</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Pending Reviews</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#f59e0b', margin: '8px 0' }}>{personalLeaves.filter(l => l.status === 'APPLIED').length}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Next Holiday</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#6366f1', margin: '8px 0' }}>6 Days</div>
                </div>
            </div>

            {activeTab === 'MY_LEAVES' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
                    {personalLeaves.length === 0 ? (
                        <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '100px', color: '#94a3b8' }}>No leave history found.</div>
                    ) : (
                        personalLeaves.map(l => (
                            <div key={l.id} className="stat-card" style={{ display: 'flex', gap: '15px', alignItems: 'flex-start' }}>
                                <div style={{
                                    padding: '12px', background: LEAVE_TYPES[l.leaveType]?.bg || '#f1f5f9',
                                    borderRadius: '12px', fontSize: '1.5rem', textAlign: 'center'
                                }}>
                                    {LEAVE_TYPES[l.leaveType]?.icon || '📄'}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                        <h4 style={{ margin: 0, fontSize: '1rem' }}>{LEAVE_TYPES[l.leaveType]?.label || l.leaveType}</h4>
                                        <span className={`badge ${l.status === 'APPROVED' ? 'badge-success' : l.status === 'REJECTED' ? 'badge-danger' : 'badge-warning'}`} style={{ fontSize: '0.6rem' }}>{l.status}</span>
                                    </div>
                                    <div style={{ fontSize: '0.85rem', color: '#64748b', margin: '4px 0' }}>{l.startDate} &mdash; {l.endDate}</div>
                                    <p style={{ fontSize: '0.8rem', color: '#4b5563', fontStyle: 'italic', margin: '8px 0 0 0' }}>"{l.reason}"</p>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            )}

            {activeTab === 'APPROVALS' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
                    {['staff', 'students'].map(tier => (
                        <div key={tier}>
                            <h3 style={{ marginBottom: '15px', textTransform: 'uppercase', color: '#94a3b8', fontSize: '0.8rem', letterSpacing: '1px' }}>{tier} Applications</h3>
                            <div className="data-table-container">
                                <table className="data-table">
                                    <thead><tr><th>Applicant</th><th>Duration</th><th>Type</th><th>Reason</th><th>Action</th></tr></thead>
                                    <tbody>
                                        {pendingLeaves[tier]?.length === 0 ? <tr><td colSpan="5" style={{ textAlign: 'center', padding: '30px', color: '#cbd5e1' }}>Inbox is empty.</td></tr> :
                                            pendingLeaves[tier]?.map(l => (
                                                <tr key={l.id}>
                                                    <td><strong>{tier === 'staff' ? l.staffName : l.studentName}</strong></td>
                                                    <td style={{ fontSize: '0.85rem' }}>{l.startDate}<br /><span style={{ color: '#94a3b8' }}>to</span> {l.endDate}</td>
                                                    <td><span className="badge" style={{ background: LEAVE_TYPES[l.leaveType]?.bg, color: LEAVE_TYPES[l.leaveType]?.color }}>{l.leaveType}</span></td>
                                                    <td style={{ maxWidth: '300px', fontSize: '0.85rem' }}>{l.reason}</td>
                                                    <td>
                                                        <div style={{ display: 'flex', gap: '8px' }}>
                                                            <button className="btn btn-sm btn-success" onClick={() => handleAction(tier, l.id, 'APPROVED')}>Approve</button>
                                                            <button className="btn btn-sm btn-danger" onClick={() => handleAction(tier, l.id, 'REJECTED')}>Reject</button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))
                                        }
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '450px', borderRadius: '20px', padding: '30px' }}>
                        <h2 style={{ marginBottom: '25px' }}>Leave Application</h2>
                        <form onSubmit={handleApply} className="form-grid">
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Leave Classification *</label>
                                <select className="form-control" name="leaveType" value={formData.leaveType} onChange={e => setFormData({ ...formData, leaveType: e.target.value })}>
                                    {Object.entries(LEAVE_TYPES).map(([k, v]) => (
                                        <option key={k} value={k}>{v.icon} {v.label}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Start Date *</label>
                                <input required type="date" className="form-control" name="startDate" value={formData.startDate} onChange={e => setFormData({ ...formData, startDate: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>End Date *</label>
                                <input required type="date" className="form-control" name="endDate" value={formData.endDate} onChange={e => setFormData({ ...formData, endDate: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Statement of Reason *</label>
                                <textarea required rows="4" className="form-control" name="reason" value={formData.reason} onChange={e => setFormData({ ...formData, reason: e.target.value })} placeholder="Specifically explain the necessity..."></textarea>
                            </div>
                            <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '12px', marginTop: '10px' }}>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowModal(false)}>Discard</button>
                                <button type="submit" className="btn btn-primary" style={{ flex: 2, padding: '12px', fontWeight: 'bold' }}>Submit to Authority</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LeaveApprovalPage;
