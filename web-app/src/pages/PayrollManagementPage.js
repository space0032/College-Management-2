import React, { useState, useEffect, useCallback } from 'react';
import {
    getPayroll,
    generatePayroll,
    markAsPaid,
    markAllAsPaid,
    updatePayrollEntry,
    deletePayrollEntry
} from '../services/payrollService';

const MONTHS = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
];

const PayrollManagementPage = () => {
    const now = new Date();
    const [month, setMonth] = useState(now.getMonth() + 1);
    const [year, setYear] = useState(now.getFullYear());
    const [payrollData, setPayrollData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [editModal, setEditModal] = useState(null); // { entry, bonuses, deductions }

    const currentUser = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();

    const fetchPayroll = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await getPayroll(month, year);
            setPayrollData(res.data?.data || []);
        } catch (err) {
            console.error(err);
            setError('Failed to load payroll data.');
        } finally {
            setLoading(false);
        }
    }, [month, year]);

    useEffect(() => {
        fetchPayroll();
    }, [fetchPayroll]);

    if (currentUser.role !== 'ADMIN') {
        return (
            <div className="page-container" style={{ textAlign: 'center', paddingTop: '60px' }}>
                <h2>🔒 Access Restricted</h2>
                <p className="text-muted">Only administrators can manage payroll.</p>
            </div>
        );
    }

    const handleGenerate = async () => {
        if (!window.confirm(`Generate payroll for ${MONTHS[month - 1]} ${year}? This will create entries for all active employees not yet included.`)) return;
        try {
            const res = await generatePayroll(month, year);
            alert(res.data?.message || 'Payroll generated.');
            fetchPayroll();
        } catch (err) {
            alert('Failed to generate payroll.');
        }
    };

    const handleMarkPaid = async (entry) => {
        if (!window.confirm(`Mark payroll for ${entry.employeeName} as PAID?`)) return;
        try {
            await markAsPaid(entry.id);
            fetchPayroll();
        } catch (err) {
            alert('Failed to mark as paid.');
        }
    };

    const handleMarkAllPaid = async () => {
        const pending = payrollData.filter(p => p.status !== 'PAID');
        if (pending.length === 0) { alert('No pending entries.'); return; }
        if (!window.confirm(`Mark ALL ${pending.length} pending entries for ${MONTHS[month - 1]} ${year} as PAID?`)) return;
        try {
            await markAllAsPaid(month, year);
            fetchPayroll();
        } catch (err) {
            alert('Failed to mark all as paid.');
        }
    };

    const handleDelete = async (entry) => {
        if (!window.confirm(`Delete payroll entry for ${entry.employeeName}?`)) return;
        try {
            await deletePayrollEntry(entry.id);
            fetchPayroll();
        } catch (err) {
            alert('Failed to delete entry.');
        }
    };

    const openEditModal = (entry) => {
        setEditModal({
            entry,
            bonuses: entry.bonuses || 0,
            deductions: entry.deductions || 0
        });
    };

    const handleEditSave = async () => {
        try {
            await updatePayrollEntry(editModal.entry.id, editModal.bonuses, editModal.deductions);
            setEditModal(null);
            fetchPayroll();
        } catch (err) {
            alert('Failed to update entry.');
        }
    };

    const formatCurrency = (amount) =>
        new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount || 0);

    const totalNet = payrollData.reduce((sum, p) => sum + (parseFloat(p.netSalary) || 0), 0);
    const paidCount = payrollData.filter(p => p.status === 'PAID').length;
    const pendingCount = payrollData.length - paidCount;

    const yearOptions = [];
    for (let y = now.getFullYear() - 2; y <= now.getFullYear() + 1; y++) yearOptions.push(y);

    return (
        <div className="page-container">
            {/* Header */}
            <div className="page-header">
                <div>
                    <h2>💰 Payroll Management</h2>
                    <p className="text-muted">Monthly salary disbursement tracking and payroll generation.</p>
                </div>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <select
                        value={month}
                        onChange={e => setMonth(parseInt(e.target.value))}
                        style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #ddd' }}
                    >
                        {MONTHS.map((m, i) => (
                            <option key={i + 1} value={i + 1}>{m}</option>
                        ))}
                    </select>
                    <select
                        value={year}
                        onChange={e => setYear(parseInt(e.target.value))}
                        style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #ddd' }}
                    >
                        {yearOptions.map(y => <option key={y} value={y}>{y}</option>)}
                    </select>
                    <button className="btn btn-secondary" onClick={handleGenerate}>⚙️ Generate Payroll</button>
                    <button className="btn btn-primary" onClick={handleMarkAllPaid} disabled={pendingCount === 0}>
                        ✅ Mark All Paid
                    </button>
                </div>
            </div>

            {/* Summary Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '15px', marginBottom: '20px' }}>
                <div className="stat-card" style={{ padding: '20px', background: '#f0f7ff', borderRadius: '10px', border: '1px solid #bee3f8' }}>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#2b6cb0' }}>{payrollData.length}</div>
                    <div style={{ color: '#4a5568', fontSize: '0.9rem' }}>Total Employees</div>
                </div>
                <div className="stat-card" style={{ padding: '20px', background: '#f0fff4', borderRadius: '10px', border: '1px solid #9ae6b4' }}>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#276749' }}>{paidCount}</div>
                    <div style={{ color: '#4a5568', fontSize: '0.9rem' }}>Paid ✓</div>
                </div>
                <div className="stat-card" style={{ padding: '20px', background: '#fffaf0', borderRadius: '10px', border: '1px solid #fbd38d' }}>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#7b341e' }}>{pendingCount}</div>
                    <div style={{ color: '#4a5568', fontSize: '0.9rem' }}>Pending Payment</div>
                </div>
            </div>

            {/* Total Payout Banner */}
            {payrollData.length > 0 && (
                <div style={{
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white', borderRadius: '10px', padding: '16px 24px',
                    marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                }}>
                    <span style={{ fontSize: '1rem', opacity: 0.9 }}>Total Payout — {MONTHS[month - 1]} {year}</span>
                    <span style={{ fontSize: '1.6rem', fontWeight: 'bold' }}>{formatCurrency(totalNet)}</span>
                </div>
            )}

            {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

            {loading ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>Loading payroll data...</div>
            ) : (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Employee</th>
                                <th>Designation</th>
                                <th style={{ textAlign: 'right' }}>Basic Salary</th>
                                <th style={{ textAlign: 'right' }}>Bonuses</th>
                                <th style={{ textAlign: 'right' }}>Deductions</th>
                                <th style={{ textAlign: 'right' }}>Net Salary</th>
                                <th>Status</th>
                                <th>Payment Date</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {payrollData.length === 0 ? (
                                <tr>
                                    <td colSpan="9" style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                                        No payroll data for {MONTHS[month - 1]} {year}. Click "Generate Payroll" to create entries.
                                    </td>
                                </tr>
                            ) : (
                                payrollData.map(entry => (
                                    <tr key={entry.id}>
                                        <td style={{ fontWeight: '500' }}>{entry.employeeName}</td>
                                        <td><span className="status-badge" style={{ background: '#e3f2fd', color: '#1565c0' }}>{entry.designation}</span></td>
                                        <td style={{ textAlign: 'right' }}>{formatCurrency(entry.basicSalary)}</td>
                                        <td style={{ textAlign: 'right', color: '#38a169' }}>+{formatCurrency(entry.bonuses)}</td>
                                        <td style={{ textAlign: 'right', color: '#e53e3e' }}>-{formatCurrency(entry.deductions)}</td>
                                        <td style={{ textAlign: 'right', fontWeight: 'bold' }}>{formatCurrency(entry.netSalary)}</td>
                                        <td>
                                            {entry.status === 'PAID'
                                                ? <span className="status-badge status-active">✓ PAID</span>
                                                : entry.status === 'CANCELLED'
                                                    ? <span className="status-badge status-rejected">CANCELLED</span>
                                                    : <span className="status-badge status-pending">PENDING</span>
                                            }
                                        </td>
                                        <td style={{ color: '#718096', fontSize: '0.85rem' }}>
                                            {entry.paymentDate || '—'}
                                        </td>
                                        <td>
                                            <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                                                {entry.status !== 'PAID' && (
                                                    <>
                                                        <button
                                                            className="btn btn-secondary"
                                                            style={{ padding: '4px 8px', fontSize: '0.8rem' }}
                                                            onClick={() => openEditModal(entry)}
                                                        >Edit</button>
                                                        <button
                                                            className="btn btn-primary"
                                                            style={{ padding: '4px 8px', fontSize: '0.8rem' }}
                                                            onClick={() => handleMarkPaid(entry)}
                                                        >Mark Paid</button>
                                                    </>
                                                )}
                                                <button
                                                    className="btn"
                                                    style={{ padding: '4px 8px', fontSize: '0.8rem', background: '#fed7d7', color: '#c53030', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                                                    onClick={() => handleDelete(entry)}
                                                >Delete</button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Edit Modal */}
            {editModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '420px' }}>
                        <div className="modal-header">
                            <h2>✏️ Edit Payroll Entry</h2>
                            <button className="modal-close" onClick={() => setEditModal(null)}>×</button>
                        </div>
                        <div style={{ padding: '20px' }}>
                            <p style={{ marginBottom: '16px', color: '#555' }}>
                                Employee: <strong>{editModal.entry.employeeName}</strong><br />
                                Basic Salary: <strong>{formatCurrency(editModal.entry.basicSalary)}</strong>
                            </p>
                            <div className="form-group">
                                <label>Bonuses (₹)</label>
                                <input
                                    type="number" min="0" step="0.01"
                                    value={editModal.bonuses}
                                    onChange={e => setEditModal(prev => ({ ...prev, bonuses: e.target.value }))}
                                />
                            </div>
                            <div className="form-group">
                                <label>Deductions (₹)</label>
                                <input
                                    type="number" min="0" step="0.01"
                                    value={editModal.deductions}
                                    onChange={e => setEditModal(prev => ({ ...prev, deductions: e.target.value }))}
                                />
                            </div>
                            <div style={{ marginTop: '8px', padding: '10px', background: '#f7fafc', borderRadius: '6px', fontSize: '0.9rem' }}>
                                Net Salary Preview: <strong style={{ color: '#2b6cb0' }}>
                                    {formatCurrency(
                                        parseFloat(editModal.entry.basicSalary || 0) +
                                        parseFloat(editModal.bonuses || 0) -
                                        parseFloat(editModal.deductions || 0)
                                    )}
                                </strong>
                            </div>
                            <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
                                <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setEditModal(null)}>Cancel</button>
                                <button className="btn btn-primary" style={{ flex: 2 }} onClick={handleEditSave}>Save Changes</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PayrollManagementPage;
