import React, { useState, useEffect } from 'react';
import {
    getStudentGatePasses, getPendingGatePasses, getAllGatePasses,
    requestGatePass, approveGatePass, rejectGatePass
} from '../services/gatePassService';

const GatePassPage = () => {
    const [activeTab, setActiveTab] = useState('my_passes');
    const [passes, setPasses] = useState([]);

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

    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : { id: 2, role: 'STUDENT' };

    useEffect(() => {
        // Default tab routing based on RBAC
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
        } catch (err) {
            console.error(err);
        }
    };

    const loadPendingPasses = async () => {
        try {
            const res = await getPendingGatePasses();
            setPasses(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const loadAllPasses = async () => {
        try {
            const res = await getAllGatePasses();
            setPasses(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const handleRequestPass = async (e) => {
        e.preventDefault();
        try {
            await requestGatePass({ ...formData, studentId: user.id });
            alert('Gate pass requested successfully. Awaiting approval.');
            setFormData({ fromDate: '', toDate: '', reason: '', destination: '', parentContact: '' });
            setActiveTab('my_passes');
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to request gate pass. Check hostel allocation.');
        }
    };

    const handleApprove = async (passId) => {
        try {
            await approveGatePass(passId, user.id, actionComment || 'Approved by Warden');
            setActionComment('');
            loadPendingPasses();
        } catch (err) {
            alert('Failed to approve pass');
        }
    };

    const handleReject = async (passId) => {
        try {
            await rejectGatePass(passId, user.id, actionComment || 'Rejected by Warden');
            setActionComment('');
            loadPendingPasses();
        } catch (err) {
            alert('Failed to reject pass');
        }
    };

    const renderStatusBadge = (status) => {
        switch (status) {
            case 'APPROVED': return <span className="badge badge-success">APPROVED</span>;
            case 'REJECTED': return <span className="badge badge-danger">REJECTED</span>;
            default: return <span className="badge badge-warning">PENDING</span>;
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Hostel Gate Passes</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    {user.role === 'STUDENT' && (
                        <>
                            <button
                                className={`btn ${activeTab === 'my_passes' ? 'btn-primary' : 'btn-secondary'}`}
                                onClick={() => setActiveTab('my_passes')}
                            >
                                My Passes
                            </button>
                            <button
                                className={`btn ${activeTab === 'request' ? 'btn-primary' : 'btn-secondary'}`}
                                onClick={() => setActiveTab('request')}
                            >
                                Request Pass
                            </button>
                        </>
                    )}
                    {user.role !== 'STUDENT' && (
                        <>
                            <button
                                className={`btn ${activeTab === 'pending' ? 'btn-primary' : 'btn-secondary'}`}
                                onClick={() => setActiveTab('pending')}
                            >
                                Pending Requests
                            </button>
                            <button
                                className={`btn ${activeTab === 'history' ? 'btn-primary' : 'btn-secondary'}`}
                                onClick={() => setActiveTab('history')}
                            >
                                All History
                            </button>
                        </>
                    )}
                </div>
            </div>

            {(activeTab === 'my_passes' || activeTab === 'history') && (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                {user.role !== 'STUDENT' && <th>Student</th>}
                                <th>Destination</th>
                                <th>Dates</th>
                                <th>Reason</th>
                                <th>Status</th>
                                <th>Remarks</th>
                            </tr>
                        </thead>
                        <tbody>
                            {passes.length === 0 ? (
                                <tr><td colSpan={user.role !== 'STUDENT' ? 6 : 5} style={{ textAlign: 'center' }}>No records found.</td></tr>
                            ) : (
                                passes.map(p => (
                                    <tr key={p.id}>
                                        {user.role !== 'STUDENT' && <td>{p.studentName}<br /><small>{p.enrollmentId || ''}</small></td>}
                                        <td>{p.destination}</td>
                                        <td>{p.fromDate} to {p.toDate}</td>
                                        <td>{p.reason}</td>
                                        <td>{renderStatusBadge(p.status)}</td>
                                        <td><small>{p.approvalComment || '-'}</small></td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {activeTab === 'request' && user.role === 'STUDENT' && (
                <div className="stat-card" style={{ maxWidth: '600px', margin: '0 auto' }}>
                    <h3>Apply for Gate Pass</h3>
                    <form className="form-grid" onSubmit={handleRequestPass} style={{ marginTop: '20px' }}>
                        <div className="form-group">
                            <label>From Date *</label>
                            <input type="date" required value={formData.fromDate} onChange={e => setFormData({ ...formData, fromDate: e.target.value })} />
                        </div>
                        <div className="form-group">
                            <label>To Date *</label>
                            <input type="date" required value={formData.toDate} onChange={e => setFormData({ ...formData, toDate: e.target.value })} />
                        </div>
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Destination Address *</label>
                            <input type="text" required value={formData.destination} onChange={e => setFormData({ ...formData, destination: e.target.value })} />
                        </div>
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Reason *</label>
                            <textarea required rows="3" value={formData.reason} onChange={e => setFormData({ ...formData, reason: e.target.value })} style={{ width: '100%', padding: '8px', border: '1px solid #ddd', borderRadius: '4px' }}></textarea>
                        </div>
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Parent Contact Number *</label>
                            <input type="text" required value={formData.parentContact} onChange={e => setFormData({ ...formData, parentContact: e.target.value })} />
                        </div>
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Submit Request</button>
                        </div>
                    </form>
                </div>
            )}

            {activeTab === 'pending' && user.role !== 'STUDENT' && (
                <div style={{ display: 'grid', gap: '20px' }}>
                    {passes.length === 0 ? (
                        <div className="stat-card">No pending gate pass requests.</div>
                    ) : (
                        passes.map(p => (
                            <div key={p.id} className="stat-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <h4>{p.studentName} <small>({p.enrollmentId || 'ID Unavailable'})</small></h4>
                                    <p style={{ margin: '5px 0' }}><strong>Destination:</strong> {p.destination}</p>
                                    <p style={{ margin: '5px 0' }}><strong>Dates:</strong> {p.fromDate} to {p.toDate}</p>
                                    <p style={{ margin: '5px 0' }}><strong>Reason:</strong> {p.reason}</p>
                                    <p style={{ margin: '5px 0' }}><strong>Parent Contact:</strong> {p.parentContact}</p>
                                    <div style={{ marginTop: '10px' }}>
                                        <input
                                            type="text"
                                            placeholder="Optional remark..."
                                            value={actionComment}
                                            onChange={e => setActionComment(e.target.value)}
                                            style={{ padding: '8px', border: '1px solid #ddd', borderRadius: '4px', width: '300px' }}
                                        />
                                    </div>
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                    <button className="btn btn-success" onClick={() => handleApprove(p.id)}>Approve</button>
                                    <button className="btn btn-danger" onClick={() => handleReject(p.id)}>Reject</button>
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
