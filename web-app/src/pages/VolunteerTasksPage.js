import React, { useState, useEffect } from 'react';
import { getMyVolunteerTasks, getVolunteerOpportunities, applyToVolunteer } from '../services/volunteerService';

const STATUS_MAP = {
    REGISTERED: { label: 'Registered', color: '#f59e0b', bg: '#fffbeb', icon: '📝' },
    APPROVED: { label: 'Assigned', color: '#10b981', bg: '#ecfdf5', icon: '✅' },
    COMPLETED: { label: 'Honored', color: '#3b82f6', bg: '#eff6ff', icon: '🏆' },
    REJECTED: { label: 'Archived', color: '#ef4444', bg: '#fef2f2', icon: '📁' },
};

const VolunteerTasksPage = () => {
    const user = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();

    const [activeTab, setActiveTab] = useState('opportunities'); // opportunities, my-tasks
    const [myTasks, setMyTasks] = useState([]);
    const [opportunities, setOpportunities] = useState([]);
    const [loading, setLoading] = useState(false);
    const [applyModal, setApplyModal] = useState(null);
    const [taskDesc, setTaskDesc] = useState('');

    useEffect(() => {
        if (activeTab === 'opportunities') fetchOpportunities();
        else fetchMyTasks();
    }, [activeTab]);

    const fetchMyTasks = () => {
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

    const handleApply = async (e) => {
        e.preventDefault();
        try {
            await applyToVolunteer(user.id, applyModal.id, taskDesc);
            alert('Quest accepted! Awaiting overseer approval.');
            setApplyModal(null);
            setTaskDesc('');
            setActiveTab('my-tasks');
        } catch (err) { alert('Application failed'); }
    };

    const totalHours = myTasks.reduce((s, t) => s + (parseFloat(t.hoursLogged) || 0), 0);

    return (
        <div className="page-container" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h1 className="page-title">🤝 Social Impact & Service</h1>
                        <p className="page-subtitle">Participate in campus initiatives, organize events, and track your community contributions</p>
                    </div>
                    <div style={{ display: 'flex', gap: '5px', background: '#e2e8f0', padding: '4px', borderRadius: '12px' }}>
                        <button className={`btn btn-sm ${activeTab === 'opportunities' ? 'btn-primary' : ''}`} style={{ background: activeTab === 'opportunities' ? '#3b82f6' : 'transparent', color: activeTab === 'opportunities' ? 'white' : '#475569', border: 'none' }} onClick={() => setActiveTab('opportunities')}>Live Quests</button>
                        <button className={`btn btn-sm ${activeTab === 'my-tasks' ? 'btn-primary' : ''}`} style={{ background: activeTab === 'my-tasks' ? '#3b82f6' : 'transparent', color: activeTab === 'my-tasks' ? 'white' : '#475569', border: 'none' }} onClick={() => setActiveTab('my-tasks')}>My Impact</button>
                    </div>
                </div>
            </div>

            {/* Impact Dashboard */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '35px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Cumulative Impact</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>{totalHours.toFixed(1)} Hrs</div>
                    <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Logged Service Time</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Active Quests</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#f59e0b', margin: '8px 0' }}>{myTasks.filter(t => t.status === 'APPROVED').length}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Events Served</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#10b981', margin: '8px 0' }}>{new Set(myTasks.map(t => t.eventName)).size}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Service Rank</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#8b5cf6', margin: '8px 0' }}>🎖️ Gold</div>
                </div>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '100px', color: '#94a3b8' }}>🗺️ Scanning campus for opportunities...</div>
            ) : (
                <div className="card-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '25px' }}>
                    {activeTab === 'opportunities' ? (
                        opportunities.map(ev => (
                            <div key={ev.id} className="stat-card" style={{ padding: '30px', display: 'flex', flexDirection: 'column', transition: 'transform 0.2s' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                                    <div style={{ fontSize: '2.5rem', background: '#f1f5f9', width: '64px', height: '64px', borderRadius: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>🎪</div>
                                    <span className="badge" style={{ background: '#e0e7ff', color: '#4338ca', height: 'fit-content' }}>{ev.eventType}</span>
                                </div>
                                <h3 style={{ margin: '0 0 10px 0', fontSize: '1.2rem' }}>{ev.name}</h3>
                                <div style={{ display: 'flex', gap: '15px', fontSize: '0.85rem', color: '#64748b', marginBottom: '20px' }}>
                                    <span>📍 {ev.location || 'Campus'}</span>
                                    <span>📅 {new Date(ev.startTime).toLocaleDateString()}</span>
                                </div>
                                <p style={{ fontSize: '0.85rem', color: '#475569', flex: 1, marginBottom: '25px', lineHeight: '1.6' }}>Join the team for this landmark campus event. Volunteers needed for logistics, hospitality, and tech support.</p>
                                <button className="btn btn-primary" style={{ width: '100%', padding: '14px', fontWeight: 'bold' }} onClick={() => setApplyModal(ev)}>Accept Quest</button>
                            </div>
                        ))
                    ) : (
                        myTasks.map(t => {
                            const style = STATUS_MAP[t.status] || STATUS_MAP.REGISTERED;
                            return (
                                <div key={t.id} className="stat-card" style={{ borderLeft: `6px solid ${style.color}`, padding: '25px' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                                        <h4 style={{ margin: 0 }}>{t.eventName}</h4>
                                        <span className="badge" style={{ background: style.bg, color: style.color, display: 'flex', alignItems: 'center', gap: '5px' }}>{style.icon} {style.label}</span>
                                    </div>
                                    <p style={{ fontSize: '0.82rem', color: '#64748b', fontStyle: 'italic', marginBottom: '15px' }}>"{t.taskDescription}"</p>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '15px', borderTop: '1px solid #f1f5f9' }}>
                                        <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Impact Record: {t.id}</span>
                                        <span style={{ fontWeight: 'bold', color: '#3b82f6' }}>{parseFloat(t.hoursLogged || 0).toFixed(1)} Hours</span>
                                    </div>
                                </div>
                            );
                        })
                    )}
                    {((activeTab === 'opportunities' && opportunities.length === 0) || (activeTab === 'my-tasks' && myTasks.length === 0)) && (
                        <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '100px', color: '#94a3b8' }}>
                            <div style={{ fontSize: '3rem', marginBottom: '15px' }}>🧩</div>
                            <p>All quests are currently assigned or completed. Check back later!</p>
                        </div>
                    )}
                </div>
            )}

            {applyModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '450px', borderRadius: '20px', padding: '40px' }}>
                        <h2 style={{ marginBottom: '10px' }}>Join the Team</h2>
                        <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '30px' }}>Project: <strong>{applyModal.name}</strong></p>
                        <form onSubmit={handleApply}>
                            <div className="form-group">
                                <label style={{ fontWeight: 'bold' }}>How will you contribute? *</label>
                                <textarea required rows="5" className="form-control" value={taskDesc} onChange={e => setTaskDesc(e.target.value)} placeholder="Describe your relevant skills or preferred role (e.g. Photography, Crowd Control, Technical Setup)..." style={{ borderRadius: '12px' }}></textarea>
                            </div>
                            <div style={{ display: 'flex', gap: '15px', marginTop: '30px' }}>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setApplyModal(null)}>Discard</button>
                                <button type="submit" className="btn btn-primary" style={{ flex: 2, padding: '15px', fontWeight: 'bold' }}>Commit to Quest</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default VolunteerTasksPage;
