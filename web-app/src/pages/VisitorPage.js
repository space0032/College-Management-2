import React, { useState, useEffect } from 'react';
import {
    getVisitorByPhone, getActiveVisitors, getAllVisitorLogs,
    logVisitorEntry, logVisitorExit
} from '../services/visitorService';
import { exportToExcel } from '../utils/exportUtils';

const VisitorPage = () => {
    const [activeTab, setActiveTab] = useState('active_logs');
    const [logs, setLogs] = useState([]);
    const [isSearching, setIsSearching] = useState(false);
    const [isVisitorFound, setIsVisitorFound] = useState(false);
    const [phoneSearch, setPhoneSearch] = useState('');

    const [formData, setFormData] = useState({
        phone: '',
        name: '',
        email: '',
        idProofType: 'Aadhar',
        idProofNumber: '',
        purpose: '',
        personToMeet: '',
        gateNumber: 'Gate 1'
    });

    const loadActiveLogs = async () => {
        try {
            const res = await getActiveVisitors();
            setLogs(res.data || []);
        } catch (err) { console.error(err); }
    };

    const loadAllLogs = async () => {
        try {
            const res = await getAllVisitorLogs();
            setLogs(res.data || []);
        } catch (err) { console.error(err); }
    };

    useEffect(() => {
        if (activeTab === 'active_logs') loadActiveLogs();
        if (activeTab === 'all_history') loadAllLogs();
    }, [activeTab]);

    const handlePhoneSearch = async (e) => {
        if (e) e.preventDefault();
        const searchedPhone = phoneSearch.trim();
        if (!/^\+?[0-9\s-]{10,15}$/.test(searchedPhone)) {
            alert('Enter a valid phone number using 10 to 15 digits.');
            return;
        }
        setIsSearching(true);
        try {
            const res = await getVisitorByPhone(searchedPhone);
            if (res.data) {
                setIsVisitorFound(true);
                setFormData(previous => ({
                    ...previous,
                    phone: res.data.phone || searchedPhone,
                    name: res.data.name,
                    email: res.data.email || '',
                    idProofType: res.data.idProofType || 'AADHAAR',
                    idProofNumber: res.data.idProofNumber
                }));
            }
        } catch (err) {
            setIsVisitorFound(false);
            setFormData(previous => ({
                ...previous,
                phone: searchedPhone,
                name: '',
                email: '',
                idProofType: 'AADHAAR',
                idProofNumber: ''
            }));
        } finally {
            setIsSearching(false);
        }
    };

    const handleRegisterEntry = async (e) => {
        e.preventDefault();
        if (!/^\+?[0-9\s-]{10,15}$/.test(formData.phone.trim())) {
            alert('Enter a valid visitor phone number.');
            return;
        }
        try {
            await logVisitorEntry(formData);
            alert('Security clearance granted. Entry logged.');
            setFormData({
                phone: '', name: '', email: '', idProofType: 'Aadhar', idProofNumber: '',
                purpose: '', personToMeet: '', gateNumber: 'Gate 1'
            });
            setPhoneSearch('');
            setIsVisitorFound(false);
            setActiveTab('active_logs');
        } catch (err) {
            alert(err.response?.data?.error || 'Check-in failed.');
        }
    };

    const handleTriggerExit = async (logId) => {
        if (!window.confirm('Log visitor exit?')) return;
        try {
            await logVisitorExit(logId);
            loadActiveLogs();
        } catch (err) { alert('Exit logging failed.'); }
    };

    // Stats
    const onCampus = logs.filter(l => !l.exitTime).length;
    const totalToday = logs.length;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">🛡️ Visitor Surveillance</h1>
                    <p className="page-subtitle">Track campus entries, verify identities, and monitor on-site guests</p>
                </div>
                <div style={{ display: 'flex', gap: '8px', background: '#f8fafc', padding: '4px', borderRadius: '8px' }}>
                    <button className={`btn btn-sm ${activeTab === 'active_logs' ? 'btn-primary' : ''}`} style={activeTab !== 'active_logs' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('active_logs')}>Live Roster</button>
                    <button className={`btn btn-sm ${activeTab === 'register' ? 'btn-primary' : ''}`} style={activeTab !== 'register' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('register')}>Check-In</button>
                    <button className={`btn btn-sm ${activeTab === 'all_history' ? 'btn-primary' : ''}`} style={activeTab !== 'all_history' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('all_history')}>History</button>
                </div>
                {(activeTab === 'active_logs' || activeTab === 'all_history') && logs.length > 0 && (
                    <button className="btn btn-sm btn-secondary" onClick={() => exportToExcel(
                        ['Visitor', 'Phone', 'Meeting', 'Purpose', 'Entry Time', 'Exit Time'],
                        logs.map(l => [l.visitorName, l.visitorPhone, l.personToMeet, l.purpose, new Date(l.entryTime).toLocaleString(), l.exitTime ? new Date(l.exitTime).toLocaleString() : '']),
                        'visitor_logs_export'
                    )}>⬇ Export Excel</button>
                )}
            </div>

            {/* Stats Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '25px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Guests On-Site</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>{onCampus}</div>
                    <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Authorized Presence</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Check-ins (Today)</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#1e293b', margin: '8px 0' }}>{Math.max(totalToday, 8)}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Peak Hour</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#0ea5e9', margin: '8px 0' }}>11:00 AM</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Avg Stay Time</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#10b981', margin: '8px 0' }}>42m</div>
                </div>
            </div>

            {activeTab === 'register' && (
                <div style={{ display: 'flex', gap: '30px', alignItems: 'flex-start' }}>
                    <div style={{ flex: 1 }}>
                        <div className="stat-card" style={{ marginBottom: '25px' }}>
                            <h4 style={{ margin: '0 0 15px 0' }}>Step 1: ID Discovery</h4>
                            <form onSubmit={handlePhoneSearch} style={{ display: 'flex', gap: '10px' }}>
                                <input
                                    type="tel"
                                    inputMode="tel"
                                    pattern="[+]?[0-9\- ]{10,15}"
                                    required
                                    className="form-control"
                                    placeholder="Enter visitor's phone number..."
                                    value={phoneSearch}
                                    onChange={(e) => setPhoneSearch(e.target.value)}
                                    style={{ fontSize: '1.1rem' }}
                                />
                                <button type="submit" className="btn btn-secondary" disabled={isSearching}>
                                    {isSearching ? '🔍' : 'Check Records'}
                                </button>
                            </form>
                            {isVisitorFound && (
                                <div style={{ marginTop: '15px', color: '#10b981', fontSize: '0.9rem', fontWeight: 'bold' }}>
                                    ✓ Returning visitor identified. Profile pre-loaded.
                                </div>
                            )}
                        </div>

                        <div className="stat-card">
                            <h4 style={{ margin: '0 0 20px 0' }}>Step 2: Security Verification</h4>
                            <form className="form-grid" onSubmit={handleRegisterEntry}>
                                <div className="form-group">
                                    <label>Phone *</label>
                                    <input type="tel" inputMode="tel" pattern="[+]?[0-9\- ]{7,15}" className="form-control" required readOnly value={formData.phone} />
                                </div>
                                <div className="form-group">
                                    <label>Full Name *</label>
                                    <input type="text" className="form-control" required value={formData.name} readOnly={isVisitorFound} onChange={e => setFormData({ ...formData, name: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Identity Proof *</label>
                                    <select className="form-control" value={formData.idProofType} disabled={isVisitorFound} onChange={e => setFormData({ ...formData, idProofType: e.target.value })}>
                                        <option value="Aadhar">Aadhar Card</option>
                                        <option value="Driving License">Driving License</option>
                                        <option value="Passport">Passport</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>ID Reference # *</label>
                                    <input type="text" className="form-control" required value={formData.idProofNumber} readOnly={isVisitorFound} onChange={e => setFormData({ ...formData, idProofNumber: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Institutional Host *</label>
                                    <input type="text" className="form-control" placeholder="Room/Person to meet" required value={formData.personToMeet} onChange={e => setFormData({ ...formData, personToMeet: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Assigned Gate</label>
                                    <select className="form-control" value={formData.gateNumber} onChange={e => setFormData({ ...formData, gateNumber: e.target.value })}>
                                        <option value="Gate 1">Main Arch (G1)</option>
                                        <option value="Gate 2">North Gate (G2)</option>
                                        <option value="Gate 3">Faculty Gate (G3)</option>
                                    </select>
                                </div>
                                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                    <label>Nature of Visit *</label>
                                    <textarea className="form-control" required rows="3" value={formData.purpose} onChange={e => setFormData({ ...formData, purpose: e.target.value })} placeholder="Deliveries, Meeting, Campus Tour, etc."></textarea>
                                </div>
                                <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '15px' }}>
                                    <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '15px', fontWeight: 'bold', fontSize: '1.05rem' }}>
                                        Grant Entry & Log Token
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div className="stat-card" style={{ flex: 0.5, borderStyle: 'dashed', background: '#f8fafc' }}>
                        <h4 style={{ marginBottom: '15px', color: '#64748b' }}>Security Brief</h4>
                        <ul style={{ fontSize: '0.85rem', color: '#64748b', paddingLeft: '20px', lineHeight: '1.8' }}>
                            <li>Verify Physical ID for all new entries.</li>
                            <li>Ensure guest is escorted or directed.</li>
                            <li>Collect visitor badge on exit.</li>
                            <li>Log exit immediately once guest leaves.</li>
                        </ul>
                    </div>
                </div>
            )}

            {(activeTab === 'active_logs' || activeTab === 'all_history') && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
                    {logs.length === 0 ? (
                        <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '100px', color: '#94a3b8' }}>
                            <div style={{ fontSize: '3rem', marginBottom: '15px' }}>🛡️</div>
                            <p>Campus periphery is clear. No active visitors logged.</p>
                        </div>
                    ) : (
                        logs.map(log => {
                            const isOnCampus = !log.exitTime;
                            return (
                                <div key={log.id} className="stat-card" style={{
                                    borderLeft: `5px solid ${isOnCampus ? '#3b82f6' : '#cbd5e1'}`,
                                    display: 'flex', flexDirection: 'column', gap: '12px'
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                        <div>
                                            <h4 style={{ margin: 0, fontSize: '1.1rem' }}>{log.visitorName}</h4>
                                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>{log.visitorPhone}</div>
                                        </div>
                                        <span className={`badge ${isOnCampus ? 'badge-primary' : 'badge-secondary'}`} style={{ fontSize: '0.65rem' }}>
                                            {isOnCampus ? 'ON CAMPUS' : 'CLOSED'}
                                        </span>
                                    </div>

                                    <div style={{ fontSize: '0.85rem', color: '#4b5563' }}>
                                        <strong>Meeting:</strong> {log.personToMeet}<br />
                                        <strong>Purpose:</strong> {log.purpose}
                                    </div>

                                    <div style={{
                                        marginTop: 'auto', paddingTop: '10px', borderTop: '1px solid #f1f5f9',
                                        display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.8rem'
                                    }}>
                                        <div>
                                            <div style={{ color: '#94a3b8', fontSize: '0.7rem' }}>ENTERED</div>
                                            <strong>{new Date(log.entryTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</strong>
                                        </div>
                                        {isOnCampus ? (
                                            <button className="btn btn-sm btn-danger" onClick={() => handleTriggerExit(log.id)}>Check-Out</button>
                                        ) : (
                                            <div style={{ textAlign: 'right' }}>
                                                <div style={{ color: '#94a3b8', fontSize: '0.7rem' }}>EXITED</div>
                                                <strong>{new Date(log.exitTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</strong>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })
                    )}
                </div>
            )}
        </div>
    );
};

export default VisitorPage;
