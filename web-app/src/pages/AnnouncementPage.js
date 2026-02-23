import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAnnouncements, addAnnouncement, updateAnnouncement, deleteAnnouncement } from '../services/announcementService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: 'Title' },
  { key: 'content', label: 'Content', render: (v) => (
    <span className="announcement-content" title={v}>{v}</span>
  )},
  { key: 'targetRole', label: 'Target Role' },
  { key: 'createdAt', label: 'Created At', render: (v) => v ? new Date(v).toLocaleDateString() : '—' },
];

const EMPTY_FORM = { title: '', content: '', targetRole: 'ALL' };
const ROLES = ['ALL', 'STUDENT', 'FACULTY', 'ADMIN'];

const AnnouncementPage = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchData = () => {
    setLoading(true);
    getAnnouncements()
      .then((res) => setItems(res.data || []))
      .catch(() => setError('Failed to load announcements.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const handleFormChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setFormError('');
  };

  const openAdd = () => { setForm(EMPTY_FORM); setEditId(null); setFormError(''); setModalOpen(true); };
  const openEdit = (row) => { setForm({ title: row.title || '', content: row.content || '', targetRole: row.targetRole || 'ALL' }); setEditId(row.id); setFormError(''); setModalOpen(true); };

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

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📢 Announcements</h1>
        <button className="btn btn-primary" onClick={openAdd}>+ Add Announcement</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading announcements…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={items} onEdit={openEdit} onDelete={handleDelete} emptyMessage="No announcements found." />
      )}

      <Modal isOpen={modalOpen} title={editId ? 'Edit Announcement' : 'Add Announcement'} onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Title</label>
          <input name="title" type="text" className="form-control" value={form.title} onChange={handleFormChange} placeholder="Enter title" />
        </div>
        <div className="form-group">
          <label className="form-label">Content</label>
          <textarea name="content" className="form-control" rows={4} value={form.content} onChange={handleFormChange} placeholder="Enter announcement content" style={{ resize: 'vertical' }} />
        </div>
        <div className="form-group">
          <label className="form-label">Target Role</label>
          <select name="targetRole" className="form-control" value={form.targetRole} onChange={handleFormChange}>
            {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
          </select>
        </div>
      </Modal>
    </div>
  );
};

export default AnnouncementPage;
