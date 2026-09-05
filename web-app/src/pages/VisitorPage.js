import React, { useState, useEffect } from 'react';
import {
    getVisitorByPhone, getActiveVisitors, getAllVisitorLogs,
    logVisitorEntry, logVisitorExit
} from '../services/visitorService';
import { exportToExcel } from '../utils/exportUtils';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { toast } from '../components/Toast';
import { getErrorMessage, getSuccessRefId } from '../utils/error';
import { SkeletonCards } from '../components/Skeleton';

const VisitorPage = () => {
    const [activeTab, setActiveTab] = useState('active_logs');
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState('');
    const [isSearching, setIsSearching] = useState(false);
    const [isVisitorFound, setIsVisitorFound] = useState(false);
    const [phoneSearch, setPhoneSearch] = useState('');
    const [checkInOpen, setCheckInOpen] = useState(false);
    const [saving, setSaving] = useState(false);
    const [pendingExit, setPendingExit] = useState(null);

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

    const loadActiveLogs = async (signal) => {
        setLoading(true);
        setLoadError('');
        try {
            const res = await getActiveVisitors(signal);
            if (signal?.aborted) return;
            setLogs(res.data || []);
        } catch (err) {
            if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
            setLoadError(err?.response?.data?.error || 'Could not load visitor logs.');
        } finally { if (!signal?.aborted) setLoading(false); }
    };

    const loadAllLogs = async (signal) => {
        setLoading(true);
        setLoadError('');
        try {
            const res = await getAllVisitorLogs(signal);
            if (signal?.aborted) return;
            setLogs(res.data || []);
        } catch (err) {
            if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
            setLoadError(err?.response?.data?.error || 'Could not load visitor history.');
        } finally { if (!signal?.aborted) setLoading(false); }
    };

    useEffect(() => {
        const controller = new AbortController();
        if (activeTab === 'active_logs') loadActiveLogs(controller.signal);
        if (activeTab === 'all_history') loadAllLogs(controller.signal);
        return () => controller.abort();
    }, [activeTab]);

    const handlePhoneSearch = async (e) => {
        if (e) e.preventDefault();
        const searchedPhone = phoneSearch.trim();
        if (!/^\+?[0-9\s-]{10,15}$/.test(searchedPhone)) {
            toast.error('Enter a valid phone number using 10 to 15 digits.');
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

    const handleRegisterEntry = async () => {
        if (!/^\+?[0-9\s-]{10,15}$/.test(formData.phone.trim())) {
            toast.error('Enter a valid visitor phone number.');
            return;
        }
        setSaving(true);
        try {
            const refId = getSuccessRefId();
            await logVisitorEntry(formData);
            toast.success('Security clearance granted. Entry logged.', { refId });
            setFormData({
                phone: '', name: '', email: '', idProofType: 'Aadhar', idProofNumber: '',
                purpose: '', personToMeet: '', gateNumber: 'Gate 1'
            });
            setPhoneSearch('');
            setIsVisitorFound(false);
            setCheckInOpen(false);
            setActiveTab('active_logs');
            loadActiveLogs();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Check-in failed.');
            toast.error(message, { refId, details: { status } });
        } finally { setSaving(false); }
    };

    const handleTriggerExit = async () => {
        if (!pendingExit) return;
        try {
            await logVisitorExit(pendingExit);
            setPendingExit(null);
            toast.success('Visitor exit logged.', { refId: getSuccessRefId() });
            if (activeTab === 'active_logs') loadActiveLogs();
            else loadAllLogs();
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not log visitor exit.');
            toast.error(message, { refId });
        }
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
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <div style={{ display: 'flex', gap: '8px', background: '#f8fafc', padding: '4px', borderRadius: '8px' }}>
                        <button className={`btn btn-sm ${activeTab === 'active_logs' ? 'btn-primary' : ''}`} style={activeTab !== 'active_logs' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('active_logs')}>Live Roster</button>
                        <button className={`btn btn-sm ${activeTab === 'all_history' ? 'btn-primary' : ''}`} style={activeTab !== 'all_history' ? { background: 'transparent', border: 'none', color: '#64748b' } : {}} onClick={() => setActiveTab('all_history')}>History</button>
                    </div>
                    <button className="btn btn-sm btn-primary" onClick={() => setCheckInOpen(true)}>+ Check-In Visitor</button>
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

            <Modal
                isOpen={checkInOpen}
                title="Check-In Visitor"
                onClose={() => setCheckInOpen(false)}
                onSubmit={handleRegisterEntry}
                submitLabel="Grant Entry & Log Token"
                submitting={saving}
                submitDisabled={!formData.phone || !formData.name || !formData.personToMeet || !formData.purpose}
                isDirty={Boolean(formData.phone || formData.name || formData.purpose)}
                size="medium"
            >
                <form onSubmit={(e) => { e.preventDefault(); handleRegisterEntry(); }}>
                    <div className="form-group">
                        <label className="form-label">Step 1 — Phone lookup</label>
                        <div style={{ display: 'flex', gap: '8px' }}>
                            <input
                                type="tel"
                                inputMode="tel"
                                pattern="[+]?[0-9\- ]{10,15}"
                                required
                                className="form-control"
                                placeholder="Enter visitor's phone number..."
                                value={phoneSearch}
                                onChange={(e) => setPhoneSearch(e.target.value)}
                            />
                            <button type="button" className="btn btn-secondary" disabled={isSearching} onClick={handlePhoneSearch}>
                                {isSearching ? 'Searching…' : 'Check Records'}
                            </button>
                        </div>
                        {isVisitorFound && <span className="field-hint" style={{ color: '#027a48' }}>✓ Returning visitor identified. Profile pre-loaded.</span>}
                    </div>
                    <div className="form-grid" style={{ marginTop: '12px' }}>
                        <div className="form-group">
                            <label className="form-label">Phone *</label>
                            <input type="tel" inputMode="tel" pattern="[+]?[0-9\- ]{7,15}" className="form-control" required readOnly value={formData.phone} placeholder="Run lookup first" />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Full Name *</label>
                            <input type="text" className="form-control" required value={formData.name} readOnly={isVisitorFound} onChange={e => setFormData({ ...formData, name: e.target.value })} />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Identity Proof *</label>
                            <select className="form-control" value={formData.idProofType} disabled={isVisitorFound} onChange={e => setFormData({ ...formData, idProofType: e.target.value })}>
                                <option value="Aadhar">Aadhar Card</option>
                                <option value="Driving License">Driving License</option>
                                <option value="Passport">Passport</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label className="form-label">ID Reference # *</label>
                            <input type="text" className="form-control" required value={formData.idProofNumber} readOnly={isVisitorFound} onChange={e => setFormData({ ...formData, idProofNumber: e.target.value })} />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Institutional Host *</label>
                            <input type="text" className="form-control" placeholder="Room/Person to meet" required value={formData.personToMeet} onChange={e => setFormData({ ...formData, personToMeet: e.target.value })} />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Assigned Gate</label>
                            <select className="form-control" value={formData.gateNumber} onChange={e => setFormData({ ...formData, gateNumber: e.target.value })}>
                                <option value="Gate 1">Main Arch (G1)</option>
                                <option value="Gate 2">North Gate (G2)</option>
                                <option value="Gate 3">Faculty Gate (G3)</option>
                            </select>
                        </div>
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label className="form-label">Nature of Visit *</label>
                            <textarea className="form-control" required rows="3" value={formData.purpose} onChange={e => setFormData({ ...formData, purpose: e.target.value })} placeholder="Deliveries, Meeting, Campus Tour, etc."></textarea>
                        </div>
                    </div>
                </form>
            </Modal>
            <ConfirmDialog
                isOpen={Boolean(pendingExit)}
                title="Log visitor exit?"
                message="The visitor will be marked as checked out."
                confirmLabel="Log Exit"
                destructive={false}
                onConfirm={handleTriggerExit}
                onCancel={() => setPendingExit(null)}
            />

            {loadError && (
                <div className="retry-bar" role="alert" style={{ marginBottom: '16px' }}>
                    <span>{loadError}</span>
                    <button className="btn btn-secondary btn-sm" onClick={() => activeTab === 'all_history' ? loadAllLogs() : loadActiveLogs()}>Retry</button>
                </div>
            )}
            {loading ? (
                <SkeletonCards count={4} />
            ) : (
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
                                            <button className="btn btn-sm btn-danger" onClick={() => setPendingExit(log.id)}>Check-Out</button>
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
