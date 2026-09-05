import React, { useEffect, useMemo, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getDepartments } from '../services/departmentService';
import { getSpecializations, addSpecialization, updateSpecialization, deleteSpecialization } from '../services/specializationService';
import SessionManager from '../utils/SessionManager';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Track' },
  { key: 'code', label: 'Code' },
  { key: 'departmentName', label: 'Department' },
  {
    key: 'isActive', label: 'Status', render: (v) => (
      <span className={`badge ${v === false ? 'badge-secondary' : 'badge-success'}`}>
        {v === false ? 'Inactive' : 'Active'}
      </span>
    )
  },
];

const EMPTY_FORM = { departmentId: '', name: '', code: '', description: '', isActive: true };

const SpecializationManagementPage = () => {
  const [tracks, setTracks] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterDept, setFilterDept] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const canManage = SessionManager.hasRole('ADMIN');

  const fetchData = () => {
    setLoading(true);
    Promise.all([getSpecializations(), getDepartments()])
      .then(([specRes, deptRes]) => {
        setTracks(specRes.data || []);
        setDepartments(deptRes.data || []);
      })
      .catch(() => setError('Failed to load tracks.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  const filtered = useMemo(() => tracks.filter(t => !filterDept || String(t.departmentId) === String(filterDept)), [tracks, filterDept]);

  const openAdd = () => {
    setForm({ ...EMPTY_FORM, departmentId: filterDept || '' });
    setEditId(null);
    setFormError('');
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setForm({
      departmentId: row.departmentId ? String(row.departmentId) : '',
      name: row.name || '',
      code: row.code || '',
      description: row.description || '',
      isActive: row.isActive !== false,
    });
    setEditId(row.id);
    setFormError('');
    setModalOpen(true);
  };

  const handleSave = async () => {
    if (!form.departmentId) { setFormError('Department is required.'); return; }
    if (!form.name.trim()) { setFormError('Track name is required.'); return; }
    setSaving(true);
    try {
      const payload = {
        departmentId: Number(form.departmentId),
        name: form.name.trim(),
        code: (form.code || '').trim(),
        description: (form.description || '').trim(),
        active: !!form.isActive,
      };
      if (editId) await updateSpecialization(editId, payload);
      else await addSpecialization(payload);
      setModalOpen(false);
      fetchData();
    } catch (err) {
      setFormError(err.response?.data?.error || err.response?.data?.message || 'Failed to save track.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (row) => {
    if (!window.confirm(`Delete track "${row.name}"? Blocked if subjects or students use it.`)) return;
    try {
      await deleteSpecialization(row.id);
      fetchData();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to delete track.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">🎯 Tracks / Specializations</h1>
          <p className="page-subtitle">Tracks inside departments drive subject auto-enrollment (e.g. Cyber Security inside Computer Engineering)</p>
        </div>
        {canManage && <button className="btn btn-primary" onClick={openAdd}>+ Add Track</button>}
      </div>

      <div style={{ display: 'flex', gap: '10px', marginBottom: '18px', alignItems: 'center' }}>
        <select value={filterDept} onChange={e => setFilterDept(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '0.87rem' }}>
          <option value="">All Departments</option>
          {departments.map(d => <option key={d.id} value={d.id}>{d.name} ({d.code})</option>)}
        </select>
        {filtered.length > 0 && (
          <span style={{ fontSize: '0.82rem', color: '#718096' }}>{filtered.length} track{filtered.length !== 1 ? 's' : ''}</span>
        )}
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading tracks…</span></div>
      ) : (
        <DataTable
          columns={COLUMNS}
          data={filtered}
          onEdit={canManage ? openEdit : undefined}
          onDelete={canManage ? handleDelete : undefined}
          emptyMessage="No tracks found. Add one to offer subject choices per department."
        />
      )}

      <Modal isOpen={modalOpen} title={editId ? 'Edit Track' : 'Add Track'} onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Department *</label>
          <select name="departmentId" required className="form-control" value={form.departmentId} onChange={e => setForm({ ...form, departmentId: e.target.value })}>
            <option value="">Select department</option>
            {departments.map(d => <option key={d.id} value={d.id}>{d.name} ({d.code})</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Track Name *</label>
          <input name="name" type="text" required className="form-control" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="e.g. Cyber Security" />
        </div>
        <div className="form-group">
          <label className="form-label">Code</label>
          <input name="code" type="text" className="form-control" value={form.code} onChange={e => setForm({ ...form, code: e.target.value })} placeholder="e.g. CS" />
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea name="description" className="form-control" rows={2} value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="What this track covers" />
        </div>
        <div className="form-group">
          <label className="form-label" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <input type="checkbox" checked={!!form.isActive} onChange={e => setForm({ ...form, isActive: e.target.checked })} /> Active (inactive tracks hide from student/subject dropdowns)
          </label>
        </div>
      </Modal>
    </div>
  );
};

export default SpecializationManagementPage;
