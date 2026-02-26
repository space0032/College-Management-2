import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getDepartments, addDepartment, updateDepartment, deleteDepartment } from '../services/departmentService';
import SessionManager from '../utils/SessionManager';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Name' },
  { key: 'description', label: 'Description' },
];

const EMPTY_FORM = { name: '', description: '' };

const DepartmentPage = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const canManage = SessionManager.hasRole('ADMIN') /* or hasPermission */;

  const fetchData = () => {
    setLoading(true);
    getDepartments()
      .then((res) => setDepartments(res.data || []))
      .catch(() => setError('Failed to load departments.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const handleFormChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setFormError('');
  };

  const openAdd = () => { setForm(EMPTY_FORM); setEditId(null); setFormError(''); setModalOpen(true); };
  const openEdit = (row) => { setForm({ name: row.name || '', description: row.description || '' }); setEditId(row.id); setFormError(''); setModalOpen(true); };

  const handleSave = async () => {
    if (!form.name) { setFormError('Department name is required.'); return; }
    setSaving(true);
    try {
      if (editId) {
        await updateDepartment(editId, form);
      } else {
        await addDepartment(form);
      }
      setModalOpen(false);
      fetchData();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to save department.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (row) => {
    if (!window.confirm(`Delete department "${row.name}"?`)) return;
    try { await deleteDepartment(row.id); fetchData(); } catch { setError('Failed to delete department.'); }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🏛️ Departments</h1>
        {canManage && <button className="btn btn-primary" onClick={openAdd}>+ Add Department</button>}
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading departments…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={departments} onEdit={canManage ? openEdit : undefined} onDelete={canManage ? handleDelete : undefined} emptyMessage="No departments found." />
      )}

      <Modal isOpen={modalOpen} title={editId ? 'Edit Department' : 'Add Department'} onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Department Name</label>
          <input name="name" type="text" className="form-control" value={form.name} onChange={handleFormChange} placeholder="Enter department name" />
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea name="description" className="form-control" rows={3} value={form.description} onChange={handleFormChange} placeholder="Enter description" style={{ resize: 'vertical' }} />
        </div>
      </Modal>
    </div>
  );
};

export default DepartmentPage;
