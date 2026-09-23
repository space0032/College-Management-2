import SessionManager from '../utils/SessionManager';
import React, { useState, useEffect, useCallback } from 'react';
import { getStaffLeaves, getStudentLeaves, getAllPendingLeaves, createStaffLeave, createStudentLeave, updateStaffLeaveStatus, updateStudentLeaveStatus } from '../services/leaveService';
import { LEAVE_TYPES, calculateDays } from '../constants/leaveConstants';

const dayDiff = calculateDays;

const LeaveApprovalPage = () => {
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [personalLeaves, setPersonalLeaves] = useState([]);
    const [pendingLeaves, setPendingLeaves] = useState({ staff: [], students: [] });
    const [processingId, setProcessingId] = useState(null);

    const [activeTab, setActiveTab] = useState('MY_LEAVES');
    const [showModal, setShowModal] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const [formData, setFormData] = useState({ leaveType: 'SICK', startDate: '', endDate: '', reason: '' });

    const currentUser = SessionManager.getUser() || {};
    const userRole = SessionManager.getUserRole() || 'STUDENT';
    const isStudent = userRole === 'STUDENT';
    const canApprove = SessionManager.hasPermission('UPDATE_LEAVE');
    const canApply = SessionManager.hasPermission('CREATE_LEAVE');

    const fetchLeaves = useCallback(async () => {
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
        } catch (err) { setError('Failed to load leave data.'); }
        finally { setLoading(false); }
    }, [activeTab, canApprove, currentUser.id, isStudent]);

    useEffect(() => { fetchLeaves(); }, [fetchLeaves]);

    const thisYearApprovedDays = (() => {
        const year = new Date().getFullYear();
        return personalLeaves
            .filter(l => l.status === 'APPROVED')
            .reduce((sum, l) => {
                const s = String(l.startDate).slice(0, 4);
                if (String(s) !== String(year)) return sum;
                return sum + dayDiff(l.startDate, l.endDate);
            }, 0);
    })();

    const pendingCount = personalLeaves.filter(l => l.status === 'PENDING').length;

    const handleApply = async (e) => {
        e.preventDefault();
        if (submitting) return;
        if (!formData.startDate || !formData.endDate || formData.endDate < formData.startDate) {
            alert('End date must be on or after the start date.');
            return;
        }
        const today = new Date().toISOString().slice(0, 10);
        if (formData.startDate < today) {
            alert('Start date cannot be in the past.');
            return;
        }
        setSubmitting(true);
        try {
            if (isStudent) await createStudentLeave({ ...formData, studentId: currentUser.id });
            else await createStaffLeave({ ...formData, staffId: currentUser.id });
            setShowModal(false);
            setFormData({ leaveType: 'SICK', startDate: '', endDate: '', reason: '' });
            fetchLeaves();
        } catch (err) { alert('Application failed. Check dates.'); }
        finally { setSubmitting(false); }
    };

    const handleAction = async (type, id, status) => {
        if (processingId) return;
        setProcessingId(`${type}-${id}`);
        try {
            const payload = { status, approvedBy: currentUser.id };
            if (type === 'staff') await updateStaffLeaveStatus(id, payload);
            else await updateStudentLeaveStatus(id, payload);
            fetchLeaves();
        } catch (err) { alert('Action failed'); }
        finally { setProcessingId(null); }
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
                        {canApply && <button className="btn btn-sm btn-primary" onClick={() => setShowModal(true)}>+ New Application</button>}
                    </div>
                </div>
            </div>

            {error && <div className="alert alert-danger" style={{ marginBottom: '20px' }}>{error}</div>}

            {/* Premium Stats Bar */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '30px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Annual Balance</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>—</div>
                    <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Provided by administration</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Used This Year</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#1e293b', margin: '8px 0' }}>{thisYearApprovedDays} Days</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Pending Reviews</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#f59e0b', margin: '8px 0' }}>{pendingCount}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Next Upcoming Leave</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#6366f1', margin: '8px 0' }}>
                        {personalLeaves.filter(l => l.status === 'APPROVED' && String(l.startDate).slice(0, 10) >= new Date().toISOString().slice(0, 10))
                            .sort((a, b) => String(a.startDate).localeCompare(String(b.startDate)))[0]?.startDate || '—'}
                    </div>
                </div>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '60px', color: '#94a3b8' }}>Loading leave data...</div>
            ) : (
                <>
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
                                            <div style={{ fontSize: '0.85rem', color: '#64748b', margin: '4px 0' }}>{l.startDate} &mdash; {l.endDate} ({dayDiff(l.startDate, l.endDate)} day{dayDiff(l.startDate, l.endDate) === 1 ? '' : 's'})</div>
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
                                                {!pendingLeaves[tier] || pendingLeaves[tier].length === 0 ? <tr><td colSpan="5" style={{ textAlign: 'center', padding: '30px', color: '#cbd5e1' }}>Inbox is empty.</td></tr> :
                                                    pendingLeaves[tier].map(l => {
                                                        const busy = processingId === `${tier}-${l.id}`;
                                                        return (
                                                            <tr key={l.id}>
                                                                <td><strong>{tier === 'staff' ? l.staffName : (l.studentName || 'N/A')}{tier !== 'staff' && (l.enrollmentId || l.enrollmentNumber || l.username) ? <span style={{ fontSize: '0.75rem', fontFamily: 'monospace', color: '#718096', marginLeft: '6px' }}>({l.enrollmentId || l.enrollmentNumber || l.username})</span> : null}</strong></td>
                                                                <td style={{ fontSize: '0.85rem' }}>{l.startDate}<br /><span style={{ color: '#94a3b8' }}>to</span> {l.endDate}</td>
                                                                <td><span className="badge" style={{ background: LEAVE_TYPES[l.leaveType]?.bg, color: LEAVE_TYPES[l.leaveType]?.color }}>{l.leaveType}</span></td>
                                                                <td style={{ maxWidth: '300px', fontSize: '0.85rem' }}>{l.reason}</td>
                                                                <td>
                                                                    <div style={{ display: 'flex', gap: '8px' }}>
                                                                        <button className="btn btn-sm btn-success" disabled={!!processingId} onClick={() => handleAction(tier, l.id, 'APPROVED')}>{busy ? 'Working...' : 'Approve'}</button>
                                                                        <button className="btn btn-sm btn-danger" disabled={!!processingId} onClick={() => handleAction(tier, l.id, 'REJECTED')}>{busy ? 'Working...' : 'Reject'}</button>
                                                                    </div>
                                                                </td>
                                                            </tr>
                                                        );
                                                    })
                                                }
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </>
            )}

            {showModal && (
                <div className="modal-overlay" role="dialog" aria-modal="true" aria-label="Leave application">
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
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowModal(false)} disabled={submitting}>Discard</button>
                                <button type="submit" className="btn btn-primary" style={{ flex: 2, padding: '12px', fontWeight: 'bold' }} disabled={submitting}>{submitting ? 'Submitting...' : 'Submit to Authority'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default LeaveApprovalPage;