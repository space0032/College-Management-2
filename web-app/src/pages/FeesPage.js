import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import { getPendingFees } from '../services/feesService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'studentId', label: 'Student ID' },
  { key: 'studentName', label: 'Student Name' },
  { key: 'amount', label: 'Amount' },
  { key: 'dueDate', label: 'Due Date' },
  { key: 'feeType', label: 'Fee Type' },
  { key: 'status', label: 'Status', render: (v) => (
    <span className={`badge badge-${v === 'PAID' ? 'success' : 'danger'}`}>{v || 'PENDING'}</span>
  )},
];

const FeesPage = () => {
  const [fees, setFees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getPendingFees()
      .then((res) => setFees(res.data || []))
      .catch(() => setError('Failed to load fees.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">💰 Fee Management</h1>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading fees…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={fees} emptyMessage="No pending fees found." />
      )}
    </div>
  );
};

export default FeesPage;
