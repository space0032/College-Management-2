import React, { useState, useEffect, useCallback } from 'react';
import {
    getEvents, registerEvent, getStudentEvents, unregisterEvent,
    getEventRegistrations, markAttendance, createEvent,
    getEventBudgets, addEventBudget, deleteEventBudget,
    getEventPolls, createEventPoll, closeEventPoll
} from '../services/eventService';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import SessionManager from '../utils/SessionManager';

const EventsPage = () => {
    const [, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('browse');
    const [events, setEvents] = useState([]);
    const [myEvents, setMyEvents] = useState([]);


    // Management state
    const [selectedEventId, setSelectedEventId] = useState('');
    const [eventRegistrations, setEventRegistrations] = useState([]);
    const [budgets, setBudgets] = useState([]);
    const [polls, setPolls] = useState([]);
    const [manageMode, setManageMode] = useState('attendance'); // attendance, budget, polls

    // Create event form
    const EMPTY_EVENT = { name: '', eventType: 'Workshop', description: '', location: '', startTime: '', endTime: '', maxParticipants: '', status: 'UPCOMING' };
    const [createForm, setCreateForm] = useState(EMPTY_EVENT);
    const [createSaving, setCreateSaving] = useState(false);

    // Modal state for Add Item
    const [itemModal, setItemModal] = useState({ open: false, type: '', title: '' });
    const [budgetForm, setBudgetForm] = useState({ item: '', estimatedCost: '', actualCost: '0', status: 'PLANNED' });
    const [pollForm, setPollForm] = useState({ question: '', options: '', status: 'ACTIVE' });

    const user = SessionManager.getUser() || {};
    const userRole = SessionManager.getUserRole() || 'STUDENT';
    const isAdmin = userRole === 'ADMIN' || userRole === 'FACULTY';

    const loadData = useCallback(async () => {
        setLoading(true);
        try {
            const [evRes, myRes] = await Promise.all([
                getEvents(),
                user.role === 'STUDENT' ? getStudentEvents(user.id) : Promise.resolve({ data: [] })
            ]);
            setEvents(evRes.data || []);
            setMyEvents(myRes.data || []);
            if (!selectedEventId && evRes.data?.length > 0) {
                setSelectedEventId(evRes.data[0].id.toString());
            }
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    }, [user.id, user.role, selectedEventId]);

    useEffect(() => { loadData(); }, [loadData]);

    const loadEventDetails = useCallback(async (eventId) => {
        if (!eventId) return;
        try {
            const [regRes, budRes, pollRes] = await Promise.all([
                getEventRegistrations(eventId),
                getEventBudgets(eventId),
                getEventPolls(eventId)
            ]);
            setEventRegistrations(regRes.data || []);
            setBudgets(budRes.data || []);
            setPolls(pollRes.data || []);
        } catch (err) {
            console.error(err);
        }
    }, []);

    useEffect(() => {
        if (selectedEventId) loadEventDetails(selectedEventId);
    }, [selectedEventId, loadEventDetails]);

    const handleRegister = async (eventId) => {
        try {
            await registerEvent(eventId, user.id);
            alert('Registration successful!');
            loadData();
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to register.');
        }
    };

    const handleUnregister = async (eventId) => {
        if (!window.confirm('Cancel your registration?')) return;
        try {
            await unregisterEvent(eventId, user.id);
            loadData();
        } catch (err) {
            alert('Failed to unregister.');
        }
    };

    const handleCreateEvent = async (e) => {
        e.preventDefault();
        setCreateSaving(true);
        try {
            await createEvent(createForm);
            setCreateForm(EMPTY_EVENT);
            loadData();
            setActiveTab('browse');
        } catch (err) {
            alert('Failed to create event.');
        } finally {
            setCreateSaving(false);
        }
    };

    const handleSaveBudget = async () => {
        try {
            await addEventBudget(selectedEventId, budgetForm);
            loadEventDetails(selectedEventId);
            setItemModal({ open: false });
        } catch (err) { alert('Failed to add budget item.'); }
    };

    const handleSavePoll = async () => {
        try {
            await createEventPoll(selectedEventId, pollForm);
            loadEventDetails(selectedEventId);
            setItemModal({ open: false });
        } catch (err) { alert('Failed to create poll.'); }
    };

    const stats = [
        { label: 'Total Events', value: events.length, icon: '📅', color: '#3182ce' },
        { label: 'Scheduled', value: events.filter(e => e.status === 'UPCOMING').length, icon: '⏰', color: '#ecc94b' },
        { label: 'Completed', value: events.filter(e => e.status === 'COMPLETED').length, icon: '✅', color: '#48bb78' },
        { label: 'My Participation', value: myEvents.length, icon: '🤝', color: '#9f7aea' },
    ];

    return (
        <div className="page-container" style={{ maxWidth: '1200px', margin: '0 auto' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div>
                    <h1 className="page-title">🎪 Events & Activities</h1>
                    <p style={{ color: '#718096', margin: 0 }}>Discover, manage, and participate in campus life milestones.</p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('browse')}>Browse</button>
                    <button className={`btn ${activeTab === 'my_events' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('my_events')}>My Events</button>
                    {isAdmin && <button className={`btn ${activeTab === 'manage' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('manage')}>Control Center</button>}
                    {isAdmin && <button className={`btn ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('create')}>+ New Event</button>}
                </div>
            </div>

            {/* Stats Row */}
            <div className="stats-row" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '30px' }}>
                {stats.map(s => (
                    <div key={s.label} className="stat-card" style={{ borderLeft: `4px solid ${s.color}`, padding: '20px' }}>
                        <div style={{ fontSize: '2rem', marginBottom: '10px' }}>{s.icon}</div>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{s.value}</div>
                        <div style={{ color: '#718096', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{s.label}</div>
                    </div>
                ))}
            </div>

            {activeTab === 'browse' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '25px' }}>
                    {events.map(ev => (
                        <div key={ev.id} className="stat-card" style={{ padding: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
                            <div style={{ height: '140px', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                <div style={{ fontSize: '3rem' }}>{ev.eventType === 'Workshop' ? '🛠️' : ev.eventType === 'Sports' ? '🏆' : '🏛️'}</div>
                                <span className={`badge ${ev.status === 'UPCOMING' ? 'badge-primary' : 'badge-success'}`} style={{ position: 'absolute', top: '15px', right: '15px' }}>{ev.status}</span>
                            </div>
                            <div style={{ padding: '20px', flex: 1 }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                    <h3 style={{ margin: 0 }}>{ev.name}</h3>
                                    <span style={{ fontSize: '0.8em', color: '#a0aec0' }}>#{ev.id}</span>
                                </div>
                                <p style={{ color: '#718096', fontSize: '0.9rem', margin: '12px 0' }}>{ev.description || 'No description provided.'}</p>
                                <div style={{ display: 'flex', gap: '15px', fontSize: '0.85rem', color: '#4a5568', marginBottom: '20px' }}>
                                    <div>📍 {ev.location}</div>
                                    <div>👥 {ev.registrationCount} / {ev.maxParticipants || '∞'}</div>
                                </div>
                                <div style={{ background: '#f7fafc', padding: '10px', borderRadius: '8px', fontSize: '0.85rem' }}>
                                    🗓️ <strong>Starts:</strong> {new Date(ev.startTime).toLocaleString()}
                                </div>
                            </div>
                            <div style={{ padding: '15px', borderTop: '1px solid #edf2f7' }}>
                                <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => handleRegister(ev.id)} disabled={ev.status === 'COMPLETED'}>
                                    {ev.status === 'COMPLETED' ? 'Event Expired' : 'Join Event'}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {activeTab === 'my_events' && (
                <div className="stat-card">
                    <DataTable
                        columns={[
                            { label: 'Event', key: 'name', render: (v) => <strong>{v}</strong> },
                            { label: 'Type', key: 'eventType' },
                            { label: 'Starts', key: 'startTime', render: (v) => new Date(v).toLocaleString() },
                            { label: 'Status', key: 'status', render: (v) => <span className="badge badge-primary">{v}</span> },
                            { label: 'Actions', key: 'id', render: (_, row) => <button className="btn btn-sm btn-danger" onClick={() => handleUnregister(row.id)}>Leave</button> }
                        ]}
                        data={myEvents}
                    />
                </div>
            )}

            {activeTab === 'manage' && (
                <div style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '25px' }}>
                    <div className="stat-card">
                        <label style={{ fontWeight: '600', display: 'block', marginBottom: '10px' }}>Select Event to Manage</label>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            {events.map(ev => (
                                <button
                                    key={ev.id}
                                    onClick={() => setSelectedEventId(ev.id.toString())}
                                    style={{
                                        textAlign: 'left', padding: '12px', borderRadius: '8px',
                                        border: '1px solid #e2e8f0', background: selectedEventId === ev.id.toString() ? '#ebf8ff' : 'white',
                                        cursor: 'pointer', transition: 'all 0.2s'
                                    }}
                                >
                                    <div style={{ fontWeight: '600', fontSize: '0.9rem' }}>{ev.name}</div>
                                    <div style={{ fontSize: '0.75rem', color: '#718096' }}>{ev.eventType} • {ev.status}</div>
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="stat-card">
                        <div className="tab-buttons" style={{ marginBottom: '20px' }}>
                            <button className={`btn-tab ${manageMode === 'attendance' ? 'active' : ''}`} onClick={() => setManageMode('attendance')}>👥 Attendance</button>
                            <button className={`btn-tab ${manageMode === 'budget' ? 'active' : ''}`} onClick={() => setManageMode('budget')}>💰 Budget</button>
                            <button className={`btn-tab ${manageMode === 'polls' ? 'active' : ''}`} onClick={() => setManageMode('polls')}>🗳️ Polls</button>
                        </div>

                        {manageMode === 'attendance' && (
                            <DataTable
                                columns={[
                                    { label: 'Student', key: 'studentName' },
                                    { label: 'Registered', key: 'registeredAt', render: (v) => new Date(v).toLocaleDateString() },
                                    { label: 'Status', key: 'attendanceStatus' },
                                    {
                                        label: 'Action', key: 'id', render: (_, row) => (
                                            <select value={row.attendanceStatus || 'PENDING'} onChange={(e) => markAttendance(row.id, e.target.value)} style={{ padding: '5px', borderRadius: '4px' }}>
                                                <option value="PENDING">Pending</option>
                                                <option value="ATTENDED">Attended</option>
                                                <option value="ABSENT">Absent</option>
                                            </select>
                                        )
                                    }
                                ]}
                                data={eventRegistrations}
                            />
                        )}

                        {manageMode === 'budget' && (
                            <div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                                    <h3>Event Ledger</h3>
                                    <button className="btn btn-sm btn-primary" onClick={() => setItemModal({ open: true, type: 'budget', title: 'Add Budget Item' })}>+ Add Item</button>
                                </div>
                                <DataTable
                                    columns={[
                                        { label: 'Line Item', key: 'item' },
                                        { label: 'Est. Cost', key: 'estimatedCost', render: (v) => `$${v}` },
                                        { label: 'Actual', key: 'actualCost', render: (v) => v > 0 ? `$${v}` : '-' },
                                        { label: 'Status', key: 'status' },
                                        { label: 'Action', key: 'id', render: (v) => <button className="btn btn-sm btn-danger" onClick={() => deleteEventBudget(v).then(() => loadEventDetails(selectedEventId))}>Remove</button> }
                                    ]}
                                    data={budgets}
                                />
                                <div style={{ marginTop: '20px', padding: '15px', background: '#f7fafc', borderRadius: '12px', display: 'flex', justifyContent: 'space-around' }}>
                                    <div style={{ textAlign: 'center' }}>
                                        <div style={{ color: '#718096', fontSize: '0.75rem' }}>Total Budget</div>
                                        <div style={{ fontWeight: 'bold', fontSize: '1.2rem' }}>${budgets.reduce((s, b) => s + b.estimatedCost, 0).toFixed(2)}</div>
                                    </div>
                                    <div style={{ textAlign: 'center' }}>
                                        <div style={{ color: '#718096', fontSize: '0.75rem' }}>Spent So Far</div>
                                        <div style={{ fontWeight: 'bold', fontSize: '1.2rem', color: '#e53e3e' }}>${budgets.reduce((s, b) => s + b.actualCost, 0).toFixed(2)}</div>
                                    </div>
                                </div>
                            </div>
                        )}

                        {manageMode === 'polls' && (
                            <div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                                    <h3>Engagement Polls</h3>
                                    <button className="btn btn-sm btn-primary" onClick={() => setItemModal({ open: true, type: 'poll', title: 'Create Quick Poll' })}>+ Create Poll</button>
                                </div>
                                {polls.map(p => (
                                    <div key={p.id} style={{ border: '1px solid #e2e8f0', borderRadius: '12px', padding: '20px', marginBottom: '15px' }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                            <h4 style={{ margin: 0 }}>{p.question}</h4>
                                            <span className={`badge ${p.status === 'ACTIVE' ? 'badge-success' : 'badge-secondary'}`}>{p.status}</span>
                                        </div>
                                        <div style={{ marginTop: '15px' }}>
                                            {p.options.split(',').map(opt => (
                                                <div key={opt} style={{ background: '#f7fafc', padding: '8px 12px', borderRadius: '6px', marginBottom: '5px', fontSize: '0.9rem' }}>
                                                    {opt}
                                                </div>
                                            ))}
                                        </div>
                                        {p.status === 'ACTIVE' && (
                                            <button className="btn btn-sm btn-secondary" style={{ marginTop: '10px' }} onClick={() => closeEventPoll(p.id).then(() => loadEventDetails(selectedEventId))}>Close Poll</button>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            )}

            {activeTab === 'create' && (
                <div style={{ maxWidth: '800px', margin: '0 auto' }} className="stat-card">
                    <h2 style={{ marginTop: 0 }}>🎪 Create Event</h2>
                    <form onSubmit={handleCreateEvent} className="form-grid">
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Event Title</label>
                            <input type="text" value={createForm.name} onChange={e => setCreateForm({ ...createForm, name: e.target.value })} required placeholder="e.g. Science Fair 2026" />
                        </div>
                        <div className="form-group">
                            <label>Event Type</label>
                            <select value={createForm.eventType} onChange={e => setCreateForm({ ...createForm, eventType: e.target.value })}>
                                {['Workshop', 'Seminar', 'Hackathon', 'Cultural', 'Sports', 'Academic', 'Other'].map(t => <option key={t} value={t}>{t}</option>)}
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Location</label>
                            <input type="text" value={createForm.location} onChange={e => setCreateForm({ ...createForm, location: e.target.value })} placeholder="Main Auditorium" />
                        </div>
                        <div className="form-group">
                            <label>Start Time</label>
                            <input type="datetime-local" value={createForm.startTime} onChange={e => setCreateForm({ ...createForm, startTime: e.target.value })} required />
                        </div>
                        <div className="form-group">
                            <label>Max Capacity</label>
                            <input type="number" value={createForm.maxParticipants} onChange={e => setCreateForm({ ...createForm, maxParticipants: e.target.value })} placeholder="Unlimited" />
                        </div>
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Brief Description</label>
                            <textarea rows="3" value={createForm.description} onChange={e => setCreateForm({ ...createForm, description: e.target.value })} placeholder="What's this event about?" />
                        </div>
                        <div style={{ gridColumn: '1 / -1', marginTop: '10px' }}>
                            <button type="submit" className="btn btn-primary" disabled={createSaving} style={{ width: '200px' }}>
                                {createSaving ? 'Launching...' : 'Create Event'}
                            </button>
                        </div>
                    </form>
                </div>
            )}

            {/* Support Modals */}
            <Modal isOpen={itemModal.open} title={itemModal.title} onClose={() => setItemModal({ open: false })} onSubmit={itemModal.type === 'budget' ? handleSaveBudget : handleSavePoll}>
                {itemModal.type === 'budget' ? (
                    <>
                        <div className="form-group"><label>Line Item</label><input type="text" value={budgetForm.item} onChange={e => setBudgetForm({ ...budgetForm, item: e.target.value })} /></div>
                        <div className="form-group"><label>Est. Cost</label><input type="number" value={budgetForm.estimatedCost} onChange={e => setBudgetForm({ ...budgetForm, estimatedCost: parseFloat(e.target.value) })} /></div>
                    </>
                ) : (
                    <>
                        <div className="form-group"><label>Question</label><input type="text" value={pollForm.question} onChange={e => setPollForm({ ...pollForm, question: e.target.value })} /></div>
                        <div className="form-group"><label>Options (comma separated)</label><input type="text" value={pollForm.options} onChange={e => setPollForm({ ...pollForm, options: e.target.value })} /></div>
                    </>
                )}
            </Modal>
        </div>
    );
};

export default EventsPage;
