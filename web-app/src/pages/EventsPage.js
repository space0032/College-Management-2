import React, { useState, useEffect, useCallback } from 'react';
import {
    getEvents, registerEvent, getStudentEvents, unregisterEvent,
    getEventRegistrations, markAttendance, createEvent,
    getEventBudgets, addEventBudget, deleteEventBudget, updateBudgetActualCost,
    getEventPolls, createEventPoll, closeEventPoll, voteEventPoll,
    getEventCollaborators, addEventCollaborator, deleteEventCollaborator,
    getEventResources, addEventResource, updateEventResourceStatus, deleteEventResource,
    getEventVolunteers, registerEventVolunteer, updateEventVolunteer
} from '../services/eventService';
import { getDepartments } from '../services/departmentService';
import { getAllStudents } from '../services/studentService';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { toast } from '../components/Toast';
import { getErrorMessage, getSuccessRefId } from '../utils/error';
import { SkeletonCards } from '../components/Skeleton';
import SessionManager from '../utils/SessionManager';
import { safeParseFloat } from '../utils/validationUtils';

const EventsPage = () => {
    const [listLoading, setListLoading] = useState(true);
    const [listError, setListError] = useState('');
    const [createOpen, setCreateOpen] = useState(false);
    const [pendingUnregister, setPendingUnregister] = useState(null);
    const [activeTab, setActiveTab] = useState('browse');
    const [events, setEvents] = useState([]);
    const [myEvents, setMyEvents] = useState([]);


    // Management state
    const [selectedEventId, setSelectedEventId] = useState('');
    const [eventRegistrations, setEventRegistrations] = useState([]);
    const [budgets, setBudgets] = useState([]);
    const [polls, setPolls] = useState([]);
    const [collaborators, setCollaborators] = useState([]);
    const [resources, setResources] = useState([]);
    const [volunteers, setVolunteers] = useState([]);
    const [departmentOptions, setDepartmentOptions] = useState([]);
    const [manageMode, setManageMode] = useState('attendance'); // attendance, budget, polls, collaborators, resources, volunteers

    // Create event form
    const EMPTY_EVENT = { name: '', eventType: 'Workshop', description: '', location: '', startTime: '', endTime: '', maxParticipants: '', status: 'UPCOMING' };
    const [createForm, setCreateForm] = useState(EMPTY_EVENT);
    const [createSaving, setCreateSaving] = useState(false);

    // Modal state for Add Item
    const [itemModal, setItemModal] = useState({ open: false, type: '', title: '' });
    const [budgetForm, setBudgetForm] = useState({ item: '', estimatedCost: '', actualCost: '0', status: 'PLANNED' });
    const [pollForm, setPollForm] = useState({ question: '', options: '', status: 'ACTIVE' });
    const [collabForm, setCollabForm] = useState({ departmentId: '' });
    const [resourceForm, setResourceForm] = useState({ resourceName: '', quantity: 1 });
    const [volunteerForm, setVolunteerForm] = useState({ enrollmentId: '', task: '' });
    const [students, setStudents] = useState([]);

    const user = SessionManager.getUser() || {};
    const userRole = SessionManager.getUserRole() || 'STUDENT';
    const isAdmin = userRole === 'ADMIN' || userRole === 'FACULTY';

    const loadData = useCallback(async (signal) => {
        setListLoading(true);
        setListError('');
        try {
            const [evRes, myRes] = await Promise.all([
                getEvents(signal),
                user.role === 'STUDENT' ? getStudentEvents(user.username, signal) : Promise.resolve({ data: [] })
            ]);
            if (signal?.aborted) return;
            setEvents(evRes.data || []);
            setMyEvents(myRes.data || []);
            if (!selectedEventId && evRes.data?.length > 0) {
                setSelectedEventId(evRes.data[0].id.toString());
            }
        } catch (err) {
            if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
            setListError(err?.response?.data?.error || 'Could not load events.');
        } finally {
            if (!signal?.aborted) setListLoading(false);
        }
    }, [user.username, user.role, selectedEventId]);

    useEffect(() => {
        const controller = new AbortController();
        loadData(controller.signal);
        return () => controller.abort();
    }, [loadData]);

    useEffect(() => {
        getDepartments().then(res => setDepartmentOptions(res.data || [])).catch(() => {});
        getAllStudents().then(res => setStudents((res.data || []).map(s => ({ id: s.id, name: s.name, username: s.username })))).catch(() => {});
    }, []);

    const loadEventDetails = useCallback(async (eventId) => {
        if (!eventId) return;
        try {
            const [regRes, budRes, pollRes, collabRes, resRes, volRes] = await Promise.all([
                getEventRegistrations(eventId),
                getEventBudgets(eventId),
                getEventPolls(eventId),
                getEventCollaborators(eventId),
                getEventResources(eventId),
                getEventVolunteers(eventId)
            ]);
            setEventRegistrations(regRes.data || []);
            setBudgets(budRes.data || []);
            setPolls(pollRes.data || []);
            setCollaborators(collabRes.data || []);
            setResources(resRes.data || []);
            setVolunteers(volRes.data || []);
        } catch (err) {
            console.error(err);
        }
    }, []);

    useEffect(() => {
        if (selectedEventId) loadEventDetails(selectedEventId);
    }, [selectedEventId, loadEventDetails]);

    const handleRegister = async (eventId) => {
        try {
            const refId = getSuccessRefId();
            await registerEvent(eventId, user.username);
            toast.success('Registration request submitted.', { refId });
            loadData();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not register for this event.');
            toast.error(message, { refId, details: { status } });
        }
    };

    const confirmUnregister = async () => {
        if (!pendingUnregister) return;
        try {
            await unregisterEvent(pendingUnregister, user.username);
            setPendingUnregister(null);
            toast.success('Registration cancelled.', { refId: getSuccessRefId() });
            loadData();
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not cancel registration.');
            toast.error(message, { refId });
        }
    };

    const handleCreateEvent = async () => {
        setCreateSaving(true);
        try {
            const refId = getSuccessRefId();
            await createEvent(createForm);
            setCreateForm(EMPTY_EVENT);
            setCreateOpen(false);
            toast.success('Event created.', { refId });
            loadData();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not create this event.');
            toast.error(message, { refId, details: { status } });
        } finally {
            setCreateSaving(false);
        }
    };

    const fail = (err, fallback) => {
        const { message, status, refId } = getErrorMessage(err, fallback);
        toast.error(message, { refId, details: { status } });
    };
    const ok = (msg) => toast.success(msg, { refId: getSuccessRefId() });

    const handleSaveBudget = async () => {
        try {
            await addEventBudget(selectedEventId, {
                ...budgetForm,
                estimatedCost: safeParseFloat(budgetForm.estimatedCost),
                actualCost: safeParseFloat(budgetForm.actualCost)
            });
            loadEventDetails(selectedEventId);
            setItemModal({ open: false });
            ok('Budget item added.');
        } catch (err) { fail(err, 'Could not add this budget item.'); }
    };

    const handleSavePoll = async () => {
        try {
            await createEventPoll(selectedEventId, pollForm);
            loadEventDetails(selectedEventId);
            setItemModal({ open: false });
            ok('Poll created.');
        } catch (err) { fail(err, 'Could not create this poll.'); }
    };

    const handleVote = async (pollId, option) => {
        try {
            await voteEventPoll(pollId, { enrollmentId: user.username, option });
            loadEventDetails(selectedEventId);
        } catch (err) {
            fail(err, 'Could not record your vote.');
        }
    };

    const handleUpdateCost = async (budgetId, actualCost, status) => {
        try {
            await updateBudgetActualCost(budgetId, {
                actualCost: safeParseFloat(actualCost),
                status
            });
            loadEventDetails(selectedEventId);
        } catch (err) {
            fail(err, 'Could not update this cost.');
        }
    };

    const handleSaveCollaborator = async () => {
        if (!collabForm.departmentId) { toast.error('Select a department.'); return; }
        try {
            await addEventCollaborator(selectedEventId, { departmentId: parseInt(collabForm.departmentId) });
            setItemModal({ open: false });
            loadEventDetails(selectedEventId);
            ok('Collaborator added.');
        } catch (err) { fail(err, 'Could not add this collaborator.'); }
    };

    const handleDeleteCollaborator = async (id) => {
        try {
            await deleteEventCollaborator(id);
            loadEventDetails(selectedEventId);
        } catch (err) { fail(err, 'Could not remove this collaborator.'); }
    };

    const handleSaveResource = async () => {
        if (!resourceForm.resourceName.trim()) { toast.error('Enter a resource name.'); return; }
        try {
            await addEventResource(selectedEventId, {
                resourceName: resourceForm.resourceName,
                quantity: parseInt(resourceForm.quantity) || 1
            });
            setItemModal({ open: false });
            loadEventDetails(selectedEventId);
            ok('Resource added.');
        } catch (err) { fail(err, 'Could not add this resource.'); }
    };

    const handleUpdateResourceStatus = async (id, status, e) => {
        try {
            await updateEventResourceStatus(id, { status });
            loadEventDetails(selectedEventId);
        } catch (err) { fail(err, 'Could not update resource status.'); }
    };

    const handleDeleteResource = async (id) => {
        try {
            await deleteEventResource(id);
            loadEventDetails(selectedEventId);
        } catch (err) { fail(err, 'Could not remove this resource.'); }
    };

    const handleSaveVolunteer = async () => {
        if (!volunteerForm.enrollmentId || !volunteerForm.task.trim()) { toast.error('Student enrollment and task are required.'); return; }
        try {
            await registerEventVolunteer(selectedEventId, {
                enrollmentId: volunteerForm.enrollmentId,
                task: volunteerForm.task
            });
            setItemModal({ open: false });
            loadEventDetails(selectedEventId);
            ok('Volunteer registered.');
        } catch (err) { fail(err, 'Could not register this volunteer.'); }
    };

    const handleUpdateVolunteer = async (id, task, status) => {
        try {
            await updateEventVolunteer(id, { task, status });
            loadEventDetails(selectedEventId);
        } catch (err) { fail(err, 'Could not update this volunteer.'); }
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
                    {isAdmin && <button className="btn btn-primary" onClick={() => setCreateOpen(true)}>+ New Event</button>}
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

            {listError && (
                <div className="retry-bar" role="alert" style={{ marginBottom: '16px' }}>
                    <span>{listError}</span>
                    <button className="btn btn-secondary btn-sm" onClick={() => loadData()}>Retry</button>
                </div>
            )}
            {listLoading && activeTab === 'browse' ? (
                <SkeletonCards count={6} />
            ) : activeTab === 'browse' && (
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
                            { label: 'Actions', key: 'id', render: (_, row) => <button className="btn btn-sm btn-danger" onClick={() => setPendingUnregister(row.id)}>Leave</button> }
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
                            <button className={`btn-tab ${manageMode === 'collaborators' ? 'active' : ''}`} onClick={() => setManageMode('collaborators')}>🤝 Collaborators</button>
                            <button className={`btn-tab ${manageMode === 'resources' ? 'active' : ''}`} onClick={() => setManageMode('resources')}>📦 Resources</button>
                            <button className={`btn-tab ${manageMode === 'volunteers' ? 'active' : ''}`} onClick={() => setManageMode('volunteers')}>🙋 Volunteers</button>
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
                                        {
                                            label: 'Actual', key: 'actualCost',
                                            render: (v, row) => (
                                                <input
                                                    type="number"
                                                    defaultValue={v}
                                                    onBlur={(e) => handleUpdateCost(row.id, parseFloat(e.target.value), row.status)}
                                                    style={{ width: '80px', padding: '4px', borderRadius: '4px', border: '1px solid #e2e8f0' }}
                                                />
                                            )
                                        },
                                        {
                                            label: 'Status', key: 'status',
                                            render: (v, row) => (
                                                <select
                                                    value={v}
                                                    onChange={(e) => handleUpdateCost(row.id, row.actualCost, e.target.value)}
                                                    style={{ padding: '4px', borderRadius: '4px', border: '1px solid #e2e8f0' }}
                                                >
                                                    {['PLANNED', 'APPROVED', 'PAID', 'CANCELLED'].map(s => <option key={s} value={s}>{s}</option>)}
                                                </select>
                                            )
                                        },
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
                                            {p.options.split(',').map(opt => {
                                                const voteCount = p.results?.[opt] || 0;
                                                const totalVotes = Object.values(p.results || {}).reduce((a, b) => a + b, 0);
                                                const percentage = totalVotes > 0 ? Math.round((voteCount / totalVotes) * 100) : 0;

                                                return (
                                                    <div key={opt} style={{ marginBottom: '10px' }}>
                                                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px', fontSize: '0.9rem' }}>
                                                            <span>{opt}</span>
                                                            <span style={{ fontWeight: 'bold' }}>{voteCount} votes ({percentage}%)</span>
                                                        </div>
                                                        <div style={{ height: '8px', background: '#edf2f7', borderRadius: '4px', overflow: 'hidden', display: 'flex', alignItems: 'center', cursor: p.status === 'ACTIVE' ? 'pointer' : 'default' }} onClick={() => p.status === 'ACTIVE' && handleVote(p.id, opt)}>
                                                            <div style={{ width: `${percentage}%`, height: '100%', background: '#4299e1', transition: 'width 0.3s ease' }} />
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                        <div style={{ display: 'flex', gap: '8px', marginTop: '10px' }}>
                                            {p.status === 'ACTIVE' && (
                                                <button className="btn btn-sm btn-secondary" onClick={() => closeEventPoll(p.id).then(() => loadEventDetails(selectedEventId))}>Close Poll</button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        {manageMode === 'collaborators' && (
                            <div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                                    <h3>Departments Collaborating</h3>
                                    <button className="btn btn-sm btn-primary" onClick={() => { setCollabForm({ departmentId: '' }); setItemModal({ open: true, type: 'collaborator', title: 'Add Collaborating Department' }); }}>+ Add Department</button>
                                </div>
                                {collaborators.length === 0 ? (
                                    <p style={{ color: '#94a3b8' }}>No collaborating departments yet.</p>
                                ) : (
                                    <DataTable
                                        columns={[
                                            { label: 'Department', key: 'departmentName' },
                                            { label: 'Status', key: 'status', render: (v) => <span className={`badge ${v === 'ACCEPTED' ? 'badge-success' : v === 'DECLINED' ? 'badge-danger' : 'badge-secondary'}`}>{v}</span> },
                                            { label: 'Action', key: 'id', render: (v) => <button className="btn btn-sm btn-danger" onClick={() => handleDeleteCollaborator(v)}>Remove</button> }
                                        ]}
                                        data={collaborators}
                                    />
                                )}
                            </div>
                        )}

                        {manageMode === 'resources' && (
                            <div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                                    <h3>Event Resources</h3>
                                    <button className="btn btn-sm btn-primary" onClick={() => { setResourceForm({ resourceName: '', quantity: 1 }); setItemModal({ open: true, type: 'resource', title: 'Add Resource' }); }}>+ Add Resource</button>
                                </div>
                                {resources.length === 0 ? (
                                    <p style={{ color: '#94a3b8' }}>No resources requested yet.</p>
                                ) : (
                                    <DataTable
                                        columns={[
                                            { label: 'Resource', key: 'resourceName' },
                                            { label: 'Qty', key: 'quantity' },
                                            {
                                                label: 'Status', key: 'status',
                                                render: (v, row) => (
                                                    <select value={v} onChange={(e) => handleUpdateResourceStatus(row.id, e.target.value)} style={{ padding: '4px', borderRadius: '4px', border: '1px solid #e2e8f0' }}>
                                                        {['REQUESTED', 'APPROVED', 'DENIED'].map(s => <option key={s} value={s}>{s}</option>)}
                                                    </select>
                                                )
                                            },
                                            { label: 'Action', key: 'id', render: (v) => <button className="btn btn-sm btn-danger" onClick={() => handleDeleteResource(v)}>Remove</button> }
                                        ]}
                                        data={resources}
                                    />
                                )}
                            </div>
                        )}

                        {manageMode === 'volunteers' && (
                            <div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                                    <h3>Event Volunteers</h3>
                                    <button className="btn btn-sm btn-primary" onClick={() => { setVolunteerForm({ enrollmentId: '', task: '' }); setItemModal({ open: true, type: 'volunteer', title: 'Register Volunteer' }); }}>+ Add Volunteer</button>
                                </div>
                                {volunteers.length === 0 ? (
                                    <p style={{ color: '#94a3b8' }}>No volunteers registered for this event.</p>
                                ) : (
                                    <DataTable
                                        columns={[
                                            { label: 'Student', key: 'studentName' },
                                            { label: 'Task', key: 'taskDescription' },
                                            {
                                                label: 'Status', key: 'status',
                                                render: (v, row) => (
                                                    <select value={v} onChange={(e) => handleUpdateVolunteer(row.id, row.taskDescription, e.target.value)} style={{ padding: '4px', borderRadius: '4px', border: '1px solid #e2e8f0' }}>
                                                        {['REGISTERED', 'APPROVED', 'COMPLETED'].map(s => <option key={s} value={s}>{s}</option>)}
                                                    </select>
                                                )
                                            }
                                        ]}
                                        data={volunteers}
                                    />
                                )}
                            </div>
                        )}
                    </div>
                </div>
            )}

            <Modal
                isOpen={createOpen}
                title="🎪 Create Event"
                onClose={() => setCreateOpen(false)}
                onSubmit={handleCreateEvent}
                submitLabel="Create Event"
                submitting={createSaving}
                isDirty={Boolean(createForm.name || createForm.location || createForm.description)}
                size="large"
            >
                <form onSubmit={(e) => { e.preventDefault(); handleCreateEvent(); }} className="form-grid">
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Event Title *</label>
                        <input type="text" className="form-control" value={createForm.name} onChange={e => setCreateForm({ ...createForm, name: e.target.value })} required placeholder="e.g. Science Fair 2026" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Event Type</label>
                        <select className="form-control" value={createForm.eventType} onChange={e => setCreateForm({ ...createForm, eventType: e.target.value })}>
                            {['Workshop', 'Seminar', 'Hackathon', 'Cultural', 'Sports', 'Academic', 'Other'].map(t => <option key={t} value={t}>{t}</option>)}
                        </select>
                    </div>
                    <div className="form-group">
                        <label className="form-label">Location</label>
                        <input type="text" className="form-control" value={createForm.location} onChange={e => setCreateForm({ ...createForm, location: e.target.value })} placeholder="Main Auditorium" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Start Time *</label>
                        <input type="datetime-local" className="form-control" value={createForm.startTime} onChange={e => setCreateForm({ ...createForm, startTime: e.target.value })} required />
                    </div>
                    <div className="form-group">
                        <label className="form-label">End Time *</label>
                        <input type="datetime-local" className="form-control" min={createForm.startTime || undefined} value={createForm.endTime} onChange={e => setCreateForm({ ...createForm, endTime: e.target.value })} required />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Max Capacity</label>
                        <input type="number" className="form-control" min="1" value={createForm.maxParticipants} onChange={e => setCreateForm({ ...createForm, maxParticipants: e.target.value })} placeholder="Unlimited" />
                    </div>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Brief Description</label>
                        <textarea rows="3" className="form-control" value={createForm.description} onChange={e => setCreateForm({ ...createForm, description: e.target.value })} placeholder="What's this event about?" />
                    </div>
                </form>
            </Modal>
            <ConfirmDialog
                isOpen={Boolean(pendingUnregister)}
                title="Cancel registration?"
                message="You will be removed from this event."
                confirmLabel="Leave Event"
                destructive={false}
                onConfirm={confirmUnregister}
                onCancel={() => setPendingUnregister(null)}
            />

            {/* Support Modals */}
            <Modal
                isOpen={itemModal.open}
                title={itemModal.title}
                onClose={() => setItemModal({ open: false })}
                onSubmit={
                    itemModal.type === 'budget' ? handleSaveBudget
                        : itemModal.type === 'poll' ? handleSavePoll
                            : itemModal.type === 'collaborator' ? handleSaveCollaborator
                                : itemModal.type === 'resource' ? handleSaveResource
                                    : handleSaveVolunteer
                }
            >
                {itemModal.type === 'budget' && (
                    <>
                        <div className="form-group"><label>Line Item</label><input type="text" value={budgetForm.item} onChange={e => setBudgetForm({ ...budgetForm, item: e.target.value })} /></div>
                        <div className="form-group"><label>Est. Cost</label><input type="number" value={budgetForm.estimatedCost} onChange={e => setBudgetForm({ ...budgetForm, estimatedCost: parseFloat(e.target.value) })} /></div>
                    </>
                )}
                {itemModal.type === 'poll' && (
                    <>
                        <div className="form-group"><label>Question</label><input type="text" value={pollForm.question} onChange={e => setPollForm({ ...pollForm, question: e.target.value })} /></div>
                        <div className="form-group"><label>Options (comma separated)</label><input type="text" value={pollForm.options} onChange={e => setPollForm({ ...pollForm, options: e.target.value })} /></div>
                    </>
                )}
                {itemModal.type === 'collaborator' && (
                    <div className="form-group">
                        <label>Department</label>
                        <select value={collabForm.departmentId} onChange={e => setCollabForm({ ...collabForm, departmentId: e.target.value })}>
                            <option value="">Select department</option>
                            {departmentOptions.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                        </select>
                    </div>
                )}
                {itemModal.type === 'resource' && (
                    <>
                        <div className="form-group"><label>Resource Name</label><input type="text" value={resourceForm.resourceName} onChange={e => setResourceForm({ ...resourceForm, resourceName: e.target.value })} placeholder="e.g. Projector, PA System" /></div>
                        <div className="form-group"><label>Quantity</label><input type="number" min="1" value={resourceForm.quantity} onChange={e => setResourceForm({ ...resourceForm, quantity: e.target.value })} /></div>
                    </>
                )}
                {itemModal.type === 'volunteer' && (
                    <>
                        <div className="form-group"><label>Student Enrollment</label><select className="form-control" value={volunteerForm.enrollmentId} onChange={e => setVolunteerForm({ ...volunteerForm, enrollmentId: e.target.value })}><option value="">Select student / enrollment…</option>{students.map(s => <option key={s.id} value={s.username}>{s.name} ({s.username})</option>)}</select></div>
                        <div className="form-group"><label>Task Description</label><input type="text" value={volunteerForm.task} onChange={e => setVolunteerForm({ ...volunteerForm, task: e.target.value })} placeholder="e.g. Stage setup" /></div>
                    </>
                )}
            </Modal>
        </div>
    );
};

export default EventsPage;
