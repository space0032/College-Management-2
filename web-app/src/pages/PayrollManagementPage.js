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
    const [editModal, setEditModal] = useState(null);

    const currentUser = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();

    const fetchPayroll = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await getPayroll(month, year);
            setPayrollData(res.data?.data || []);
        } catch (err) { setError('Failed to bridge with payroll ledger.'); }
        finally { setLoading(false); }
    }, [month, year]);

    useEffect(() => { fetchPayroll(); }, [fetchPayroll]);

    if (currentUser.role !== 'ADMIN') {
        return (
            <div className="page-container" style={{ textAlign: 'center', paddingTop: '100px' }}>
                <div style={{ fontSize: '4rem', marginBottom: '20px' }}>🔐</div>
                <h2>Institutional Access Restricted</h2>
                <p style={{ color: '#64748b' }}>Payroll operations require Administrative tier credentials.</p>
            </div>
        );
    }

    const handleMarkPaid = async (entry) => {
        if (!window.confirm(`Finalize salary disbursement for ${entry.employeeName}?`)) return;
        try {
            await markAsPaid(entry.id);
            fetchPayroll();
        } catch (err) { alert('Disbursement failed'); }
    };

    const formatCurrency = (amount) =>
        new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount || 0);

    const totalNet = payrollData.reduce((sum, p) => sum + (parseFloat(p.netSalary) || 0), 0);
    const paidCount = payrollData.filter(p => p.status === 'PAID').length;

    return (
        <div className="page-container" style={{ background: '#f1f5f9', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                    <div>
                        <h1 className="page-title">💳 Payroll & Treasury</h1>
                        <p className="page-subtitle">Monthly salary architecture, tax adjustments, and disbursement tracking</p>
                    </div>
                    <div style={{ display: 'flex', gap: '12px', alignItems: 'center', background: 'white', padding: '10px 20px', borderRadius: '15px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }}>
                        <select className="form-control" style={{ border: 'none', background: 'none', fontWeight: 'bold', width: 'auto' }} value={month} onChange={e => setMonth(parseInt(e.target.value))}>
                            {MONTHS.map((m, i) => <option key={i + 1} value={i + 1}>{m}</option>)}
                        </select>
                        <div style={{ width: '1px', height: '20px', background: '#e2e8f0' }} />
                        <select className="form-control" style={{ border: 'none', background: 'none', fontWeight: 'bold', width: 'auto' }} value={year} onChange={e => setYear(parseInt(e.target.value))}>
                            {[now.getFullYear() - 1, now.getFullYear(), now.getFullYear() + 1].map(y => <option key={y} value={y}>{y}</option>)}
                        </select>
                        <button className="btn btn-primary" onClick={() => {
                            if (window.confirm(`Generate payroll records for ${MONTHS[month - 1]} ${year}?`)) {
                                generatePayroll(month, year).then(() => {
                                    alert('Payroll batch generated successfully.');
                                    fetchPayroll();
                                }).catch(() => alert('Failed to generate batch. Check if records already exist.'));
                            }
                        }}>
                            ⚡ Bulk Generate
                        </button>
                    </div>
                </div>
            </div>

            {/* Premium Payout Dashboard */}
            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 2fr) 1fr', gap: '30px', marginBottom: '30px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)', color: 'white', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <div style={{ fontSize: '0.9rem', opacity: 0.8, letterSpacing: '1px' }}>MONTHLY TREASURY OUTFLOW</div>
                        <div style={{ fontSize: '2.5rem', fontWeight: '900', margin: '10px 0' }}>{formatCurrency(totalNet)}</div>
                        <div style={{ display: 'flex', gap: '20px' }}>
                            <div style={{ fontSize: '0.8rem' }}><span style={{ opacity: 0.6 }}>BASE:</span> {formatCurrency(payrollData.reduce((s, p) => s + (p.basicSalary || 0), 0))}</div>
                            <div style={{ fontSize: '0.8rem' }}><span style={{ opacity: 0.6 }}>ADJUSTS:</span> <span style={{ color: '#10b981' }}>+{formatCurrency(payrollData.reduce((s, p) => s + (p.bonuses || 0), 0))}</span></div>
                        </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                        <div style={{ fontSize: '3.5rem', opacity: 0.2 }}>📊</div>
                    </div>
                </div>

                <div style={{ display: 'grid', gridTemplateRows: '1fr 1fr', gap: '15px' }}>
                    <div className="stat-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <div style={{ fontSize: '0.75rem', color: '#64748b' }}>DISBURSEMENT RATIO</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{payrollData.length > 0 ? Math.round((paidCount / payrollData.length) * 100) : 0}%</div>
                        </div>
                        <div style={{ width: '50px', height: '50px', borderRadius: '50%', border: '4px solid #f1f5f9', borderTopColor: '#10b981', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.7rem' }}>✓</div>
                    </div>
                    <div className="stat-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <div style={{ fontSize: '0.75rem', color: '#64748b' }}>PENDING PAYMENTS</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#f59e0b' }}>{payrollData.length - paidCount} Staff</div>
                        </div>
                        <button className="btn btn-sm btn-primary" onClick={() => markAllAsPaid(month, year).then(fetchPayroll)}>Mark All Paid</button>
                    </div>
                </div>
            </div>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '100px', color: '#94a3b8' }}>🧮 Auditor is computing payroll sequence...</div>
            ) : (
                <div className="data-table-container" style={{ boxShadow: '0 10px 15px -3px rgba(0,0,0,0.1)' }}>
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Staff Member</th>
                                <th>Unit</th>
                                <th style={{ textAlign: 'right' }}>Formula (Basic+B-D)</th>
                                <th style={{ textAlign: 'right' }}>Net Disbursement</th>
                                <th>Status</th>
                                <th style={{ textAlign: 'center' }}>Ops</th>
                            </tr>
                        </thead>
                        <tbody>
                            {payrollData.length === 0 ? (
                                <tr>
                                    <td colSpan="6" style={{ textAlign: 'center', padding: '60px' }}>
                                        <div style={{ fontSize: '2rem', marginBottom: '10px' }}>📅</div>
                                        <div style={{ color: '#64748b' }}>No ledger entries for this cycle.</div>
                                        <button className="btn btn-secondary" style={{ marginTop: '15px' }} onClick={() => generatePayroll(month, year).then(fetchPayroll)}>Generate Records</button>
                                    </td>
                                </tr>
                            ) : (
                                payrollData.map(p => (
                                    <tr key={p.id}>
                                        <td>
                                            <div style={{ fontWeight: '600' }}>{p.employeeName}</div>
                                            <div style={{ fontSize: '0.7rem', color: '#94a3b8' }}>ID: {p.employeeId || 'EMP-' + p.id}</div>
                                        </td>
                                        <td><span className="badge" style={{ background: '#f1f5f9', color: '#475569' }}>{p.designation}</span></td>
                                        <td style={{ textAlign: 'right', fontSize: '0.85rem' }}>
                                            <span style={{ color: '#64748b' }}>{formatCurrency(p.basicSalary)}</span>
                                            <span style={{ color: '#10b981' }}> +{formatCurrency(p.bonuses)}</span>
                                            <span style={{ color: '#ef4444' }}> -{formatCurrency(p.deductions)}</span>
                                        </td>
                                        <td style={{ textAlign: 'right', fontWeight: '800', color: '#1e293b' }}>{formatCurrency(p.netSalary)}</td>
                                        <td>
                                            <span className={`badge ${p.status === 'PAID' ? 'badge-success' : 'badge-warning'}`} style={{ display: 'flex', alignItems: 'center', gap: '5px', width: 'fit-content' }}>
                                                {p.status === 'PAID' ? '✓ DISBURSED' : '⌛ PENDING'}
                                            </span>
                                        </td>
                                        <td style={{ textAlign: 'center' }}>
                                            {p.status !== 'PAID' ? (
                                                <div style={{ display: 'flex', gap: '5px', justifyContent: 'center' }}>
                                                    <button className="btn btn-sm btn-secondary" onClick={() => setEditModal({ ...p, bonusesInput: p.bonuses, deductionsInput: p.deductions })}>Edit</button>
                                                    <button className="btn btn-sm btn-primary" onClick={() => handleMarkPaid(p)}>Pay</button>
                                                </div>
                                            ) : (
                                                <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>{p.paymentDate}</span>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {editModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '400px', borderRadius: '20px', padding: '30px' }}>
                        <h2 style={{ marginBottom: '5px' }}>Adjust Payroll</h2>
                        <p style={{ color: '#64748b', fontSize: '0.85rem', marginBottom: '25px' }}>{editModal.employeeName}</p>
                        <div className="form-group">
                            <label>Bonus Incentives (₹)</label>
                            <input className="form-control" type="number" value={editModal.bonusesInput} onChange={e => setEditModal({ ...editModal, bonusesInput: e.target.value })} />
                        </div>
                        <div className="form-group">
                            <label>Deductions / Adjustments (₹)</label>
                            <input className="form-control" type="number" value={editModal.deductionsInput} onChange={e => setEditModal({ ...editModal, deductionsInput: e.target.value })} />
                        </div>
                        <div style={{ background: '#f8fafc', padding: '15px', borderRadius: '12px', marginTop: '20px', border: '1px solid #e2e8f0' }}>
                            <div style={{ fontSize: '0.7rem', color: '#64748b' }}>CALCULATED NET DISBURSEMENT</div>
                            <div style={{ fontSize: '1.4rem', fontWeight: 'bold' }}>
                                {formatCurrency(parseFloat(editModal.basicSalary) + parseFloat(editModal.bonusesInput || 0) - parseFloat(editModal.deductionsInput || 0))}
                            </div>
                        </div>
                        <div style={{ display: 'flex', gap: '10px', marginTop: '25px' }}>
                            <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setEditModal(null)}>Discard</button>
                            <button className="btn btn-primary" style={{ flex: 2 }} onClick={() => updatePayrollEntry(editModal.id, editModal.bonusesInput, editModal.deductionsInput).then(() => { setEditModal(null); fetchPayroll(); })}>Commit Ledger</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PayrollManagementPage;
