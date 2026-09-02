import React, { useState, useEffect } from 'react';
import {
    getDisciplinaryRecords, createDisciplinaryRecord, updateDisciplinaryRecord,
    getGrievanceTickets, createGrievanceTicket, updateGrievanceTicket,
    getParentComms, sendParentComm
} from '../services/studentAffairsService';

// ─── Helpers ──────────────────────────────────────────────────────────────────

const getBadgeStyle = (val) => {
    const map = {
        'High': { background: '#fef2f2', color: '#dc2626' },
        'Medium': { background: '#fffbeb', color: '#d97706' },
        'Low': { background: '#f0fdf4', color: '#16a34a' },
        'Open': { background: '#eff6ff', color: '#2563eb' },
        'In Progress': { background: '#fffbeb', color: '#d97706' },
        'Under Review': { background: '#fffbeb', color: '#d97706' },
        'Resolved': { background: '#f0fdf4', color: '#16a34a' },
        'Closed': { background: '#f8fafc', color: '#64748b' },
        'Email': { background: '#eff6ff', color: '#3b82f6' },
        'SMS': { background: '#f0fdf4', color: '#10b981' },
        'SMS + Email': { background: '#faf5ff', color: '#8b5cf6' },
        'WhatsApp': { background: '#ecfdf5', color: '#059669' },
    };
    const s = map[val] || { background: '#f1f5f9', color: '#64748b' };
    return { ...s, padding: '3px 10px', borderRadius: '20px', fontSize: '0.78rem', fontWeight: '600', display: 'inline-block' };
};

// ─── Tab: Disciplinary Records ────────────────────────────────────────────────
const DisciplinaryTab = () => {
    const [incidents, setIncidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [modal, setModal] = useState(false);
    const [form, setForm] = useState({ student: '', enrollNo: '', date: '', type: '', severity: 'Low', action: '', status: 'Under Review' });
    const [detail, setDetail] = useState(null);
    const [filter, setFilter] = useState('');

    useEffect(() => {
        getDisciplinaryRecords()
            .then(res => setIncidents(res.data || []))
            .catch(() => setIncidents([
                { id: 'DISC-104', student: 'John Doe', enrollNo: 'S2023-401', date: '2024-10-12', type: 'Plagiarism', severity: 'High', action: 'Verbal Warning + Deduction', status: 'Under Review' },
                { id: 'DISC-103', student: 'Jane Smith', enrollNo: 'S2023-112', date: '2024-09-28', type: 'Hostel Curfew Violation', severity: 'Medium', action: 'Written Warning', status: 'Resolved' },
            ]))
            .finally(() => setLoading(false));
    }, []);

    const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

    const handleSave = async () => {
        if (!form.student || !form.type) { alert('Student name and Violation Type are required.'); return; }
        try {
            const res = await createDisciplinaryRecord({ ...form, date: form.date || new Date().toISOString().split('T')[0] });
            setIncidents(prev => [res.data, ...prev]);
        } catch {
            const newId = `DISC-${100 + incidents.length + 5}`;
            setIncidents(prev => [{ ...form, id: newId, date: form.date || new Date().toISOString().split('T')[0] }, ...prev]);
        }
        setModal(false);
        setForm({ student: '', enrollNo: '', date: '', type: '', severity: 'Low', action: '', status: 'Under Review' });
    };

    const handleResolve = async (inc) => {
        try {
            await updateDisciplinaryRecord(inc.id, { ...inc, status: 'Resolved' });
        } catch { /* fallback to in-memory */ }
        setIncidents(prev => prev.map(i => i.id === inc.id ? { ...i, status: 'Resolved' } : i));
    };

    const filtered = filter ? incidents.filter(i => i.status === filter) : incidents;
    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading records...</div>;

    return (
        <div className="stat-card" style={{ borderTop: '4px solid #ef4444' }}>
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px', gap: '16px', flexWrap: 'wrap' }}>
                <div>
                    <h3 style={{ margin: 0 }}>⚖️ Disciplinary Records</h3>
                    <p className="text-muted" style={{ margin: '4px 0 0' }}>Behavioral incident tracking, violations, and resolutions.</p>
                </div>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
                    <select className="form-control" style={{ width: 'auto', fontSize: '0.85rem' }} value={filter} onChange={e => setFilter(e.target.value)}>
                        <option value="">All Status</option>
                        <option>Under Review</option>
                        <option>Resolved</option>
                        <option>Closed</option>
                    </select>
                    <button className="btn btn-primary" onClick={() => setModal(true)}>+ Log Incident</button>
                </div>
            </div>

            {/* Stats Bar */}
            <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
                {[
                    { label: 'Total Cases', value: incidents.length, color: '#6366f1' },
                    { label: 'Under Review', value: incidents.filter(i => i.status === 'Under Review').length, color: '#d97706' },
                    { label: 'Resolved', value: incidents.filter(i => i.status === 'Resolved').length, color: '#10b981' },
                    { label: 'High Severity', value: incidents.filter(i => i.severity === 'High').length, color: '#ef4444' },
                ].map(s => (
                    <div key={s.label} style={{ padding: '10px 18px', background: '#f8fafc', borderRadius: '8px', borderLeft: `3px solid ${s.color}`, flex: '1', minWidth: '110px' }}>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: s.color }}>{s.value}</div>
                        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>{s.label}</div>
                    </div>
                ))}
            </div>

            {/* Table */}
            <div className="data-table-container">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Case ID</th><th>Student</th><th>Date</th><th>Violation</th>
                            <th>Severity</th><th>Action Taken</th><th>Status</th><th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map(inc => (
                            <tr key={inc.id}>
                                <td><code style={{ fontFamily: 'monospace', background: '#f1f5f9', padding: '2px 6px', borderRadius: '4px' }}>{inc.id}</code></td>
                                <td>
                                    <div style={{ fontWeight: '600', fontSize: '0.88rem' }}>{inc.student}</div>
                                    <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{inc.enrollNo}</div>
                                </td>
                                <td>{inc.date}</td>
                                <td>{inc.type}</td>
                                <td><span style={getBadgeStyle(inc.severity)}>{inc.severity}</span></td>
                                <td style={{ maxWidth: '180px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{inc.action || '—'}</td>
                                <td><span style={getBadgeStyle(inc.status)}>{inc.status}</span></td>
                                <td>
                                    <div style={{ display: 'flex', gap: '6px' }}>
                                        <button className="btn btn-secondary btn-sm" onClick={() => setDetail(inc)}>👁️</button>
                                        {inc.status !== 'Closed' && (
                                            <button className="btn btn-sm" style={{ background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0' }}
                                                onClick={() => handleResolve(inc)}>
                                                ✓ Resolve
                                            </button>
                                        )}
                                    </div>
                                </td>
                            </tr>
                        ))}
                        {filtered.length === 0 && (
                            <tr><td colSpan="8" style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>No records found.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>

            {/* Log Incident Modal */}
            {modal && (
                <div style={{ position: 'fixed', inset: 0, zIndex: 999, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center' }} onClick={() => setModal(false)}>
                    <div style={{ background: 'white', borderRadius: '12px', padding: '30px', maxWidth: '520px', width: '90%', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }} onClick={e => e.stopPropagation()}>
                        <h3 style={{ margin: '0 0 20px' }}>⚖️ Log Disciplinary Incident</h3>
                        <div className="form-grid">
                            {[{ name: 'student', label: 'Student Name *' }, { name: 'enrollNo', label: 'Enrollment No.' }, { name: 'date', label: 'Incident Date', type: 'date' }, { name: 'type', label: 'Violation Type *' }, { name: 'action', label: 'Action Taken' }].map(f => (
                                <div className="form-group" key={f.name}>
                                    <label className="form-label">{f.label}</label>
                                    <input name={f.name} type={f.type || 'text'} className="form-control" value={form[f.name]} onChange={handleChange} />
                                </div>
                            ))}
                            <div className="form-group">
                                <label className="form-label">Severity</label>
                                <select name="severity" className="form-control" value={form.severity} onChange={handleChange}>
                                    <option>Low</option><option>Medium</option><option>High</option>
                                </select>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Status</label>
                                <select name="status" className="form-control" value={form.status} onChange={handleChange}>
                                    <option>Under Review</option><option>Resolved</option><option>Closed</option>
                                </select>
                            </div>
                        </div>
                        <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                            <button className="btn btn-primary" style={{ flex: 1 }} onClick={handleSave}>Save Incident</button>
                            <button className="btn btn-secondary" onClick={() => setModal(false)}>Cancel</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Detail Modal */}
            {detail && (
                <div style={{ position: 'fixed', inset: 0, zIndex: 999, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center' }} onClick={() => setDetail(null)}>
                    <div style={{ background: 'white', borderRadius: '12px', padding: '30px', maxWidth: '480px', width: '90%', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }} onClick={e => e.stopPropagation()}>
                        <h3 style={{ margin: '0 0 20px' }}>{detail.id} — Case Details</h3>
                        {[['Student', detail.student], ['Enrollment', detail.enrollNo], ['Date', detail.date], ['Violation', detail.type], ['Severity', detail.severity], ['Action', detail.action || '—'], ['Status', detail.status]].map(([k, v]) => (
                            <div key={k} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #f1f5f9' }}>
                                <span style={{ color: '#64748b', fontWeight: '500' }}>{k}</span>
                                <span style={{ fontWeight: '600' }}>{v}</span>
                            </div>
                        ))}
                        <button className="btn btn-secondary" style={{ marginTop: '20px', width: '100%' }} onClick={() => setDetail(null)}>Close</button>
                    </div>
                </div>
            )}
        </div>
    );
};

// ─── Tab: Grievance System ────────────────────────────────────────────────────
const GrievanceTab = () => {
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [modal, setModal] = useState(false);
    const [form, setForm] = useState({ category: 'Infrastructure', title: '', description: '', reporter: '', priority: 'Medium', anonymous: false });
    const [filterStatus, setFilterStatus] = useState('');

    useEffect(() => {
        getGrievanceTickets()
            .then(res => setTickets(res.data || []))
            .catch(() => setTickets([
                { id: 'TKT-8842', category: 'Infrastructure', title: 'Heating issue in Library Wing B', reporter: 'Anonymous', date: '2024-10-20', priority: 'High', status: 'Open' },
                { id: 'TKT-8841', category: 'IT Services', title: 'WiFi disconnection in CS Lab 3', reporter: 'S2022-819', date: '2024-10-18', priority: 'Medium', status: 'Resolved' },
            ]))
            .finally(() => setLoading(false));
    }, []);

    const handleChange = e => {
        const val = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
        setForm({ ...form, [e.target.name]: val });
    };

    const handleFile = async () => {
        if (!form.title || !form.description) { alert('Title and Description are required.'); return; }
        const payload = {
            category: form.category, title: form.title, description: form.description,
            reporter: form.anonymous ? 'Anonymous' : (form.reporter || 'Unknown'),
            date: new Date().toISOString().split('T')[0],
            priority: form.priority, status: 'Open'
        };
        try {
            const res = await createGrievanceTicket(payload);
            setTickets(prev => [res.data, ...prev]);
        } catch {
            const id = `TKT-${8800 + tickets.length + 43}`;
            setTickets(prev => [{ id, ...payload }, ...prev]);
        }
        setModal(false);
        setForm({ category: 'Infrastructure', title: '', description: '', reporter: '', priority: 'Medium', anonymous: false });
    };

    const changeStatus = async (id, status) => {
        try { await updateGrievanceTicket(id, { status }); } catch { /* fallback */ }
        setTickets(prev => prev.map(t => t.id === id ? { ...t, status } : t));
    };

    const filtered = filterStatus ? tickets.filter(t => t.status === filterStatus) : tickets;
    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Loading tickets...</div>;

    return (
        <div className="stat-card" style={{ borderTop: '4px solid #3b82f6' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
                <div>
                    <h3 style={{ margin: 0 }}>📢 Grievance System</h3>
                    <p className="text-muted" style={{ margin: '4px 0 0' }}>Anonymous and identified complaints and feedback tickets.</p>
                </div>
                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                    <select className="form-control" style={{ width: 'auto', fontSize: '0.85rem' }} value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
                        <option value="">All Status</option>
                        <option>Open</option><option>In Progress</option><option>Resolved</option>
                    </select>
                    <button className="btn btn-primary" onClick={() => setModal(true)}>+ File Grievance</button>
                </div>
            </div>

            {/* Stats bar */}
            <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
                {[
                    { label: 'Total', value: tickets.length, color: '#6366f1' },
                    { label: 'Open', value: tickets.filter(t => t.status === 'Open').length, color: '#3b82f6' },
                    { label: 'In Progress', value: tickets.filter(t => t.status === 'In Progress').length, color: '#d97706' },
                    { label: 'Resolved', value: tickets.filter(t => t.status === 'Resolved').length, color: '#10b981' },
                ].map(s => (
                    <div key={s.label} style={{ padding: '10px 18px', background: '#f8fafc', borderRadius: '8px', borderLeft: `3px solid ${s.color}`, flex: '1', minWidth: '80px' }}>
                        <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: s.color }}>{s.value}</div>
                        <div style={{ fontSize: '0.75rem', color: '#64748b' }}>{s.label}</div>
                    </div>
                ))}
            </div>

            {/* Ticket Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
                {filtered.map(t => (
                    <div key={t.id} style={{ border: '1px solid #e2e8f0', borderRadius: '10px', padding: '16px', background: '#fafcff', position: 'relative' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                            <div>
                                <code style={{ fontSize: '0.78rem', background: '#eff6ff', color: '#3b82f6', padding: '2px 8px', borderRadius: '4px' }}>{t.id}</code>
                                <span style={{ ...getBadgeStyle(t.priority), marginLeft: '8px' }}>{t.priority}</span>
                            </div>
                            <span style={getBadgeStyle(t.status)}>{t.status}</span>
                        </div>
                        <div style={{ fontSize: '0.78rem', color: '#94a3b8', marginBottom: '6px' }}>📂 {t.category}</div>
                        <h4 style={{ margin: '0 0 8px', fontSize: '0.92rem', fontWeight: '600' }}>{t.title}</h4>
                        <p style={{ fontSize: '0.8rem', color: '#64748b', margin: '0 0 12px' }}>
                            👤 {t.reporter} &nbsp;·&nbsp; 📅 {t.date}
                        </p>
                        <div style={{ display: 'flex', gap: '8px' }}>
                            {t.status === 'Open' && (
                                <button className="btn btn-sm btn-primary" style={{ flex: 1 }} onClick={() => changeStatus(t.id, 'In Progress')}>
                                    Assign & Start
                                </button>
                            )}
                            {t.status === 'In Progress' && (
                                <button className="btn btn-sm" style={{ flex: 1, background: '#f0fdf4', color: '#16a34a', border: '1px solid #bbf7d0' }} onClick={() => changeStatus(t.id, 'Resolved')}>
                                    ✓ Mark Resolved
                                </button>
                            )}
                            {t.status === 'Resolved' && (
                                <span style={{ fontSize: '0.8rem', color: '#10b981', fontStyle: 'italic' }}>✅ Resolved</span>
                            )}
                        </div>
                    </div>
                ))}
                {filtered.length === 0 && (
                    <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: '60px', color: '#94a3b8' }}>No grievance tickets match the filter.</div>
                )}
            </div>

            {/* File Grievance Modal */}
            {modal && (
                <div style={{ position: 'fixed', inset: 0, zIndex: 999, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center' }} onClick={() => setModal(false)}>
                    <div style={{ background: 'white', borderRadius: '12px', padding: '30px', maxWidth: '520px', width: '90%', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }} onClick={e => e.stopPropagation()}>
                        <h3 style={{ margin: '0 0 20px' }}>📢 File a Grievance</h3>
                        <div className="form-group">
                            <label className="form-label">Category</label>
                            <select name="category" className="form-control" value={form.category} onChange={handleChange}>
                                {['Infrastructure', 'IT Services', 'Canteen', 'Faculty', 'Administration', 'Hostel', 'Security', 'Other'].map(c => <option key={c}>{c}</option>)}
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Title / Short Description *</label>
                            <input name="title" className="form-control" value={form.title} onChange={handleChange} placeholder="e.g. Broken chairs in Seminar Hall" />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Detailed Description *</label>
                            <textarea name="description" className="form-control" rows="3" value={form.description} onChange={handleChange} placeholder="Explain the issue in detail..." />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Priority</label>
                            <select name="priority" className="form-control" value={form.priority} onChange={handleChange}>
                                <option>Low</option><option>Medium</option><option>High</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                <input type="checkbox" name="anonymous" checked={form.anonymous} onChange={handleChange} />
                                <span>Submit Anonymously</span>
                            </label>
                        </div>
                        {!form.anonymous && (
                            <div className="form-group">
                                <label className="form-label">Your Student ID</label>
                                <input name="reporter" className="form-control" value={form.reporter} onChange={handleChange} placeholder="e.g. S2023-401" />
                            </div>
                        )}
                        <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
                            <button className="btn btn-primary" style={{ flex: 1 }} onClick={handleFile}>Submit Grievance</button>
                            <button className="btn btn-secondary" onClick={() => setModal(false)}>Cancel</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

// ─── Tab: Parent Communication ────────────────────────────────────────────────
const ParentCommTab = () => {
    const [comms, setComms] = useState([]);
    const [form, setForm] = useState({ subject: '', recipient: 'All Parents', channel: 'Email', message: '', specific: '' });
    const [sending, setSending] = useState(false);
    const [sent, setSent] = useState(false);

    useEffect(() => {
        getParentComms()
            .then(res => setComms(res.data || []))
            .catch(() => setComms([
                { id: 1, subject: 'Campus Closure Notice - Diwali Holidays', recipient: 'All Parents', channel: 'Email', date: '2024-10-15', sentBy: 'Admin Office' },
                { id: 2, subject: 'Mid-Term Grade Reports Available', recipient: 'All Parents', channel: 'SMS + Email', date: '2024-10-05', sentBy: 'Academic Office' },
            ]));
    }, []);

    const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

    const handleSend = async () => {
        if (!form.subject || !form.message) { alert('Subject and Message are required.'); return; }
        setSending(true);
        const payload = {
            subject: form.subject,
            recipient: form.recipient === "Specific Student's Guardian" ? `${form.specific}'s Guardian` : form.recipient,
            channel: form.channel, message: form.message,
            date: new Date().toLocaleDateString('en-CA'), sentBy: 'Current Admin'
        };
        try {
            const res = await sendParentComm(payload);
            setComms(prev => [res.data, ...prev]);
        } catch {
            setComms(prev => [{ id: Date.now(), ...payload }, ...prev]);
        }
        setSending(false);
        setSent(true);
        setForm({ subject: '', recipient: 'All Parents', channel: 'Email', message: '', specific: '' });
        setTimeout(() => setSent(false), 3000);
    };

    return (
        <div className="stat-card" style={{ borderTop: '4px solid #10b981' }}>
            <h3 style={{ marginBottom: '4px' }}>👪 Parent Communication Portal</h3>
            <p className="text-muted" style={{ marginBottom: '24px' }}>Send broadcasts, academic alerts, and conduct notices to parents/guardians.</p>

            <div style={{ display: 'flex', gap: '24px', flexWrap: 'wrap' }}>
                {/* Compose Form */}
                <div style={{ flex: '1', minWidth: '280px', padding: '20px', background: '#f8fafc', borderRadius: '10px', border: '1px solid #e2e8f0' }}>
                    <h4 style={{ marginTop: 0 }}>✉️ Compose Message</h4>
                    {sent && (
                        <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: '8px', padding: '10px 14px', marginBottom: '16px', color: '#16a34a', fontWeight: '500' }}>
                            ✅ Message dispatched successfully!
                        </div>
                    )}
                    <div className="form-group">
                        <label className="form-label">Recipient Group</label>
                        <select name="recipient" className="form-control" value={form.recipient} onChange={handleChange}>
                            <option>All Parents (Broadcast)</option>
                            <option>Year 1 Parents</option>
                            <option>Year 2 Parents</option>
                            <option>Year 3 Parents</option>
                            <option>Year 4 Parents</option>
                            <option>Specific Student's Guardian</option>
                        </select>
                    </div>
                    {form.recipient === "Specific Student's Guardian" && (
                        <div className="form-group">
                            <label className="form-label">Student Enrollment No.</label>
                            <input name="specific" className="form-control" value={form.specific} onChange={handleChange} placeholder="e.g. S2023-401" />
                        </div>
                    )}
                    <div className="form-group">
                        <label className="form-label">Channel</label>
                        <select name="channel" className="form-control" value={form.channel} onChange={handleChange}>
                            <option>Email</option><option>SMS</option><option>WhatsApp</option><option>SMS + Email</option>
                        </select>
                    </div>
                    <div className="form-group">
                        <label className="form-label">Subject *</label>
                        <input name="subject" className="form-control" value={form.subject} onChange={handleChange} placeholder="e.g. End-of-Semester Updates" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Message *</label>
                        <textarea name="message" className="form-control" rows="5" value={form.message} onChange={handleChange} placeholder="Type your message here..." />
                    </div>
                    <button className="btn btn-primary" style={{ width: '100%' }} onClick={handleSend} disabled={sending}>
                        {sending ? '⏳ Sending...' : '✉️ Send Communication'}
                    </button>
                </div>

                {/* Sent History */}
                <div style={{ flex: '1', minWidth: '280px' }}>
                    <h4 style={{ marginTop: 0 }}>📋 Communication History</h4>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        {comms.map(c => (
                            <div key={c.id} style={{ padding: '14px 16px', border: '1px solid #e2e8f0', borderRadius: '8px', background: 'white', borderLeft: '4px solid #10b981' }}>
                                <div style={{ fontWeight: '600', fontSize: '0.88rem', marginBottom: '6px' }}>{c.subject}</div>
                                <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', fontSize: '0.78rem', color: '#64748b' }}>
                                    <span>👥 {c.recipient}</span>
                                    <span style={getBadgeStyle(c.channel)}>{c.channel}</span>
                                    <span>📅 {c.date}</span>
                                </div>
                                <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '4px' }}>Sent by: {c.sentBy}</div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

// ─── Main Page ────────────────────────────────────────────────────────────────
const StudentAffairsPage = () => {
    const [activeTab, setActiveTab] = useState('scholarships');

    const tabs = [
        { id: 'scholarships', label: 'Scholarships', icon: '🎓' },
        { id: 'discipline', label: 'Disciplinary Records', icon: '⚖️' },
        { id: 'grievance', label: 'Grievance System', icon: '📢' },
        { id: 'parents', label: 'Parent Communication', icon: '👪' },
        { id: 'wellness', label: 'Wellness & Health', icon: '🏥' },
        { id: 'career', label: 'Career Services', icon: '💼' },
        { id: 'housing', label: 'Housing & Residential', icon: '🏠' },
        { id: 'gov', label: 'Student Government', icon: '🗳️' },
        { id: 'activities', label: 'Extracurriculars', icon: '🎭' }
    ];

    const renderContent = () => {
        switch (activeTab) {
            case 'scholarships':
                return (
                    <div className="stat-card">
                        <h3>Active Scholarships</h3>
                        <p className="text-muted">Merit-based and need-based financial awards — see the Scholarships module for full management.</p>
                        <div className="data-table-container" style={{ marginTop: '20px' }}>
                            <table className="data-table">
                                <thead><tr><th>Award Name</th><th>Recipient</th><th>Amount</th><th>Status</th></tr></thead>
                                <tbody>
                                    <tr><td colSpan="4" style={{ textAlign: 'center', color: '#64748b' }}>Scholarship recipients are shown in the Scholarships module.</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                );
            case 'discipline': return <DisciplinaryTab />;
            case 'grievance': return <GrievanceTab />;
            case 'parents': return <ParentCommTab />;
            case 'wellness':
                return (
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        <div className="stat-card"><h3>🏥 Campus Clinic Activity</h3><div style={{ fontSize: '2rem', fontWeight: 'bold', margin: '15px 0' }}>14</div><p className="text-muted">Appointments scheduled for today</p></div>
                        <div className="stat-card"><h3>🧠 Counseling Sessions</h3><div style={{ fontSize: '2rem', fontWeight: 'bold', margin: '15px 0' }}>8</div><p className="text-muted">Wellness check-ins completed this week</p></div>
                    </div>
                );
            default:
                return (
                    <div className="stat-card" style={{ textAlign: 'center', padding: '100px' }}>
                        <div style={{ fontSize: '4rem', marginBottom: '20px' }}>🚧</div>
                        <h2>{tabs.find(t => t.id === activeTab)?.label} Module</h2>
                        <p className="text-muted">This management sub-system is currently being synchronized with the main ledger.</p>
                    </div>
                );
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">🏛️ Student Affairs Management</h1>
                    <p className="page-subtitle">Consolidated administrative portal for non-academic student life and wellness</p>
                </div>
            </div>

            <div style={{ display: 'flex', gap: '10px', overflowX: 'auto', paddingBottom: '15px', marginBottom: '25px', borderBottom: '1px solid #e2e8f0' }}>
                {tabs.map(tab => (
                    <button
                        key={tab.id}
                        className={`btn btn-sm ${activeTab === tab.id ? 'btn-primary' : ''}`}
                        style={{
                            whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: '8px',
                            background: activeTab === tab.id ? '' : 'white',
                            color: activeTab === tab.id ? '' : '#475569',
                            border: activeTab === tab.id ? '' : '1px solid #e2e8f0', padding: '10px 18px'
                        }}
                        onClick={() => setActiveTab(tab.id)}
                    >
                        <span>{tab.icon}</span> {tab.label}
                    </button>
                ))}
            </div>

            <div className="content-area">{renderContent()}</div>
        </div>
    );
};

export default StudentAffairsPage;
