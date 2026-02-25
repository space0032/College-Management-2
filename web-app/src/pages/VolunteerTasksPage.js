import React, { useState, useEffect } from 'react';
import { getMyVolunteerTasks, getVolunteerOpportunities, applyToVolunteer } from '../services/volunteerService';

const STATUS_STYLES = {
    REGISTERED: { background: '#fffbeb', color: '#92400e' },
    APPROVED: { background: '#f0fff4', color: '#276749' },
    COMPLETED: { background: '#ebf8ff', color: '#2c5282' },
    REJECTED: { background: '#fff5f5', color: '#c53030' },
};

const VolunteerTasksPage = () => {
    const user = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();

    const [activeTab, setActiveTab] = useState('my-tasks');
    const [myTasks, setMyTasks] = useState([]);
    const [opportunities, setOpportunities] = useState([]);
    const [loading, setLoading] = useState(false);
    const [applyModal, setApplyModal] = useState(null); // { event }
    const [taskDesc, setTaskDesc] = useState('');
    const [error, setError] = useState(null);

    const fetchMyTasks = () => {
        if (!user.id) return;
        setLoading(true);
        getMyVolunteerTasks(user.id)
            .then(res => setMyTasks(Array.isArray(res.data) ? res.data : []))
            .catch(() => setMyTasks([]))
            .finally(() => setLoading(false));
    };

    const fetchOpportunities = () => {
        setLoading(true);
        getVolunteerOpportunities()
            .then(res => setOpportunities(Array.isArray(res.data) ? res.data : []))
            .catch(() => setOpportunities([]))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        if (activeTab === 'my-tasks') fetchMyTasks();
        else fetchOpportunities();
    }, [activeTab]); // eslint-disable-line

    const handleApply = async (e) => {
        e.preventDefault();
        setError(null);
        if (!taskDesc.trim()) { setError('Please describe how you can help.'); return; }
        try {
            await applyToVolunteer(user.id, applyModal.id, taskDesc);
            alert('Application submitted! Awaiting approval.');
            setApplyModal(null);
            setTaskDesc('');
            fetchMyTasks();
        } catch (err) {
            const msg = err.response?.data?.error || 'Failed to submit application.';
            setError(msg);
        }
    };

    const tabStyle = (tab) => ({
        padding: '10px 24px', border: 'none',
        borderBottom: activeTab === tab ? '3px solid #3b82f6' : '3px solid transparent',
        background: 'none', cursor: 'pointer',
        fontWeight: activeTab === tab ? '600' : '400',
        color: activeTab === tab ? '#3b82f6' : '#718096',
        fontSize: '0.95rem', transition: 'all 0.2s'
    });

    const totalHours = myTasks.reduce((s, t) => s + (parseFloat(t.hoursLogged) || 0), 0);

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>🤝 Volunteer Portal</h2>
                    <p className="text-muted">Volunteer for campus events and track your contributions.</p>
                </div>
            </div>

            {user.role !== 'STUDENT' && (
                <div style={{ padding: '16px', background: '#fffbeb', border: '1px solid #fbd38d', borderRadius: '8px', marginBottom: '20px', color: '#92400e' }}>
                    ⚠️ This page is primarily for students. You can browse opportunities below.
                </div>
            )}

            {/* Summary if student */}
            {myTasks.length > 0 && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '14px', marginBottom: '20px' }}>
                    <div style={{ padding: '16px', background: '#ebf8ff', borderRadius: '10px', border: '1px solid #bee3f8' }}>
                        <div style={{ fontSize: '1.6rem', fontWeight: 'bold', color: '#2b6cb0' }}>{myTasks.length}</div>
                        <div style={{ color: '#4a5568', fontSize: '0.85rem' }}>Total Tasks</div>
                    </div>
                    <div style={{ padding: '16px', background: '#f0fff4', borderRadius: '10px', border: '1px solid #9ae6b4' }}>
                        <div style={{ fontSize: '1.6rem', fontWeight: 'bold', color: '#276749' }}>
                            {myTasks.filter(t => t.status === 'APPROVED' || t.status === 'COMPLETED').length}
                        </div>
                        <div style={{ color: '#4a5568', fontSize: '0.85rem' }}>Approved/Completed</div>
                    </div>
                    <div style={{ padding: '16px', background: '#fffaf0', borderRadius: '10px', border: '1px solid #fbd38d' }}>
                        <div style={{ fontSize: '1.6rem', fontWeight: 'bold', color: '#7b341e' }}>{totalHours.toFixed(1)}</div>
                        <div style={{ color: '#4a5568', fontSize: '0.85rem' }}>Hours Logged</div>
                    </div>
                </div>
            )}

            {/* Tabs */}
            <div style={{ borderBottom: '1px solid #e2e8f0', marginBottom: '24px', display: 'flex' }}>
                <button style={tabStyle('my-tasks')} onClick={() => setActiveTab('my-tasks')}>📋 My Tasks</button>
                <button style={tabStyle('opportunities')} onClick={() => setActiveTab('opportunities')}>🔍 Browse Opportunities</button>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>Loading...</div>
            ) : activeTab === 'my-tasks' ? (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Event</th>
                                <th>Task Description</th>
                                <th>Status</th>
                                <th>Hours Logged</th>
                            </tr>
                        </thead>
                        <tbody>
                            {myTasks.length === 0 ? (
                                <tr>
                                    <td colSpan="4" style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                                        No volunteer tasks yet. Browse opportunities to apply!
                                    </td>
                                </tr>
                            ) : (
                                myTasks.map(t => (
                                    <tr key={t.id}>
                                        <td style={{ fontWeight: 500 }}>🎪 {t.eventName}</td>
                                        <td style={{ color: '#555' }}>{t.taskDescription}</td>
                                        <td>
                                            <span className="status-badge" style={STATUS_STYLES[t.status] || STATUS_STYLES['REGISTERED']}>
                                                {t.status}
                                            </span>
                                        </td>
                                        <td style={{ color: '#2b6cb0', fontWeight: 500 }}>
                                            {parseFloat(t.hoursLogged || 0).toFixed(1)} hrs
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Event Name</th>
                                <th>Type</th>
                                <th>Location</th>
                                <th>Date</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {opportunities.length === 0 ? (
                                <tr>
                                    <td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                                        No upcoming events available for volunteering right now.
                                    </td>
                                </tr>
                            ) : (
                                opportunities.map(ev => (
                                    <tr key={ev.id}>
                                        <td style={{ fontWeight: 500 }}>🎪 {ev.name}</td>
                                        <td><span className="status-badge" style={{ background: '#e9d8fd', color: '#553c9a' }}>{ev.eventType}</span></td>
                                        <td style={{ color: '#555' }}>{ev.location || '—'}</td>
                                        <td style={{ fontSize: '0.85rem', color: '#718096' }}>
                                            {ev.startTime ? new Date(ev.startTime).toLocaleDateString() : 'TBA'}
                                        </td>
                                        <td>
                                            <button
                                                className="btn btn-primary"
                                                style={{ padding: '5px 12px', fontSize: '0.82rem' }}
                                                onClick={() => { setApplyModal(ev); setTaskDesc(''); setError(null); }}
                                            >
                                                🙋 Volunteer
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Apply Modal */}
            {applyModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '440px' }}>
                        <div className="modal-header">
                            <h2>🤝 Volunteer for {applyModal.name}</h2>
                            <button className="modal-close" onClick={() => setApplyModal(null)}>×</button>
                        </div>
                        <form onSubmit={handleApply} style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                            {error && <div style={{ color: '#e53e3e', padding: '10px', background: '#fff5f5', borderRadius: '6px' }}>{error}</div>}
                            <div className="form-group">
                                <label>How would you like to help? *</label>
                                <textarea
                                    required
                                    rows={4}
                                    value={taskDesc}
                                    onChange={e => setTaskDesc(e.target.value)}
                                    placeholder="e.g., Technical setup, Stage decoration, Promotions, Registration desk..."
                                />
                            </div>
                            <div style={{ display: 'flex', gap: '10px' }}>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setApplyModal(null)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" style={{ flex: 2 }}>Submit Application</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default VolunteerTasksPage;
