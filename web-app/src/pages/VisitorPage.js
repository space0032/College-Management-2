import React, { useState, useEffect } from 'react';
import {
    getVisitorByPhone, getActiveVisitors, getAllVisitorLogs,
    logVisitorEntry, logVisitorExit
} from '../services/visitorService';

const VisitorPage = () => {
    const [activeTab, setActiveTab] = useState('active_logs');
    const [logs, setLogs] = useState([]);

    // Registration Form State
    const [phoneSearch, setPhoneSearch] = useState('');
    const [isVisitorFound, setIsVisitorFound] = useState(false);

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
        } catch (err) {
            console.error(err);
        }
    };

    const loadAllLogs = async () => {
        try {
            const res = await getAllVisitorLogs();
            setLogs(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    useEffect(() => {
        if (activeTab === 'active_logs') loadActiveLogs();
        if (activeTab === 'all_history') loadAllLogs();
    }, [activeTab]);

    const handlePhoneSearch = async () => {
        if (!phoneSearch || phoneSearch.length < 10) return alert('Enter valid phone number');

        try {
            const res = await getVisitorByPhone(phoneSearch);
            if (res.data) {
                setIsVisitorFound(true);
                setFormData({
                    ...formData,
                    phone: res.data.phone,
                    name: res.data.name,
                    email: res.data.email || '',
                    idProofType: res.data.idProofType,
                    idProofNumber: res.data.idProofNumber
                });
            }
        } catch (err) {
            if (err.response?.status === 404) {
                setIsVisitorFound(false);
                setFormData({ ...formData, phone: phoneSearch, name: '', email: '', idProofNumber: '' });
                alert('New visitor. Please supply complete details.');
            }
        }
    };

    const handleRegisterEntry = async (e) => {
        e.preventDefault();
        try {
            await logVisitorEntry(formData);
            alert('Visitor Entry Registered!');
            setFormData({
                phone: '', name: '', email: '', idProofType: 'Aadhar', idProofNumber: '',
                purpose: '', personToMeet: '', gateNumber: 'Gate 1'
            });
            setPhoneSearch('');
            setIsVisitorFound(false);
            setActiveTab('active_logs');
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to register entry.');
        }
    };

    const handleTriggerExit = async (logId) => {
        try {
            await logVisitorExit(logId);
            loadActiveLogs(); // Refresh the list
        } catch (err) {
            alert('Failed to log exit.');
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Visitor Management</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        className={`btn ${activeTab === 'active_logs' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('active_logs')}
                    >
                        Active Visitors
                    </button>
                    <button
                        className={`btn ${activeTab === 'register' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('register')}
                    >
                        Register Entry
                    </button>
                    <button
                        className={`btn ${activeTab === 'all_history' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('all_history')}
                    >
                        Entry/Exit History
                    </button>
                </div>
            </div>

            {(activeTab === 'active_logs' || activeTab === 'all_history') && (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Visitor</th>
                                <th>Contact</th>
                                <th>Purpose</th>
                                <th>Host</th>
                                <th>Gate</th>
                                <th>Entry Time</th>
                                <th>Exit Time</th>
                                {activeTab === 'active_logs' && <th>Action</th>}
                            </tr>
                        </thead>
                        <tbody>
                            {logs.length === 0 ? (
                                <tr><td colSpan={activeTab === 'active_logs' ? 8 : 7} style={{ textAlign: 'center' }}>No log records found.</td></tr>
                            ) : (
                                logs.map(log => (
                                    <tr key={log.id}>
                                        <td>{log.visitorName}</td>
                                        <td>{log.visitorPhone}</td>
                                        <td>{log.purpose}</td>
                                        <td>{log.personToMeet}</td>
                                        <td>{log.gateNumber}</td>
                                        <td>{new Date(log.entryTime).toLocaleString()}</td>
                                        <td>{log.exitTime ? new Date(log.exitTime).toLocaleString() : <span className="badge badge-warning">On Campus</span>}</td>
                                        {activeTab === 'active_logs' && (
                                            <td>
                                                <button className="btn btn-danger btn-sm" onClick={() => handleTriggerExit(log.id)}>Toggle Exit</button>
                                            </td>
                                        )}
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {activeTab === 'register' && (
                <div className="form-container" style={{ maxWidth: '800px', margin: '0 auto', gap: '30px', display: 'flex', flexDirection: 'column' }}>

                    <div className="stat-card">
                        <h3>Retrieve Existing Visitor</h3>
                        <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                            <input
                                type="text"
                                placeholder="Enter 10-digit Phone Number"
                                value={phoneSearch}
                                onChange={(e) => setPhoneSearch(e.target.value)}
                                style={{ flex: 1, padding: '10px' }}
                            />
                            <button className="btn btn-secondary" onClick={handlePhoneSearch}>Search Directory</button>
                        </div>
                    </div>

                    <div className="stat-card">
                        <h3>Visitor Verification & Entry</h3>
                        {isVisitorFound && <div className="badge badge-success" style={{ marginBottom: '15px', display: 'inline-block' }}>✓ Returning Visitor Found</div>}

                        <form className="form-grid" onSubmit={handleRegisterEntry}>
                            <div className="form-group">
                                <label>Phone Number *</label>
                                <input type="text" required value={formData.phone} readOnly={isVisitorFound} onChange={e => setFormData({ ...formData, phone: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Full Name *</label>
                                <input type="text" required value={formData.name} readOnly={isVisitorFound} onChange={e => setFormData({ ...formData, name: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Email Address</label>
                                <input type="email" value={formData.email} readOnly={isVisitorFound} onChange={e => setFormData({ ...formData, email: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>ID Proof Type *</label>
                                <select value={formData.idProofType} disabled={isVisitorFound} onChange={e => setFormData({ ...formData, idProofType: e.target.value })}>
                                    <option value="Aadhar">Aadhar Card</option>
                                    <option value="Driving License">Driving License</option>
                                    <option value="Passport">Passport</option>
                                </select>
                            </div>
                            <div className="form-group">
                                <label>ID Proof Number *</label>
                                <input type="text" required value={formData.idProofNumber} readOnly={isVisitorFound} onChange={e => setFormData({ ...formData, idProofNumber: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Host/Person To Meet *</label>
                                <input type="text" required value={formData.personToMeet} onChange={e => setFormData({ ...formData, personToMeet: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Gate Number *</label>
                                <select value={formData.gateNumber} onChange={e => setFormData({ ...formData, gateNumber: e.target.value })}>
                                    <option value="Gate 1">Gate 1 (Main)</option>
                                    <option value="Gate 2">Gate 2 (Hostel)</option>
                                    <option value="Gate 3">Gate 3 (Service)</option>
                                </select>
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Purpose of Visit *</label>
                                <input type="text" required value={formData.purpose} onChange={e => setFormData({ ...formData, purpose: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '12px', fontSize: '1.1rem' }}>
                                    Complete Security Check-In
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

        </div>
    );
};

export default VisitorPage;
