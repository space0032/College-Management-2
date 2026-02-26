import React, { useEffect, useState } from 'react';
import Modal from '../components/Modal';
import { getAnnouncements, addAnnouncement, updateAnnouncement, deleteAnnouncement } from '../services/announcementService';

const EMPTY_FORM = { title: '', content: '', targetRole: 'ALL', priority: 'NORMAL', pinned: false };
const ROLES = ['ALL', 'STUDENT', 'FACULTY', 'ADMIN'];
const PRIORITIES = ['LOW', 'NORMAL', 'HIGH'];

const PRIORITY_STYLE = {
  HIGH: { bg: '#fff5f5', border: '#fc8181', badge: '#e53e3e', label: '🔴 HIGH' },
  NORMAL: { bg: 'white', border: '#e2e8f0', badge: '#3182ce', label: '🔵 NORMAL' },
  LOW: { bg: '#f7fafc', border: '#e2e8f0', badge: '#68d391', label: '🟢 LOW' },
};

const relTime = (dateStr) => {
  if (!dateStr) return '';
  const diff = Date.now() - new Date(dateStr).getTime();
  const m = Math.floor(diff / 60000);
  if (m < 1) return 'just now';
  if (m < 60) return `${m}m ago`;
  const h = Math.floor(m / 60);
  if (h < 24) return `${h}h ago`;
  const d = Math.floor(h / 24);
  return `${d}d ago`;
};

const AnnouncementPage = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [filterRole, setFilterRole] = useState('ALL');
  const [filterPriority, setFilterPriority] = useState('ALL');
  const [searchQ, setSearchQ] = useState('');

  const user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
  const isAdmin = SessionManager.hasRole('ADMIN');

  const fetchData = () => {
    setLoading(true);
    getAnnouncements()
      .then((res) => setItems(res.data || []))
      .catch(() => setError('Failed to load announcements.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const handleFormChange = (e) => {
    const val = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((prev) => ({ ...prev, [e.target.name]: val }));
    setFormError('');
  };

  const openAdd = () => { setForm(EMPTY_FORM); setEditId(null); setFormError(''); setModalOpen(true); };
  const openEdit = (row) => {
    setForm({
      title: row.title || '', content: row.content || '',
      targetRole: row.targetRole || 'ALL', priority: row.priority || 'NORMAL',
      pinned: row.pinned || false
    });
    setEditId(row.id); setFormError(''); setModalOpen(true);
  };

  const handleSave = async () => {
    if (!form.title || !form.content) { setFormError('Title and content are required.'); return; }
    setSaving(true);
    try {
      if (editId) {
        await updateAnnouncement(editId, form);
      } else {
        await addAnnouncement(form);
      }
      setModalOpen(false);
      fetchData();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to save announcement.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (row) => {
    if (!window.confirm(`Delete announcement "${row.title}"?`)) return;
    try { await deleteAnnouncement(row.id); fetchData(); } catch { setError('Failed to delete announcement.'); }
  };

  const handleTogglePin = async (row) => {
    try {
      await updateAnnouncement(row.id, { ...row, pinned: !row.pinned });
      fetchData();
    } catch { /* silent */ }
  };

  // Filtered + sorted: pinned first, then by priority (HIGH > NORMAL > LOW), then by date
  const filtered = items
    .filter(a => filterRole === 'ALL' || a.targetRole === filterRole || a.targetRole === 'ALL')
    .filter(a => filterPriority === 'ALL' || a.priority === filterPriority)
    .filter(a => !searchQ || (a.title || '').toLowerCase().includes(searchQ.toLowerCase()) || (a.content || '').toLowerCase().includes(searchQ.toLowerCase()))
    .sort((a, b) => {
      if (!!b.pinned !== !!a.pinned) return b.pinned ? 1 : -1;
      const pOrder = { HIGH: 0, NORMAL: 1, LOW: 2 };
      if ((pOrder[a.priority] ?? 1) !== (pOrder[b.priority] ?? 1)) return (pOrder[a.priority] ?? 1) - (pOrder[b.priority] ?? 1);
      return new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
    });

  const pinnedCount = items.filter(a => a.pinned).length;
  const highCount = items.filter(a => a.priority === 'HIGH').length;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📢 Announcements</h1>
        {isAdmin && <button className="btn btn-primary" onClick={openAdd}>+ Add Announcement</button>}
      </div>

      {/* Stats row */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '18px', flexWrap: 'wrap' }}>
        {[
          { label: 'Total', value: items.length, color: '#2b6cb0', bg: '#ebf8ff' },
          { label: '🔴 High Priority', value: highCount, color: '#c53030', bg: '#fff5f5' },
          { label: '📌 Pinned', value: pinnedCount, color: '#9f7aea', bg: '#faf5ff' },
        ].map(s => (
          <div key={s.label} style={{ padding: '8px 16px', background: s.bg, borderRadius: '8px', minWidth: '100px', textAlign: 'center' }}>
            <div style={{ fontWeight: 'bold', fontSize: '1.2rem', color: s.color }}>{s.value}</div>
            <div style={{ fontSize: '0.73rem', color: '#718096' }}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Filters */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '18px', flexWrap: 'wrap', alignItems: 'center' }}>
        <input type="text" placeholder="Search announcements…" value={searchQ} onChange={e => setSearchQ(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', flex: '1 1 200px', fontSize: '0.87rem' }} />
        <select value={filterRole} onChange={e => setFilterRole(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '0.87rem' }}>
          <option value="ALL">All Roles</option>
          {ROLES.filter(r => r !== 'ALL').map(r => <option key={r} value={r}>{r}</option>)}
        </select>
        <select value={filterPriority} onChange={e => setFilterPriority(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '0.87rem' }}>
          <option value="ALL">All Priorities</option>
          {PRIORITIES.map(p => <option key={p} value={p}>{p}</option>)}
        </select>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading announcements…</span></div>
      ) : filtered.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
          <div style={{ fontSize: '2.5rem' }}>📭</div>
          <div>No announcements match your filters.</div>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {filtered.map(a => {
            const ps = PRIORITY_STYLE[a.priority] || PRIORITY_STYLE.NORMAL;
            return (
              <div key={a.id} style={{
                background: ps.bg, border: `1px solid ${ps.border}`,
                borderLeft: `4px solid ${ps.badge}`,
                borderRadius: '10px', padding: '14px 18px',
                display: 'flex', gap: '14px', alignItems: 'flex-start',
                boxShadow: a.pinned ? '0 2px 8px rgba(0,0,0,0.08)' : 'none',
                transition: 'box-shadow 0.15s'
              }}>
                <div style={{ fontSize: '1.4rem', flexShrink: 0, marginTop: '2px' }}>
                  {a.pinned ? '📌' : '📢'}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '4px' }}>
                    <span style={{ fontWeight: '700', fontSize: '0.95rem', color: '#2d3748' }}>{a.title}</span>
                    <span style={{ padding: '1px 8px', borderRadius: '12px', fontSize: '0.72rem', fontWeight: '600', background: ps.badge, color: 'white' }}>
                      {a.priority || 'NORMAL'}
                    </span>
                    {a.targetRole && a.targetRole !== 'ALL' && (
                      <span style={{ padding: '1px 8px', borderRadius: '12px', fontSize: '0.72rem', fontWeight: '600', background: '#f1f5f9', color: '#64748b' }}>
                        {a.targetRole}
                      </span>
                    )}
                    {a.pinned && <span style={{ fontSize: '0.72rem', color: '#9f7aea', fontWeight: '600' }}>📌 Pinned</span>}
                  </div>
                  <div style={{ color: '#4a5568', fontSize: '0.875rem', lineHeight: '1.5', marginBottom: '6px' }}>{a.content}</div>
                  <div style={{ fontSize: '0.75rem', color: '#a0aec0' }}>{relTime(a.createdAt)}</div>
                </div>
                {isAdmin && (
                  <div style={{ display: 'flex', gap: '6px', flexShrink: 0 }}>
                    <button title={a.pinned ? 'Unpin' : 'Pin'} onClick={() => handleTogglePin(a)} style={{ background: 'none', border: '1px solid #e2e8f0', borderRadius: '6px', cursor: 'pointer', padding: '4px 8px', fontSize: '0.8rem' }}>
                      {a.pinned ? '📍' : '📌'}
                    </button>
                    <button className="btn btn-secondary btn-sm" onClick={() => openEdit(a)}>Edit</button>
                    <button className="btn btn-danger btn-sm" onClick={() => handleDelete(a)}>Delete</button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      <Modal isOpen={modalOpen} title={editId ? 'Edit Announcement' : 'Add Announcement'} onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Title *</label>
          <input name="title" type="text" className="form-control" value={form.title} onChange={handleFormChange} placeholder="Enter title" />
        </div>
        <div className="form-group">
          <label className="form-label" style={{ display: 'flex', justifyContent: 'space-between' }}>
            <span>Content *</span>
            <span style={{ fontSize: '0.8rem', color: form.content.length > 500 ? '#e53e3e' : '#a0aec0' }}>
              {form.content.length}/500 {form.content.length >= 500 ? '(Max)' : ''}
            </span>
          </label>
          <textarea name="content" className="form-control" rows={4} maxLength={500} value={form.content} onChange={handleFormChange} placeholder="Enter announcement content" style={{ resize: 'vertical' }} />
        </div>
        <div className="form-group">
          <label className="form-label">Target Role</label>
          <select name="targetRole" className="form-control" value={form.targetRole} onChange={handleFormChange}>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Priority</label>
          <select name="priority" className="form-control" value={form.priority} onChange={handleFormChange}>
            {PRIORITIES.map(p => <option key={p} value={p}>{p}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
            <input type="checkbox" name="pinned" checked={form.pinned} onChange={handleFormChange} />
            <span className="form-label" style={{ margin: 0 }}>📌 Pin this announcement (shows at top)</span>
          </label>
        </div>
      </Modal>
    </div>
  );
};

export default AnnouncementPage;
