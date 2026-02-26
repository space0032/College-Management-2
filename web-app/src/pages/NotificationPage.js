import SessionManager from '../utils/SessionManager';
import React, { useEffect, useState } from 'react';
import Modal from '../components/Modal';
import { getNotifications, createNotification } from '../services/notificationService';

const EMPTY_FORM = { message: '', recipientRole: 'ALL' };
const ROLES = ['ALL', 'STUDENT', 'FACULTY', 'ADMIN'];

const relTime = (dateStr) => {
  if (!dateStr) return '';
  const diff = Date.now() - new Date(dateStr).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return 'just now';
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  return `${Math.floor(h / 24)}d ago`;
};

const NotificationPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [filterRead, setFilterRead] = useState('all'); // 'all' | 'unread' | 'read'
  const [filterRole, setFilterRole] = useState('ALL');
  const [readIds, setReadIds] = useState(() => {
    // Persist read state in localStorage
    try { return new Set(JSON.parse(localStorage.getItem('readNotifications') || '[]')); } catch { return new Set(); }
  });

  const isAdmin = SessionManager.hasRole('ADMIN');

  const fetchData = () => {
    setLoading(true);
    getNotifications()
      .then((res) => setNotifications(res.data || []))
      .catch(() => setError('Failed to load notifications.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchData();
    // Poll every 30 seconds for new notifications
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, []);

  const markRead = (id) => {
    const next = new Set(readIds);
    next.add(id);
    setReadIds(next);
    localStorage.setItem('readNotifications', JSON.stringify([...next]));
  };

  const markUnread = (id) => {
    const next = new Set(readIds);
    next.delete(id);
    setReadIds(next);
    localStorage.setItem('readNotifications', JSON.stringify([...next]));
  };

  const markAllRead = () => {
    const next = new Set([...readIds, ...notifications.map(n => n.id)]);
    setReadIds(next);
    localStorage.setItem('readNotifications', JSON.stringify([...next]));
  };

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

  const isRead = (n) => readIds.has(n.id) || n.read;

  const filtered = notifications
    .filter(n => filterRole === 'ALL' || n.recipientRole === filterRole || n.recipientRole === 'ALL')
    .filter(n => filterRead === 'all' || (filterRead === 'unread' && !isRead(n)) || (filterRead === 'read' && isRead(n)));

  const unreadCount = notifications.filter(n => !isRead(n)).length;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">
          🔔 Notifications
          {unreadCount > 0 && (
            <span style={{ marginLeft: '10px', background: '#e53e3e', color: 'white', borderRadius: '20px', padding: '2px 10px', fontSize: '0.7rem', fontWeight: '700', verticalAlign: 'middle' }}>
              {unreadCount} new
            </span>
          )}
        </h1>
        <div style={{ display: 'flex', gap: '8px' }}>
          {unreadCount > 0 && (
            <button className="btn btn-secondary" onClick={markAllRead} style={{ fontSize: '0.82rem' }}>
              ✓ Mark All Read
            </button>
          )}
          {isAdmin && (
            <button className="btn btn-primary" onClick={() => { setForm(EMPTY_FORM); setFormError(''); setModalOpen(true); }}>
              + Create Notification
            </button>
          )}
        </div>
      </div>

      {/* Stats */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '18px', flexWrap: 'wrap' }}>
        {[
          { label: 'Total', value: notifications.length, color: '#2b6cb0', bg: '#ebf8ff' },
          { label: 'Unread', value: unreadCount, color: '#c53030', bg: '#fff5f5' },
          { label: 'Read', value: notifications.length - unreadCount, color: '#276749', bg: '#f0fff4' },
        ].map(s => (
          <div key={s.label} style={{ padding: '8px 16px', background: s.bg, borderRadius: '8px', minWidth: '90px', textAlign: 'center', cursor: 'pointer' }}
            onClick={() => setFilterRead(s.label === 'Total' ? 'all' : s.label.toLowerCase())}>
            <div style={{ fontWeight: 'bold', fontSize: '1.2rem', color: s.color }}>{s.value}</div>
            <div style={{ fontSize: '0.73rem', color: '#718096' }}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '18px', alignItems: 'center', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden' }}>
          {[{ id: 'all', label: 'All' }, { id: 'unread', label: '🔴 Unread' }, { id: 'read', label: '✓ Read' }].map(f => (
            <button key={f.id} onClick={() => setFilterRead(f.id)} style={{
              padding: '6px 14px', border: 'none', cursor: 'pointer', fontSize: '0.82rem',
              background: filterRead === f.id ? '#3b82f6' : 'white',
              color: filterRead === f.id ? 'white' : '#4a5568', fontWeight: filterRead === f.id ? '600' : '400'
            }}>{f.label}</button>
          ))}
        </div>
        <select value={filterRole} onChange={e => setFilterRole(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '0.87rem' }}>
          <option value="ALL">All Roles</option>
          {ROLES.filter(r => r !== 'ALL').map(r => <option key={r} value={r}>{r}</option>)}
        </select>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading notifications…</span></div>
      ) : filtered.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
          <div style={{ fontSize: '2.5rem' }}>🔔</div>
          <div>No notifications{filterRead !== 'all' ? ` in "${filterRead}" filter` : ''}.</div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {filtered.map(n => {
            const read = isRead(n);
            return (
              <div key={n.id} style={{
                background: read ? 'white' : '#fffaf0',
                border: `1px solid ${read ? '#e2e8f0' : '#fbd38d'}`,
                borderLeft: `4px solid ${read ? '#e2e8f0' : '#ed8936'}`,
                borderRadius: '10px', padding: '12px 16px',
                display: 'flex', gap: '12px', alignItems: 'center'
              }}>
                <div style={{ fontSize: '1.3rem', flexShrink: 0 }}>
                  {read ? '🔕' : '🔔'}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ color: '#2d3748', fontSize: '0.9rem', fontWeight: read ? '400' : '600' }}>{n.message}</div>
                  <div style={{ display: 'flex', gap: '10px', marginTop: '4px', alignItems: 'center' }}>
                    {n.recipientRole && n.recipientRole !== 'ALL' && (
                      <span style={{ background: '#f1f5f9', color: '#64748b', padding: '1px 8px', borderRadius: '12px', fontSize: '0.72rem' }}>
                        {n.recipientRole}
                      </span>
                    )}
                    <span style={{ fontSize: '0.73rem', color: '#a0aec0' }}>{relTime(n.createdAt)}</span>
                    {!read && <span style={{ background: '#fed7aa', color: '#c05621', padding: '1px 8px', borderRadius: '12px', fontSize: '0.72rem', fontWeight: '600' }}>NEW</span>}
                  </div>
                </div>
                <button
                  onClick={() => read ? markUnread(n.id) : markRead(n.id)}
                  title={read ? 'Mark as unread' : 'Mark as read'}
                  style={{
                    background: 'none', border: '1px solid #e2e8f0', borderRadius: '6px',
                    cursor: 'pointer', padding: '4px 10px', fontSize: '0.78rem', color: '#4a5568', whiteSpace: 'nowrap'
                  }}
                >
                  {read ? '↩ Unread' : '✓ Read'}
                </button>
              </div>
            );
          })}
        </div>
      )}

      <Modal isOpen={modalOpen} title="Create Notification" onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Sending…' : 'Send'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Message *</label>
          <textarea name="message" className="form-control" rows={3} value={form.message}
            onChange={(e) => { setForm((p) => ({ ...p, message: e.target.value })); setFormError(''); }}
            placeholder="Enter notification message" style={{ resize: 'vertical' }} />
        </div>
        <div className="form-group">
          <label className="form-label">Recipient Role</label>
          <select name="recipientRole" className="form-control" value={form.recipientRole}
            onChange={(e) => setForm((p) => ({ ...p, recipientRole: e.target.value }))}>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
          </select>
        </div>
      </Modal>
    </div>
  );
};

export default NotificationPage;
