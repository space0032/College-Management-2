import SessionManager from '../utils/SessionManager';
import React, { useState, useEffect } from 'react';
import { getStaffLeaves, createStaffLeave } from '../services/leaveService';
import Modal from '../components/Modal';

const LEAVE_TYPES = {
    'SICK': { label: 'Sick Leave', icon: '🤒', color: '#ef4444', bg: '#fef2f2' },
    'CASUAL': { label: 'Casual Leave', icon: '🏖️', color: '#3b82f6', bg: '#eff6ff' },
    'EARNED': { label: 'Earned Leave', icon: '⭐', color: '#f59e0b', bg: '#fffbeb' },
    'DUTY': { label: 'On-Duty', icon: '💼', color: '#10b981', bg: '#ecfdf5' }
};

const StaffLeavePage = () => {
    const [leaves, setLeaves] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({ leaveType: 'SICK', startDate: '', endDate: '', reason: '' });

    const currentUser = SessionManager.getUser() || {};

    const fetchLeaves = React.useCallback(async () => {
        setLoading(true);
        try {
            const res = await getStaffLeaves(currentUser.id);
            setLeaves(res.data || []);
        } catch { console.error('Failed to load leaves'); }
        finally { setLoading(false); }
    }, [currentUser.id]);

    useEffect(() => {
        fetchLeaves();
    }, [fetchLeaves]);

    const handleApply = async (e) => {
        e.preventDefault();
        const today = new Date().toISOString().split('T')[0];
        if (!formData.startDate || !formData.endDate) { alert('Start date and end date are required.'); return; }
        if (formData.startDate < today) { alert('Start date cannot be in the past.'); return; }
        if (formData.endDate < formData.startDate) { alert('End date cannot be before start date.'); return; }
        try {
            await createStaffLeave({ ...formData, staffId: currentUser.id });
            setShowModal(false);
            fetchLeaves();
        } catch { alert('Application failed.'); }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">📅 Staff Leave Management</h1>
                    <p className="page-subtitle">Personal absence portal and balance tracking</p>
                </div>
                <button className="btn btn-primary" onClick={() => setShowModal(true)}>+ Request Leave</button>
            </div>

            {/* Balances Card — computed from actual approved leave records */}
            {(() => {
                const approved = leaves.filter(l => l.status === 'APPROVED');
                const used = (type) => approved.filter(l => l.leaveType === type).length;
                const pending = leaves.filter(l => l.status === 'APPLIED').length;
                const LIMITS = { SICK: 12, CASUAL: 5, EARNED: 15 };
                return (
                    <div className="stat-card" style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '30px', background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)', color: 'white' }}>
                        {['SICK', 'CASUAL', 'EARNED'].map(type => {
                            const usedCount = used(type);
                            const limit = LIMITS[type];
                            const remaining = limit - usedCount;
                            const pct = (usedCount / limit) * 100;
                            return (
                                <div key={type}>
                                    <div style={{ fontSize: '0.8rem', opacity: 0.8 }}>{type.charAt(0) + type.slice(1).toLowerCase()} Leave</div>
                                    <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: remaining < 3 ? '#f87171' : 'white' }}>{remaining}/{limit}</div>
                                    <div style={{ marginTop: '6px', height: '4px', background: 'rgba(255,255,255,0.2)', borderRadius: '4px' }}>
                                        <div style={{ width: `${pct}%`, height: '100%', background: pct > 80 ? '#f87171' : '#34d399', borderRadius: '4px', transition: 'width 0.5s' }} />
                                    </div>
                                    <div style={{ fontSize: '0.72rem', opacity: 0.7, marginTop: '3px' }}>{usedCount} used</div>
                                </div>
                            );
                        })}
                        <div>
                            <div style={{ fontSize: '0.8rem', opacity: 0.8 }}>Total Pending</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#f59e0b' }}>{pending}</div>
                            <div style={{ fontSize: '0.72rem', opacity: 0.7, marginTop: '3px' }}>awaiting approval</div>
                        </div>
                    </div>
                );
            })()}

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
                {loading ? <p>Loading history...</p> : leaves.map(l => (
                    <div key={l.id} className="stat-card" style={{ display: 'flex', gap: '15px', alignItems: 'flex-start' }}>
                        <div style={{ padding: '12px', background: LEAVE_TYPES[l.leaveType]?.bg || '#f1f5f9', borderRadius: '12px', fontSize: '1.5rem' }}>
                            {LEAVE_TYPES[l.leaveType]?.icon || '📄'}
                        </div>
                        <div style={{ flex: 1 }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <h4 style={{ margin: 0 }}>{LEAVE_TYPES[l.leaveType]?.label || l.leaveType}</h4>
                                <span className={`badge ${l.status === 'APPROVED' ? 'badge-success' : l.status === 'REJECTED' ? 'badge-danger' : 'badge-warning'}`}>{l.status}</span>
                            </div>
                            <div style={{ fontSize: '0.85rem', color: '#64748b', margin: '5px 0' }}>{l.startDate} to {l.endDate}</div>
                            <p style={{ fontSize: '0.8rem', margin: '8px 0 0 0', color: '#475569' }}>{l.reason}</p>
                        </div>
                    </div>
                ))}
            </div>

            {showModal && (
                <Modal isOpen={showModal} title="New Leave Application" onClose={() => setShowModal(false)} onSubmit={handleApply}>
                    <div className="form-grid">
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Leave Type</label>
                            <select className="form-control" value={formData.leaveType} onChange={e => setFormData({ ...formData, leaveType: e.target.value })}>
                                {Object.entries(LEAVE_TYPES).map(([k, v]) => <option key={k} value={k}>{v.icon} {v.label}</option>)}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Start Date</label>
                            <input type="date" className="form-control" value={formData.startDate} onChange={e => setFormData({ ...formData, startDate: e.target.value })} />
                        </div>
                        <div className="form-group">
                            <label>End Date</label>
                            <input type="date" className="form-control" value={formData.endDate} onChange={e => setFormData({ ...formData, endDate: e.target.value })} />
                        </div>
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Reason</label>
                            <textarea className="form-control" rows="3" value={formData.reason} onChange={e => setFormData({ ...formData, reason: e.target.value })}></textarea>
                        </div>
                    </div>
                </Modal>
            )}
        </div>
    );
};

export default StaffLeavePage;
