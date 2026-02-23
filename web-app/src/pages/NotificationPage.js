import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getNotifications, createNotification } from '../services/notificationService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'message', label: 'Message' },
  { key: 'recipientRole', label: 'Recipient' },
  { key: 'createdAt', label: 'Created At', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
  { key: 'read', label: 'Read', render: (v) => (
    <span className={`badge badge-${v ? 'success' : 'warning'}`}>{v ? 'Read' : 'Unread'}</span>
  )},
];

const EMPTY_FORM = { message: '', recipientRole: 'ALL' };
const ROLES = ['ALL', 'STUDENT', 'FACULTY', 'ADMIN'];

const NotificationPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchData = () => {
    setLoading(true);
    getNotifications()
      .then((res) => setNotifications(res.data || []))
      .catch(() => setError('Failed to load notifications.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const handleSave = async () => {
    if (!form.message) { setFormError('Message is required.'); return; }
    setSaving(true);
    try {
      await createNotification(form);
      setModalOpen(false);
      setForm(EMPTY_FORM);
      fetchData();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create notification.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🔔 Notifications</h1>
        <button className="btn btn-primary" onClick={() => { setForm(EMPTY_FORM); setFormError(''); setModalOpen(true); }}>
          + Create Notification
        </button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading notifications…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={notifications} emptyMessage="No notifications found." />
      )}

      <Modal isOpen={modalOpen} title="Create Notification" onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Sending…' : 'Send'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Message</label>
          <textarea name="message" className="form-control" rows={3} value={form.message} onChange={(e) => { setForm((p) => ({ ...p, message: e.target.value })); setFormError(''); }} placeholder="Enter notification message" style={{ resize: 'vertical' }} />
        </div>
        <div className="form-group">
          <label className="form-label">Recipient Role</label>
          <select name="recipientRole" className="form-control" value={form.recipientRole} onChange={(e) => setForm((p) => ({ ...p, recipientRole: e.target.value }))}>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
          </select>
        </div>
      </Modal>
    </div>
  );
};

export default NotificationPage;
