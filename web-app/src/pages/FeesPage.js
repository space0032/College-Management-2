import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getPendingFees, getAllFees, recordPayment, getPaymentHistory, getFeeCategories, createFeeEntry } from '../services/feesService';
import { getAllStudents } from '../services/studentService';
import { exportToCSV, exportToExcel } from '../utils/exportUtils';
import ReceiptModal from '../components/ReceiptModal';

const formatDueDate = (v) => {
  if (!v) return '—';
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  const overdue = d < new Date(new Date().toDateString());
  const text = d.toLocaleDateString('en-IN', { year: 'numeric', month: 'short', day: 'numeric' });
  return overdue ? `${text} (overdue)` : text;
};

const balanceOf = (fee) => Number(fee?.totalAmount ?? fee?.amount ?? 0) - Number(fee?.paidAmount ?? 0);

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'studentUsername', label: 'Enrollment No.', render: (v) => (
    <span style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#2d3748' }}>{v || 'N/A'}</span>
  )},
  { key: 'studentName', label: 'Student Name' },
  { key: 'totalAmount', label: 'Amount', render: (v, f) => `₹${Number(v ?? f?.amount ?? 0).toLocaleString('en-IN')}` },
  { key: 'dueDate', label: 'Due Date', render: (v) => formatDueDate(v) },
  { key: 'categoryName', label: 'Fee Type', render: (v, f) => v || f?.feeType || f?.fee_type || '—' },
  {
    key: 'status', label: 'Status', render: (v) => {
      const s = v || 'PENDING';
      const cls = s === 'PAID' ? 'success' : s === 'PARTIAL' ? 'warning' : 'danger';
      return <span className={`badge badge-${cls}`}>{s}</span>;
    }
  },
];

const FeesPage = () => {
  const [fees, setFees] = useState([]);
  const [allFees, setAllFees] = useState(false);
  const [payModal, setPayModal] = useState(false);
  const [historyModal, setHistoryModal] = useState(false);
  const [selectedFee, setSelectedFee] = useState(null);
  const [history, setHistory] = useState([]);
  const [payForm, setPayForm] = useState({ amount: '', paymentMode: 'CASH', remarks: '' });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [receiptFee, setReceiptFee] = useState(null);

  const [entryModal, setEntryModal] = useState(false);
  const [entryForm, setEntryForm] = useState({ enrollmentId: '', categoryId: '', amount: '', dueDate: '' });
  const [categories, setCategories] = useState([]);
  const [students, setStudents] = useState([]);
  const [entryError, setEntryError] = useState('');
  const [bootstrapError, setBootstrapError] = useState('');
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState('');

  useEffect(() => {
    let cancelled = false;
    Promise.all([
      getFeeCategories().then(res => res.data || []),
      getAllStudents().then(res => (res.data || []).map(s => ({ id: s.id, name: s.name, username: s.username })))
    ]).then(([cats, studs]) => {
      if (cancelled) return;
      setCategories(cats);
      setStudents(studs);
      setBootstrapError('');
    }).catch((err) => {
      if (cancelled) return;
      const status = err?.response?.status;
      setBootstrapError(status === 403
        ? 'You lack permission to load fee categories/students. The entry form may be incomplete.'
        : 'Failed to load fee categories/students. The entry form may be incomplete.');
    });
    return () => { cancelled = true; };
  }, []);

  const fetchFees = React.useCallback(async () => {
    setLoading(true);
    setError('');
    const apiCall = allFees ? getAllFees : getPendingFees;
    try {
      const res = await apiCall();
      setFees(res.data || []);
    } catch (err) {
      const status = err?.response?.status;
      setFees([]);
      if (status === 403) {
        setError('Access denied loading fees (missing VIEW_FEES permission).');
      } else if (!err?.response) {
        setError('Cannot reach the server. Check your connection and retry.');
      } else {
        setError(err.response?.data?.error || 'Failed to load fees.');
      }
    } finally {
      setLoading(false);
    }
  }, [allFees]);

  useEffect(() => {
    fetchFees();
  }, [fetchFees]);

  const handlePayClick = (fee) => {
    setSelectedFee(fee);
    const remaining = balanceOf(fee);
    setPayForm({ amount: Number.isFinite(remaining) && remaining > 0 ? remaining.toFixed(2) : '', paymentMode: 'CASH', remarks: '' });
    setFormError('');
    setPayModal(true);
  };

  const handleReceiptClick = async (fee, payment) => {
    // A StudentFee row carries no payment date/receipt of its own. Resolve the
    // real payment (latest first — backend orders history by payment_date DESC)
    // so the receipt shows the actual paid date and receipt number.
    if (payment?.receiptNumber || payment?.paymentDate) {
      setReceiptFee({
        ...fee,
        amount: payment.amount ?? fee.amount ?? fee.totalAmount,
        paidAmount: payment.amount ?? fee.paidAmount,
        receiptNumber: payment.receiptNumber || fee.receiptNumber,
        paidDate: payment.paymentDate || payment.payment_date || fee.paidDate
      });
      return;
    }
    try {
      const res = await getPaymentHistory(fee.id);
      const list = res.data || [];
      const latest = list[0];
      if (latest) {
        setReceiptFee({
          ...fee,
          amount: latest.amount ?? fee.amount ?? fee.totalAmount,
          paidAmount: latest.amount ?? fee.paidAmount,
          receiptNumber: latest.receiptNumber || fee.receiptNumber,
          paidDate: latest.paymentDate || latest.payment_date
        });
      } else {
        setReceiptFee(fee);
      }
    } catch {
      setReceiptFee(fee);
    }
  };

  const handleHistoryClick = async (fee) => {
    setSelectedFee(fee);
    setHistoryModal(true);
    setHistory([]);
    setHistoryError('');
    setHistoryLoading(true);
    try {
      const res = await getPaymentHistory(fee.id);
      setHistory(res.data || []);
    } catch (err) {
      setHistory([]);
      setHistoryError(err?.response?.data?.error || 'Failed to load payment history.');
    } finally {
      setHistoryLoading(false);
    }
  };

  const handlePaymentSubmit = async () => {
    if (saving) return;
    const amount = Number(payForm.amount);
    if (!payForm.amount || Number.isNaN(amount) || amount <= 0) {
      setFormError('Please enter a valid amount.');
      return;
    }
    const remaining = selectedFee ? balanceOf(selectedFee) : NaN;
    if (Number.isFinite(remaining) && amount > remaining) {
      setFormError(`Amount exceeds the remaining balance of ₹${remaining.toLocaleString('en-IN')}.`);
      return;
    }
    setSaving(true);
    try {
      const res = await recordPayment({
        studentFeeId: selectedFee.id,
        amount,
        paymentMode: payForm.paymentMode,
        remarks: payForm.remarks
      });
      const capped = res?.data?.capped;
      setPayModal(false);
      setSelectedFee(null);
      await fetchFees();
      if (capped) {
        setError(`Payment was capped to the remaining balance (recorded ₹${Number(res.data.recordedAmount ?? amount).toLocaleString('en-IN')}).`);
      }
    } catch (err) {
      setFormError(err.response?.data?.error || err.response?.data?.message || 'Failed to record payment.');
    } finally {
      setSaving(false);
    }
  };

  const openEntryModal = () => {
    setEntryForm({ enrollmentId: '', categoryId: '', amount: '', dueDate: '' });
    setEntryError('');
    setEntryModal(true);
  };

  const handleAddFeeSubmit = async () => {
    if (saving) return;
    if (!entryForm.enrollmentId || !entryForm.categoryId || !entryForm.amount || Number(entryForm.amount) <= 0) {
      setEntryError('Student, fee category and a positive amount are required.');
      return;
    }
    if (entryForm.dueDate) {
      const todayStr = new Date().toISOString().split('T')[0];
      if (entryForm.dueDate < todayStr) {
        setEntryError('Due date cannot be in the past.');
        return;
      }
    }
    setSaving(true);
    try {
      await createFeeEntry({
        enrollmentId: entryForm.enrollmentId,
        categoryId: Number(entryForm.categoryId),
        amount: Number(entryForm.amount),
        dueDate: entryForm.dueDate || null
      });
      setEntryModal(false);
      await fetchFees();
    } catch (err) {
      setEntryError(err.response?.data?.error || err.response?.data?.message || 'Failed to create fee entry.');
    } finally {
      setSaving(false);
    }
  };

  const exportRows = () => fees.map(f => [
    f.studentName,
    f.studentUsername || '',
    f.categoryName || f.feeType || '',
    f.totalAmount ?? f.amount ?? '',
    f.paidAmount ?? '',
    balanceOf(f),
    f.dueDate || '',
    f.status || 'PENDING'
  ]);
  const exportHeaders = ['Student', 'Enrollment No.', 'Fee Type', 'Total Amount', 'Paid', 'Balance', 'Due Date', 'Status'];
  const exportName = `fees_${allFees ? 'all' : 'pending'}_${new Date().toISOString().split('T')[0]}`;

  const extendedColumns = [
    ...COLUMNS,
    { key: 'paidAmount', label: 'Paid', render: (v) => `₹${Number(v ?? 0).toLocaleString('en-IN')}` },
    { key: 'balance', label: 'Balance', render: (_, f) => `₹${balanceOf(f).toLocaleString('en-IN')}` },
    {
      key: 'actions', label: 'Actions', render: (_, fee) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          {fee.status !== 'PAID' && (
            <button className="btn-icon" onClick={() => handlePayClick(fee)} title="Record payment" aria-label={`Record payment for ${fee.studentName || fee.id}`}>💳</button>
          )}
          {fee.status === 'PAID' && (
            <button className="btn-icon" onClick={() => handleReceiptClick(fee)} title="View receipt" aria-label={`View receipt for ${fee.studentName || fee.id}`}>🧾</button>
          )}
          <button className="btn-icon" onClick={() => handleHistoryClick(fee)} title="View payment history" aria-label={`View payment history for ${fee.studentName || fee.id}`}>📜</button>
        </div>
      )
    }
  ];

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">💰 Fee Management</h1>
        <div className="page-actions">
          <button
            className="btn btn-secondary"
            onClick={() => exportToCSV(exportHeaders, exportRows(), exportName)}>
            ⬇ Export CSV
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => exportToExcel(exportHeaders, exportRows(), exportName)}>
            ⬇ Export Excel
          </button>
          <button
            className="btn btn-primary"
            onClick={openEntryModal}>
            ＋ Add Fee Entry
          </button>
          <button
            className={allFees ? "btn btn-secondary" : "btn btn-primary"}
            onClick={() => setAllFees(false)}>
            Pending Fees
          </button>
          <button
            className={allFees ? "btn btn-primary" : "btn btn-secondary"}
            onClick={() => setAllFees(true)}>
            All Fees
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error} <button className="btn btn-sm btn-secondary" style={{ marginLeft: 12 }} onClick={fetchFees}>Retry</button></div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading fees…</span></div>
      ) : (
        <DataTable columns={extendedColumns} data={fees} emptyMessage={error ? 'Could not load fees.' : 'No fees found.'} error="" onRetry={error ? fetchFees : undefined} />
      )}

      {payModal && selectedFee && (
        <Modal isOpen={payModal} title={`Record Payment: ${selectedFee.studentName}`} onClose={() => { if (!saving) { setPayModal(false); setSelectedFee(null); } }} onSubmit={handlePaymentSubmit} submitLabel="Pay" submitting={saving} submitDisabled={saving}>
          {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
          <div style={{ marginBottom: 16 }}>
            <p><strong>Fee Type:</strong> {selectedFee.categoryName}</p>
            <p><strong>Remaining balance:</strong> ₹{balanceOf(selectedFee).toLocaleString('en-IN')}</p>
          </div>
          <div className="form-group">
            <label className="form-label">Payment Amount</label>
            <input type="number" className="form-control" min="0.01" step="0.01" max={Number.isFinite(balanceOf(selectedFee)) ? balanceOf(selectedFee) : undefined} value={payForm.amount} onChange={(e) => setPayForm((p) => ({ ...p, amount: e.target.value }))} />
          </div>
          <div className="form-group">
            <label className="form-label">Payment Mode</label>
            <select className="form-control" value={payForm.paymentMode} onChange={(e) => setPayForm((p) => ({ ...p, paymentMode: e.target.value }))}>
              <option value="CASH">Cash</option>
              <option value="ONLINE">Online/Card</option>
              <option value="CHEQUE">Cheque</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Remarks</label>
            <input type="text" className="form-control" value={payForm.remarks} onChange={(e) => setPayForm((p) => ({ ...p, remarks: e.target.value }))} />
          </div>
        </Modal>
      )}

      {historyModal && selectedFee && (
        <Modal isOpen={historyModal} title={`Payment History: ${selectedFee.studentName}`} onClose={() => setHistoryModal(false)} hideFooter>
          {historyError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{historyError} <button className="btn btn-sm btn-secondary" style={{ marginLeft: 12 }} onClick={() => handleHistoryClick(selectedFee)}>Retry</button></div>}
          {historyLoading ? <div className="loading-container"><div className="spinner" /><span>Loading history…</span></div> : history.length === 0 && !historyError ? <p>No payment history found.</p> : history.length > 0 && (
            <DataTable
              columns={[
                { key: 'receiptNumber', label: 'Receipt' },
                { key: 'paymentDate', label: 'Date' },
                { key: 'amount', label: 'Amount' },
                { key: 'paymentMode', label: 'Mode' },
                {
                  key: 'actions', label: 'Receipt', render: (_, p) => (
                    <button className="btn-icon" onClick={() => handleReceiptClick(selectedFee, p)} title="View Receipt" aria-label={`View receipt ${p.receiptNumber || ''}`}>🧾</button>
                  )
                }
              ]}
              data={history}
            />
          )}
        </Modal>
      )}

      {entryModal && (
        <Modal isOpen={entryModal} title="Add Fee Entry" onClose={() => { if (!saving) setEntryModal(false); }} onSubmit={handleAddFeeSubmit} submitLabel="Create Fee Entry" submitting={saving} submitDisabled={saving}>
          {bootstrapError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{bootstrapError}</div>}
          {entryError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{entryError}</div>}
          <div className="form-group">
            <label className="form-label">Student</label>
            <select className="form-control" value={entryForm.enrollmentId} onChange={(e) => setEntryForm((p) => ({ ...p, enrollmentId: e.target.value }))}>
              <option value="">Select student / enrollment…</option>
              {students.map(s => <option key={s.id} value={s.username}>{s.name} ({s.username})</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Fee Category</label>
            <select className="form-control" value={entryForm.categoryId} onChange={(e) => setEntryForm((p) => ({ ...p, categoryId: e.target.value }))}>
              <option value="">Select fee category…</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.categoryName}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Amount</label>
            <input type="number" className="form-control" min="0.01" step="0.01" value={entryForm.amount} onChange={(e) => setEntryForm((p) => ({ ...p, amount: e.target.value }))} placeholder="0.00" />
          </div>
          <div className="form-group">
            <label className="form-label">Due Date</label>
            <input type="date" className="form-control" min={new Date().toISOString().split('T')[0]} value={entryForm.dueDate} onChange={(e) => setEntryForm((p) => ({ ...p, dueDate: e.target.value }))} />
          </div>
        </Modal>
      )}

      {receiptFee && (
        <ReceiptModal fee={receiptFee} onClose={() => setReceiptFee(null)} />
      )}
    </div>
  );
};

export default FeesPage;
