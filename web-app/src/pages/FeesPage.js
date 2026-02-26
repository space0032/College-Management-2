import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getPendingFees, getAllFees, recordPayment, getPaymentHistory } from '../services/feesService';
import { exportToCSV } from '../utils/exportUtils';
import ReceiptModal from '../components/ReceiptModal';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'studentId', label: 'Student ID' },
  { key: 'studentName', label: 'Student Name' },
  { key: 'amount', label: 'Amount' },
  { key: 'dueDate', label: 'Due Date' },
  { key: 'feeType', label: 'Fee Type' },
  {
    key: 'status', label: 'Status', render: (v) => (
      <span className={`badge badge-${v === 'PAID' ? 'success' : 'danger'}`}>{v || 'PENDING'}</span>
    )
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

  const fetchFees = React.useCallback(() => {
    setLoading(true);
    const apiCall = allFees ? getAllFees : getPendingFees;
    apiCall()
      .then((res) => setFees(res.data || []))
      .catch(() => setError('Failed to load fees.'))
      .finally(() => setLoading(false));
  }, [allFees]);

  useEffect(() => {
    fetchFees();
  }, [fetchFees]);

  const handlePayClick = (fee) => {
    setSelectedFee(fee);
    setPayForm({ amount: (fee.totalAmount - fee.paidAmount).toString(), paymentMode: 'ONLINE', remarks: '' });
    setFormError('');
    setPayModal(true);
  };

  const handleHistoryClick = async (fee) => {
    setSelectedFee(fee);
    setHistoryModal(true);
    try {
      const res = await getPaymentHistory(fee.id);
      setHistory(res.data || []);
    } catch {
      setHistory([]);
    }
  };

  const handlePaymentSubmit = async () => {
    if (!payForm.amount || isNaN(payForm.amount) || Number(payForm.amount) <= 0) {
      setFormError('Please enter a valid amount.');
      return;
    }
    setSaving(true);
    try {
      await recordPayment({
        studentFeeId: selectedFee.id,
        amount: Number(payForm.amount),
        paymentMode: payForm.paymentMode,
        remarks: payForm.remarks
      });
      setPayModal(false);
      fetchFees();
    } catch (err) {
      setFormError(err.response?.data?.error || 'Failed to record payment.');
    } finally {
      setSaving(false);
    }
  };

  const extendedColumns = [
    ...COLUMNS,
    { key: 'totalAmount', label: 'Total Amount' },
    { key: 'paidAmount', label: 'Paid' },
    {
      key: 'actions', label: 'Actions', render: (_, fee) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          {fee.status !== 'PAID' && (
            <button className="btn-icon" onClick={() => handlePayClick(fee)} title="Pay Now">💳</button>
          )}
          {fee.status === 'PAID' && (
            <button className="btn-icon" onClick={() => setReceiptFee(fee)} title="View Receipt">🧾</button>
          )}
          <button className="btn-icon" onClick={() => handleHistoryClick(fee)} title="View History">📜</button>
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
            onClick={() => exportToCSV(
              ['Student', 'Fee Type', 'Amount', 'Due Date', 'Status', 'Total', 'Paid'],
              fees.map(f => [f.studentName, f.feeType, f.amount, f.dueDate, f.status, f.totalAmount, f.paidAmount]),
              'fees_export'
            )}>
            ⬇ Export CSV
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

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading fees…</span></div>
      ) : (
        <DataTable columns={extendedColumns} data={fees} emptyMessage="No fees found." />
      )}

      {payModal && selectedFee && (
        <Modal isOpen={payModal} title={`Record Payment: ${selectedFee.studentName}`} onClose={() => setPayModal(false)} onSubmit={handlePaymentSubmit} submitLabel={saving ? 'Processing...' : 'Pay'}>
          {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
          <div style={{ marginBottom: 16 }}>
            <p><strong>Fee Type:</strong> {selectedFee.categoryName}</p>
            <p><strong>Total Due:</strong> ${(selectedFee.totalAmount - selectedFee.paidAmount).toFixed(2)}</p>
          </div>
          <div className="form-group">
            <label className="form-label">Payment Amount</label>
            <input type="number" className="form-control" value={payForm.amount} onChange={(e) => setPayForm((p) => ({ ...p, amount: e.target.value }))} />
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
        <Modal isOpen={historyModal} title={`Payment History: ${selectedFee.studentName}`} onClose={() => setHistoryModal(false)}>
          {history.length === 0 ? <p>No payment history found.</p> : (
            <DataTable
              columns={[
                { key: 'receiptNumber', label: 'Receipt' },
                { key: 'paymentDate', label: 'Date' },
                { key: 'amount', label: 'Amount' },
                { key: 'paymentMode', label: 'Mode' },
                {
                  key: 'actions', label: 'Receipt', render: (_, p) => (
                    <button className="btn-icon" onClick={() => setReceiptFee({ ...selectedFee, amount: p.amount, id: p.id, receiptNumber: p.receiptNumber, paidDate: p.paymentDate })} title="Print Receipt">🧾</button>
                  )
                }
              ]}
              data={history}
            />
          )}
        </Modal>
      )}

      {receiptFee && (
        <ReceiptModal fee={receiptFee} onClose={() => setReceiptFee(null)} />
      )}
    </div>
  );
};

export default FeesPage;
