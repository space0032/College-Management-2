import React, { useState, useEffect } from 'react';
import {
    getEvents, registerEvent, getStudentEvents, unregisterEvent,
    getEventRegistrations, markAttendance, createEvent
} from '../services/eventService';

const EventsPage = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [events, setEvents] = useState([]);
    const [myEvents, setMyEvents] = useState([]);

    // Management state
    const [selectedEventId, setSelectedEventId] = useState('');
    const [eventRegistrations, setEventRegistrations] = useState([]);

    // Create event form
    const EMPTY_EVENT = { name: '', eventType: 'Workshop', description: '', location: '', startTime: '', endTime: '', maxParticipants: '', status: 'UPCOMING' };
    const [createForm, setCreateForm] = useState(EMPTY_EVENT);
    const [createError, setCreateError] = useState('');
    const [createSaving, setCreateSaving] = useState(false);

    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : { id: 2, role: 'STUDENT' };

    useEffect(() => {
        if (activeTab === 'browse' || activeTab === 'manage') loadEvents();
        if (activeTab === 'my_events') loadMyEvents();
        // eslint-disable-next-line
    }, [activeTab]);

    useEffect(() => {
        if (activeTab === 'manage' && selectedEventId) {
            loadEventRegistrations(selectedEventId);
        }
    }, [activeTab, selectedEventId]);

    const loadEvents = async () => {
        try {
            const res = await getEvents();
            setEvents(res.data || []);
            if (!selectedEventId && res.data?.length > 0) {
                setSelectedEventId(res.data[0].id.toString());
            }
        } catch (err) {
            console.error(err);
        }
    };

    const loadMyEvents = async () => {
        try {
            const res = await getStudentEvents(user.id);
            setMyEvents(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const loadEventRegistrations = async (eventId) => {
        try {
            const res = await getEventRegistrations(eventId);
            setEventRegistrations(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const handleRegister = async (eventId) => {
        try {
            await registerEvent(eventId, user.id);
            alert('Registered for event successfully!');
            loadEvents(); // Optionally reload or optimistic update checking status
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to register for event.');
        }
    };

    const handleUnregister = async (eventId) => {
        if (!window.confirm('Are you sure you want to cancel your registration?')) return;
        try {
            await unregisterEvent(eventId, user.id);
            loadMyEvents();
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to unregister.');
        }
    };

    const handleMarkAttendance = async (regId, status) => {
        try {
            await markAttendance(regId, status);
            loadEventRegistrations(selectedEventId);
        } catch (err) {
            alert('Failed to update attendance.');
        }
    };

    const handleCreateEvent = async (e) => {
        e.preventDefault();
        if (!createForm.name || !createForm.startTime) { setCreateError('Name and start time are required.'); return; }
        setCreateSaving(true);
        setCreateError('');
        try {
            await createEvent(createForm);
            setCreateForm(EMPTY_EVENT);
            loadEvents();
            setActiveTab('browse');
        } catch (err) {
            setCreateError(err.response?.data?.error || 'Failed to create event.');
        } finally {
            setCreateSaving(false);
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Campus Events</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('browse')}
                    >
                        Browse Events
                    </button>
                    <button
                        className={`btn ${activeTab === 'my_events' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('my_events')}
                    >
                        My Registrations
                    </button>
                    {(user.role === 'ADMIN' || user.role === 'FACULTY') && (
                        <button
                            className={`btn ${activeTab === 'manage' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setActiveTab('manage')}
                        >
                            Manage Attendance
                        </button>
                    )}
                    {(user.role === 'ADMIN' || user.role === 'FACULTY') && (
                        <button
                            className={`btn ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setActiveTab('create')}
                        >
                            + Create Event
                        </button>
                    )}
                </div>
            </div>

            {activeTab === 'browse' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
                    {events.length === 0 ? (
                        <p>No events found.</p>
                    ) : (
                        events.map(ev => (
                            <div key={ev.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column' }}>
                                <div style={{ flex: 1 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                        <h3>{ev.name}</h3>
                                        <span className={`badge ${ev.status === 'UPCOMING' ? 'badge-primary' : (ev.status === 'ONGOING' ? 'badge-success' : 'badge-secondary')}`}>
                                            {ev.status}
                                        </span>
                                    </div>
                                    <span className="badge badge-warning" style={{ marginTop: '5px' }}>{ev.eventType}</span>
                                    <p style={{ marginTop: '10px', color: '#666', minHeight: '50px' }}>{ev.description}</p>
                                    <div style={{ fontSize: '0.9em', color: '#888', marginBottom: '15px' }}>
                                        <strong>Location:</strong> {ev.location}<br />
                                        <strong>Starts:</strong> {new Date(ev.startTime).toLocaleString()}<br />
                                        <strong>Registered:</strong> {ev.registrationCount} / {ev.maxParticipants || 'Unlimited'}
                                    </div>
                                </div>
                                <button
                                    className="btn btn-primary"
                                    style={{ width: '100%' }}
                                    onClick={() => handleRegister(ev.id)}
                                    disabled={ev.status === 'COMPLETED'}
                                >
                                    {ev.status === 'COMPLETED' ? 'Event Ended' : 'Register Now'}
                                </button>
                            </div>
                        ))
                    )}
                </div>
            )}

            {activeTab === 'my_events' && (
                <div>
                    {myEvents.length === 0 ? (
                        <div className="stat-card">You have not registered for any events yet.</div>
                    ) : (
                        <div className="data-table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Event Name</th>
                                        <th>Type</th>
                                        <th>Date & Time</th>
                                        <th>Location</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {myEvents.map(ev => (
                                        <tr key={ev.id}>
                                            <td>{ev.name}</td>
                                            <td>{ev.eventType}</td>
                                            <td>{new Date(ev.startTime).toLocaleString()}</td>
                                            <td>{ev.location}</td>
                                            <td>
                                                <span className={`badge ${ev.status === 'COMPLETED' ? 'badge-secondary' : 'badge-primary'}`}>
                                                    {ev.status}
                                                </span>
                                            </td>
                                            <td>
                                                <button
                                                    className="btn btn-sm btn-danger"
                                                    onClick={() => handleUnregister(ev.id)}
                                                    disabled={ev.status === 'COMPLETED' || ev.status === 'ONGOING'}
                                                >
                                                    Cancel
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'manage' && (
                <div className="stat-card">
                    <div style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '15px' }}>
                        <label><strong>Select Event:</strong></label>
                        <select
                            value={selectedEventId}
                            onChange={e => setSelectedEventId(e.target.value)}
                            style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
                        >
                            <option value="">-- Select an Event --</option>
                            {events.map(ev => <option key={ev.id} value={ev.id}>{ev.name}</option>)}
                        </select>
                    </div>

                    {selectedEventId && (
                        <div className="data-table-container">
                            <h4>Registered Students</h4>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Student Name</th>
                                        <th>Registered At</th>
                                        <th>Attendance Status</th>
                                        <th>Update Attendance</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {eventRegistrations.length === 0 ? (
                                        <tr><td colSpan="4" style={{ textAlign: 'center' }}>No registrations yet.</td></tr>
                                    ) : (
                                        eventRegistrations.map(reg => (
                                            <tr key={reg.id}>
                                                <td>{reg.studentName}</td>
                                                <td>{new Date(reg.registeredAt).toLocaleString()}</td>
                                                <td>
                                                    <span className={`badge ${reg.attendanceStatus === 'ATTENDED' ? 'badge-success' : reg.attendanceStatus === 'ABSENT' ? 'badge-danger' : 'badge-warning'}`}>
                                                        {reg.attendanceStatus || 'PENDING'}
                                                    </span>
                                                </td>
                                                <td>
                                                    <select
                                                        value={reg.attendanceStatus || 'PENDING'}
                                                        onChange={(e) => handleMarkAttendance(reg.id, e.target.value)}
                                                        style={{ padding: '4px', borderRadius: '4px', border: '1px solid #ddd' }}
                                                    >
                                                        <option value="PENDING">PENDING</option>
                                                        <option value="ATTENDED">ATTENDED</option>
                                                        <option value="ABSENT">ABSENT</option>
                                                    </select>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}

            {/* ===== CREATE EVENT TAB ===== */}
            {activeTab === 'create' && (
                <div style={{ maxWidth: '700px' }}>
                    <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '12px', padding: '28px' }}>
                        <h3 style={{ marginTop: 0, marginBottom: '24px', color: '#2d3748' }}>🎪 Create New Event</h3>
                        {createError && <div className="alert alert-error" style={{ marginBottom: '16px' }}>{createError}</div>}
                        <form onSubmit={handleCreateEvent} className="form-grid">
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Event Name *</label>
                                <input type="text" required value={createForm.name} onChange={e => setCreateForm(p => ({ ...p, name: e.target.value }))} placeholder="e.g. Annual Tech Fest 2026" />
                            </div>
                            <div className="form-group">
                                <label>Event Type</label>
                                <select value={createForm.eventType} onChange={e => setCreateForm(p => ({ ...p, eventType: e.target.value }))}>
                                    {['Workshop', 'Seminar', 'Hackathon', 'Cultural', 'Sports', 'Academic', 'Guest Lecture', 'Other'].map(t => (
                                        <option key={t} value={t}>{t}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-group">
                                <label>Status</label>
                                <select value={createForm.status} onChange={e => setCreateForm(p => ({ ...p, status: e.target.value }))}>
                                    <option value="UPCOMING">Upcoming</option>
                                    <option value="ONGOING">Ongoing</option>
                                    <option value="COMPLETED">Completed</option>
                                </select>
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Description</label>
                                <textarea rows="3" value={createForm.description} onChange={e => setCreateForm(p => ({ ...p, description: e.target.value }))} placeholder="Describe this event…" style={{ resize: 'vertical' }} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Location / Venue</label>
                                <input type="text" value={createForm.location} onChange={e => setCreateForm(p => ({ ...p, location: e.target.value }))} placeholder="e.g. Main Auditorium, Block B" />
                            </div>
                            <div className="form-group">
                                <label>Start Date & Time *</label>
                                <input type="datetime-local" required value={createForm.startTime} onChange={e => setCreateForm(p => ({ ...p, startTime: e.target.value }))} />
                            </div>
                            <div className="form-group">
                                <label>End Date & Time</label>
                                <input type="datetime-local" value={createForm.endTime} onChange={e => setCreateForm(p => ({ ...p, endTime: e.target.value }))} />
                            </div>
                            <div className="form-group">
                                <label>Max Participants</label>
                                <input type="number" min="1" value={createForm.maxParticipants} onChange={e => setCreateForm(p => ({ ...p, maxParticipants: e.target.value }))} placeholder="Leave blank for unlimited" />
                            </div>
                            <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end', gap: '10px' }}>
                                <button type="submit" className="btn btn-primary" disabled={createSaving} style={{ flex: 1 }}>
                                    {createSaving ? 'Creating…' : '🎪 Create Event'}
                                </button>
                                <button type="button" className="btn btn-secondary" onClick={() => { setCreateForm(EMPTY_EVENT); setCreateError(''); }}>
                                    Reset
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default EventsPage;
