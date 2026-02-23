import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllFaculty, createFaculty, updateFaculty, deleteFaculty, searchFaculty } from '../services/facultyService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Name' },
  { key: 'email', label: 'Email' },
  { key: 'department', label: 'Department' },
  { key: 'qualification', label: 'Qualification' },
];

const EMPTY_FORM = { name: '', email: '', phone: '', department: '', qualification: '' };

const FacultyManagementPage = () => {
  const [faculty, setFaculty] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchFaculty = () => {
    setLoading(true);
    getAllFaculty()
      .then((res) => setFaculty(res.data || []))
      .catch(() => setError('Failed to load faculty.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchFaculty(); }, []);

  const handleSearch = async () => {
    if (!search.trim()) return fetchFaculty();
    setLoading(true);
    try {
      const res = await searchFaculty(search);
      setFaculty(res.data || []);
    } catch {
      setError('Search failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleFormChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setFormError('');
  };

  const openAdd = () => { setForm(EMPTY_FORM); setEditId(null); setModalOpen(true); };
  const openEdit = (row) => { setForm({ name: row.name || '', email: row.email || '', phone: row.phone || '', department: row.department || '', qualification: row.qualification || '' }); setEditId(row.id); setModalOpen(true); };

  const handleSave = async () => {
    if (!form.name || !form.email) { setFormError('Name and email are required.'); return; }
    setSaving(true);
    try {
      if (editId) {
        await updateFaculty(editId, form);
      } else {
        await createFaculty(form);
      }
      setModalOpen(false);
      fetchFaculty();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to save faculty.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (row) => {
    if (!window.confirm(`Delete faculty "${row.name}"?`)) return;
    try {
      await deleteFaculty(row.id);
      fetchFaculty();
    } catch {
      setError('Failed to delete faculty.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">👩‍🏫 Faculty Management</h1>
        <div className="page-actions">
          <div className="search-bar">
            <input
              type="text"
              className="form-control"
              placeholder="Search faculty…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button className="btn btn-secondary btn-sm" onClick={handleSearch}>Search</button>
          </div>
          <button className="btn btn-primary" onClick={openAdd}>+ Add Faculty</button>
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading faculty…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={faculty} onEdit={openEdit} onDelete={handleDelete} emptyMessage="No faculty found." />
      )}

      <Modal
        isOpen={modalOpen}
        title={editId ? 'Edit Faculty' : 'Add Faculty'}
        onClose={() => setModalOpen(false)}
        onSubmit={handleSave}
        submitLabel={saving ? 'Saving…' : 'Save'}
      >
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {['name', 'email', 'phone', 'department', 'qualification'].map((field) => (
          <div className="form-group" key={field}>
            <label className="form-label" htmlFor={`faculty-${field}`}>
              {field.charAt(0).toUpperCase() + field.slice(1)}
            </label>
            <input
              id={`faculty-${field}`}
              name={field}
              type={field === 'email' ? 'email' : 'text'}
              className="form-control"
              value={form[field]}
              onChange={handleFormChange}
              placeholder={`Enter ${field}`}
            />
          </div>
        ))}
      </Modal>
    </div>
  );
};

export default FacultyManagementPage;
