import React, { useState, useEffect } from 'react';
import {
    getStudentGatePasses, getPendingGatePasses, getAllGatePasses,
    requestGatePass, approveGatePass, rejectGatePass
} from '../services/gatePassService';
import SessionManager from '../utils/SessionManager';

const GatePassPage = () => {
    const [activeTab, setActiveTab] = useState('my_passes');
    const [passes, setPasses] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Form State
    const [formData, setFormData] = useState({
        fromDate: '',
        toDate: '',
        reason: '',
        destination: '',
        parentContact: ''
    });

    // Admin Action State
    const [actionComment, setActionComment] = useState('');

    const user = SessionManager.getUser() || {};

    useEffect(() => {
        if (user.role === 'STUDENT' && activeTab !== 'my_passes' && activeTab !== 'request') {
            setActiveTab('my_passes');
        } else if (user.role !== 'STUDENT' && activeTab === 'my_passes') {
            setActiveTab('pending');
        }
    }, [user.role, activeTab]);

    useEffect(() => {
        if (activeTab === 'my_passes' && user.role === 'STUDENT') loadStudentPasses();
        if (activeTab === 'pending' && user.role !== 'STUDENT') loadPendingPasses();
        if (activeTab === 'history' && user.role !== 'STUDENT') loadAllPasses();
        // eslint-disable-next-line
    }, [activeTab]);

    const loadStudentPasses = async () => {
        try {
            const res = await getStudentGatePasses(user.id);
            setPasses(res.data || []);
        } catch (err) { console.error(err); }
    };

    const loadPendingPasses = async () => {
        try {
            const res = await getPendingGatePasses();
            setPasses(res.data || []);
        } catch (err) { console.error(err); }
    };

    const loadAllPasses = async () => {
        try {
            const res = await getAllGatePasses();
            setPasses(res.data || []);
        } catch (err) { console.error(err); }
    };

    const handleRequestPass = async (e) => {
        e.preventDefault();
        // Date validation — no past dates
        const today = new Date().toISOString().split('T')[0];
        if (formData.fromDate < today) { alert('From date cannot be in the past.'); return; }
        if (formData.toDate && formData.toDate < formData.fromDate) { alert('Return date must be after departure date.'); return; }
        setIsSubmitting(true);
        try {
            await requestGatePass({ ...formData, studentId: user.id });
            alert('Gate pass request broadcasted to warden.');
            setFormData({ fromDate: '', toDate: '', reason: '', destination: '', parentContact: '' });
            setActiveTab('my_passes');
        } catch (err) {
            alert(err.response?.data?.error || 'Hostel assignment required for gate pass.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleApprove = async (passId) => {
        try {
            await approveGatePass(passId, user.id, actionComment || 'Approved by Warden');
            setActionComment('');
            loadPendingPasses();
        } catch (err) { alert('Approval failed'); }
    };

    const handleReject = async (passId) => {
        try {
            await rejectGatePass(passId, user.id, actionComment || 'Rejected (Check remarks)');
            setActionComment('');
            loadPendingPasses();
        } catch (err) { alert('Rejection failed'); }
    };

    // Stats
    const today = new Date().toISOString().split('T')[0];
    const pendingCount = user.role !== 'STUDENT' ? passes.filter(p => p.status === 'PENDING').length : 0;
    const approvedToday = passes.filter(p => p.status === 'APPROVED' && (p.approvedAt || p.requestDate || '').startsWith(today)).length;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">🚪 Citadel Gate Pass</h1>
                    <p className="page-subtitle">Digital routing for campus entry/exit and overnight stays</p>
                </div>
                <div style={{ display: 'flex', gap: '8px', background: '#f8fafc', padding: '4px', borderRadius: '8px' }}>
                    {user.role === 'STUDENT' ? (
                        <>
                            <button className={`btn btn-sm ${activeTab === 'my_passes' ? 'btn-primary' : ''}`} style={activeTab !== 'my_passes' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('my_passes')}>Existing Passes</button>
                            <button className={`btn btn-sm ${activeTab === 'request' ? 'btn-primary' : ''}`} style={activeTab !== 'request' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('request')}>+ New Request</button>
                        </>
                    ) : (
                        <>
                            <button className={`btn btn-sm ${activeTab === 'pending' ? 'btn-primary' : ''}`} style={activeTab !== 'pending' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('pending')}>Pending Approval</button>
                            <button className={`btn btn-sm ${activeTab === 'history' ? 'btn-primary' : ''}`} style={activeTab !== 'history' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('history')}>Audit Logs</button>
                        </>
                    )}
                </div>
            </div>

            {/* Stats Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '25px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>System Status</div>
                    <div style={{ fontSize: '1.4rem', fontWeight: 'bold', margin: '8px 0' }}>ACTIVE</div>
                    <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Secure Digital Logging</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Pending Review</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#f59e0b', margin: '8px 0' }}>{pendingCount || 0}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Approved Today</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#10b981', margin: '8px 0' }}>{approvedToday}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Off-Campus Residents</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#6366f1', margin: '8px 0' }}>{Math.max(0, approvedToday - 2)}</div>
                </div>
            </div>

            {activeTab === 'request' && user.role === 'STUDENT' && (
                <div style={{ maxWidth: '700px', margin: '0 auto' }}>
                    <div className="stat-card" style={{ padding: '30px' }}>
                        <h3 style={{ marginBottom: '20px', borderBottom: '1px solid #f1f5f9', paddingBottom: '15px' }}>📝 Apply for Outing/Leave</h3>
                        <form className="form-grid" onSubmit={handleRequestPass}>
                            <div className="form-group">
                                <label>Outing Start *</label>
                                <input type="date" className="form-control" required value={formData.fromDate} onChange={e => setFormData({ ...formData, fromDate: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Expected Return *</label>
                                <input type="date" className="form-control" required value={formData.toDate} onChange={e => setFormData({ ...formData, toDate: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Full Destination Address *</label>
                                <input type="text" className="form-control" placeholder="House no, Street, City, ZIP..." required value={formData.destination} onChange={e => setFormData({ ...formData, destination: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Purpose / Reason for Leave *</label>
                                <textarea className="form-control" placeholder="Specifically explain the necessity of this outing..." required rows="4" value={formData.reason} onChange={e => setFormData({ ...formData, reason: e.target.value })}></textarea>
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Emergency Contact (Parent/Guardian) *</label>
                                <input type="text" className="form-control" placeholder="+91 91234 56789" required value={formData.parentContact} onChange={e => setFormData({ ...formData, parentContact: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '10px' }}>
                                <button type="submit" disabled={isSubmitting} className="btn btn-primary" style={{ width: '100%', padding: '15px', fontWeight: 'bold', fontSize: '1rem' }}>
                                    {isSubmitting ? 'Sending Request...' : 'Submit Digital Request'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {(activeTab === 'my_passes' || activeTab === 'history') && (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                {user.role !== 'STUDENT' && <th>Student Identity</th>}
                                <th>Destination</th>
                                <th>Schedule</th>
                                <th>Outcome</th>
                                <th>Remarks</th>
                            </tr>
                        </thead>
                        <tbody>
                            {passes.length === 0 ? (
                                <tr><td colSpan={user.role !== 'STUDENT' ? 5 : 4} style={{ textAlign: 'center', padding: '60px', color: '#94a3b8' }}>
                                    <div style={{ fontSize: '2rem', marginBottom: '10px' }}>📬</div>
                                    No gate pass history found.
                                </td></tr>
                            ) : (
                                passes.map(p => (
                                    <tr key={p.id}>
                                        {user.role !== 'STUDENT' && (
                                            <td>
                                                <strong>{p.studentName}</strong><br />
                                                <code style={{ fontSize: '0.75rem', color: '#64748b' }}>{p.enrollmentId || 'ID:EX-001'}</code>
                                            </td>
                                        )}
                                        <td style={{ maxWidth: '200px', fontSize: '0.9rem' }}>{p.destination}</td>
                                        <td style={{ fontSize: '0.85rem' }}>
                                            <span style={{ color: '#64748b' }}>From:</span> {p.fromDate}<br />
                                            <span style={{ color: '#64748b' }}>To:</span> {p.toDate}
                                        </td>
                                        <td>
                                            <span className={`badge ${p.status === 'APPROVED' ? 'badge-success' : p.status === 'REJECTED' ? 'badge-danger' : 'badge-warning'}`} style={{ padding: '6px 12px', borderRadius: '20px' }}>
                                                {p.status}
                                            </span>
                                        </td>
                                        <td style={{ fontSize: '0.85rem', color: '#4b5563', fontStyle: 'italic' }}>
                                            {p.approvalComment || '—'}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {activeTab === 'pending' && user.role !== 'STUDENT' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(450px, 1fr))', gap: '25px' }}>
                    {passes.length === 0 ? (
                        <div className="stat-card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '60px', color: '#94a3b8' }}>
                            <div style={{ fontSize: '3rem', marginBottom: '15px' }}>✅</div>
                            <p>Queue is empty. All gate pass requests have been processed.</p>
                        </div>
                    ) : (
                        passes.map(p => (
                            <div key={p.id} className="stat-card" style={{ borderLeft: '6px solid #f6ad55', display: 'flex', flexDirection: 'column' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                                    <div>
                                        <h4 style={{ margin: 0 }}>{p.studentName}</h4>
                                        <code style={{ fontSize: '0.8rem' }}>{p.enrollmentId || 'ID:EX-001'}</code>
                                    </div>
                                    <div style={{ textAlign: 'right' }}>
                                        <div style={{ fontSize: '0.7rem', color: '#94a3b8', textTransform: 'uppercase' }}>Applied On</div>
                                        <div style={{ fontSize: '0.85rem' }}>{new Date().toLocaleDateString()}</div>
                                    </div>
                                </div>

                                <div style={{ background: '#f8fafc', padding: '15px', borderRadius: '8px', marginBottom: '20px', fontSize: '0.9rem' }}>
                                    <div style={{ marginBottom: '8px' }}>📍 <strong>Dest:</strong> {p.destination}</div>
                                    <div style={{ marginBottom: '8px' }}>🗓️ <strong>Dates:</strong> {p.fromDate} to {p.toDate}</div>
                                    <div style={{ marginBottom: '8px' }}>❓ <strong>Reason:</strong> {p.reason}</div>
                                    <div>📞 <strong>Parent:</strong> {p.parentContact}</div>
                                </div>

                                <div style={{ marginTop: 'auto' }}>
                                    <label style={{ fontSize: '0.75rem', fontWeight: 'bold', color: '#64748b' }}>WARDEN REMARKS:</label>
                                    <textarea
                                        placeholder="Add approval context or rejection reason..."
                                        className="form-control"
                                        rows="2"
                                        value={actionComment}
                                        onChange={e => setActionComment(e.target.value)}
                                        style={{ marginTop: '5px', marginBottom: '15px' }}
                                    ></textarea>
                                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                                        <button className="btn btn-success" style={{ fontWeight: 'bold' }} onClick={() => handleApprove(p.id)}>Approve Pass</button>
                                        <button className="btn btn-danger" style={{ fontWeight: 'bold' }} onClick={() => handleReject(p.id)}>Reject</button>
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            )}
        </div>
    );
};

export default GatePassPage;
