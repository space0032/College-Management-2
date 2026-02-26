import React, { useState, useEffect } from 'react';
import { getAuditLogs } from '../services/auditService';
import { exportToCSV } from '../utils/exportUtils';

const ACTION_COLORS = {
    CREATE: '#e6fffa', LOGIN: '#ebf8ff', DELETE: '#fff5f5',
    UPDATE: '#fffff0', LOGOUT: '#faf5ff', DEFAULT: '#f7fafc'
};
const ACTION_BADGES = {
    CREATE: '#276749', LOGIN: '#2b6cb0', DELETE: '#c53030',
    UPDATE: '#c05621', LOGOUT: '#553c9a', DEFAULT: '#4a5568'
};

const AuditLogPage = () => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchUser, setSearchUser] = useState('');
    const [dateFrom, setDateFrom] = useState('');
    const [dateTo, setDateTo] = useState('');

    useEffect(() => { fetchLogs(); }, []);

    const fetchLogs = async (params = {}) => {
        setLoading(true);
        setError(null);
        try {
            const res = await getAuditLogs({ limit: 200, ...params });
            setLogs(Array.isArray(res.data) ? res.data : []);
        } catch {
            setError('Failed to load audit logs. Ensure backend is running.');
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = (e) => {
        e.preventDefault();
        const params = {};
        if (dateFrom) params.from = dateFrom;
        if (dateTo) params.to = dateTo;
        fetchLogs(params);
    };

    const handleReset = () => {
        setSearchUser(''); setDateFrom(''); setDateTo('');
        fetchLogs();
    };

    const filtered = searchUser
        ? logs.filter(l => (l.username || '').toLowerCase().includes(searchUser.toLowerCase()) ||
            (l.action || '').toLowerCase().includes(searchUser.toLowerCase()) ||
            (l.entityType || '').toLowerCase().includes(searchUser.toLowerCase()))
        : logs;

    const formatTimestamp = (ts) => {
        if (!ts) return 'N/A';
        try {
            const d = new Date(ts);
            return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
                + ' ' + d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
        } catch { return String(ts); }
    };

    const getActionStyle = (action) => {
        const key = Object.keys(ACTION_COLORS).find(k => (action || '').toUpperCase().includes(k)) || 'DEFAULT';
        return { bg: ACTION_COLORS[key], color: ACTION_BADGES[key] };
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>🗒️ Audit Log</h2>
                    <p className="text-muted">System-wide activity tracking. All user actions are recorded here.</p>
                </div>
                {filtered.length > 0 && (
                    <button className="btn btn-secondary" onClick={() => exportToCSV(
                        ['Time', 'Username', 'Action', 'Entity Type', 'Entity ID', 'Details'],
                        filtered.map(l => [formatTimestamp(l.timestamp), l.username, l.action, l.entityType, l.entityId, l.details]),
                        'audit_log_export'
                    )}>⬇ Export CSV</button>
                )}
            </div>

            {/* Filters */}
            <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '16px', marginBottom: '20px' }}>
                <form onSubmit={handleSearch} style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'flex-end' }}>
                    <div className="form-group" style={{ margin: 0, flex: '2 1 200px' }}>
                        <label>Search User / Action / Entity</label>
                        <input
                            type="text" value={searchUser} onChange={e => setSearchUser(e.target.value)}
                            placeholder="Filter by username, action..."
                        />
                    </div>
                    <div className="form-group" style={{ margin: 0, flex: '1 1 150px' }}>
                        <label>From Date</label>
                        <input type="date" value={dateFrom} onChange={e => setDateFrom(e.target.value)} />
                    </div>
                    <div className="form-group" style={{ margin: 0, flex: '1 1 150px' }}>
                        <label>To Date</label>
                        <input type="date" value={dateTo} onChange={e => setDateTo(e.target.value)} />
                    </div>
                    <button type="submit" className="btn btn-primary" style={{ height: '42px' }}>🔍 Filter</button>
                    <button type="button" className="btn btn-secondary" style={{ height: '42px' }} onClick={handleReset}>Reset</button>
                </form>
            </div>

            {/* Stats */}
            <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
                {['LOGIN', 'CREATE', 'UPDATE', 'DELETE'].map(act => {
                    const count = filtered.filter(l => (l.action || '').toUpperCase().includes(act)).length;
                    const style = getActionStyle(act);
                    return (
                        <div key={act} style={{ padding: '12px 18px', borderRadius: '8px', background: style.bg, border: `1px solid ${style.color}30`, minWidth: '100px', textAlign: 'center' }}>
                            <div style={{ fontSize: '1.4rem', fontWeight: 'bold', color: style.color }}>{count}</div>
                            <div style={{ fontSize: '0.75rem', color: '#718096' }}>{act} events</div>
                        </div>
                    );
                })}
                <div style={{ padding: '12px 18px', borderRadius: '8px', background: '#f7fafc', border: '1px solid #e2e8f0', minWidth: '100px', textAlign: 'center' }}>
                    <div style={{ fontSize: '1.4rem', fontWeight: 'bold', color: '#2d3748' }}>{filtered.length}</div>
                    <div style={{ fontSize: '0.75rem', color: '#718096' }}>Total shown</div>
                </div>
            </div>

            {error && <div style={{ color: '#c53030', padding: '14px', background: '#fff5f5', borderRadius: '8px', marginBottom: '16px' }}>{error}</div>}

            {loading ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>Loading audit logs...</div>
            ) : (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Time</th>
                                <th>User</th>
                                <th>Action</th>
                                <th>Entity</th>
                                <th>Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filtered.length === 0 ? (
                                <tr><td colSpan="5" style={{ textAlign: 'center', padding: '30px' }}>No audit log entries found.</td></tr>
                            ) : filtered.map(log => {
                                const { bg, color } = getActionStyle(log.action);
                                return (
                                    <tr key={log.id}>
                                        <td style={{ fontSize: '0.82rem', color: '#718096', whiteSpace: 'nowrap' }}>
                                            {formatTimestamp(log.timestamp)}
                                        </td>
                                        <td>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                <div style={{
                                                    width: '28px', height: '28px', borderRadius: '50%',
                                                    background: '#ebf8ff', display: 'flex', alignItems: 'center',
                                                    justifyContent: 'center', fontSize: '0.75rem', fontWeight: 'bold', color: '#2b6cb0'
                                                }}>
                                                    {(log.username || '?')[0].toUpperCase()}
                                                </div>
                                                <span style={{ fontWeight: '500' }}>{log.username || 'System'}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <span style={{
                                                background: bg, color,
                                                padding: '3px 10px', borderRadius: '20px', fontSize: '0.78rem', fontWeight: '600'
                                            }}>
                                                {log.action || 'UNKNOWN'}
                                            </span>
                                        </td>
                                        <td style={{ fontSize: '0.85rem' }}>
                                            {log.entityType && <span style={{ background: '#f7fafc', padding: '2px 8px', borderRadius: '4px', fontSize: '0.8rem' }}>{log.entityType}</span>}
                                            {log.entityId && <span style={{ marginLeft: '6px', color: '#a0aec0', fontSize: '0.78rem' }}>#{log.entityId}</span>}
                                        </td>
                                        <td style={{ fontSize: '0.83rem', color: '#4a5568', maxWidth: '300px' }}>
                                            {log.details || '-'}
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default AuditLogPage;
