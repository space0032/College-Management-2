import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getDepartments, addDepartment, updateDepartment, deleteDepartment } from '../services/departmentService';
import { getFeeCategories, getProgramFees, saveProgramFees } from '../services/feesService';
import SessionManager from '../utils/SessionManager';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Name' },
  { key: 'description', label: 'Description' },
];

const EMPTY_FORM = { name: '', code: '', description: '' };
const currentYear = () => String(new Date().getFullYear());

const DepartmentPage = () => {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const [categories, setCategories] = useState([]);
  const [feeAmounts, setFeeAmounts] = useState({});
  const [feeYear, setFeeYear] = useState(currentYear());
  const [feeNote, setFeeNote] = useState('');

  const canManage = SessionManager.hasRole('ADMIN') /* or hasPermission */;

  const fetchData = () => {
    setLoading(true);
    getDepartments()
      .then((res) => setDepartments(res.data || []))
      .catch(() => setError('Failed to load departments.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchData(); }, []);

  useEffect(() => {
    getFeeCategories().then((res) => setCategories(res.data || [])).catch(() => setCategories([]));
  }, []);

  const loadFeesFor = async (departmentName, year) => {
    if (!departmentName) {
      const defaults = {};
      categories.forEach((c) => { defaults[c.id] = c.baseAmount ?? ''; });
      setFeeAmounts(defaults);
      setFeeNote('Defaults from global fee categories. Edit before saving.');
      return;
    }
    try {
      const res = await getProgramFees(departmentName, year);
      const map = {};
      (res.data || []).forEach((row) => { map[row.categoryId] = row.amount; });
      categories.forEach((c) => {
        if (map[c.id] === undefined) map[c.id] = c.baseAmount ?? '';
      });
      setFeeAmounts(map);
      setFeeNote((res.data || []).length === 0
        ? 'No customization saved yet — showing global defaults.'
        : 'Custom fee breakdown for this program and year.');
    } catch {
      const defaults = {};
      categories.forEach((c) => { defaults[c.id] = c.baseAmount ?? ''; });
      setFeeAmounts(defaults);
      setFeeNote('Could not load saved fees — showing global defaults.');
    }
  };

  const handleFormChange = (e) => {
    const next = { ...form, [e.target.name]: e.target.value };
    setForm(next);
    setFormError('');
    if (e.target.name === 'name') loadFeesFor(next.name.trim(), feeYear);
  };

  const handleFeeAmountChange = (categoryId, value) => {
    setFeeAmounts((prev) => ({ ...prev, [categoryId]: value }));
  };

  const handleFeeYearChange = (e) => {
    const year = e.target.value;
    setFeeYear(year);
    loadFeesFor(form.name.trim(), year);
  };

  const openAdd = () => {
    setForm(EMPTY_FORM); setEditId(null); setFormError(''); setFeeYear(currentYear()); setModalOpen(true);
    const defaults = {};
    categories.forEach((c) => { defaults[c.id] = c.baseAmount ?? ''; });
    setFeeAmounts(defaults);
    setFeeNote('Defaults from global fee categories. Edit before saving.');
  };
  const openEdit = (row) => {
    setForm({ name: row.name || '', code: row.code || '', description: row.description || '' });
    setEditId(row.id); setFormError(''); setFeeYear(currentYear()); setModalOpen(true);
    loadFeesFor(row.name || '', currentYear());
  };

  const handleSave = async () => {
    if (!form.name.trim() || !form.code.trim()) { setFormError('Department name and code are required.'); return; }
    setSaving(true);
    try {
      if (editId) {
        await updateDepartment(editId, form);
      } else {
        await addDepartment(form);
      }
      const fees = Object.entries(feeAmounts)
        .map(([categoryId, amount]) => ({ categoryId: Number(categoryId), amount: Number(amount) }))
        .filter((f) => Number.isFinite(f.amount) && f.amount > 0);
      if (fees.length > 0) {
        try {
          await saveProgramFees(form.name.trim(), feeYear, fees);
        } catch (feeErr) {
          setFormError(feeErr.response?.data?.error || 'Department saved, but program fees failed to save.');
          return;
        }
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
          <label className="form-label">Department Name *</label>
          <input name="name" type="text" required className="form-control" value={form.name} onChange={handleFormChange} placeholder="Enter department name" />
        </div>
        <div className="form-group">
          <label className="form-label">Department Code *</label>
          <input name="code" type="text" required maxLength="10" className="form-control" value={form.code} onChange={handleFormChange} placeholder="e.g. CSE" />
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea name="description" className="form-control" rows={3} value={form.description} onChange={handleFormChange} placeholder="Enter description" style={{ resize: 'vertical' }} />
        </div>

        <div className="form-group" style={{ marginTop: 16, borderTop: '1px solid #e2e8f0', paddingTop: 12 }}>
          <label className="form-label">Program Fees — customizable breakdown *</label>
          <p style={{ fontSize: '0.82rem', color: '#4a5568', margin: '0 0 8px' }}>
            Set per-category fees for new enrollments in this program. Applies to future students only.
            {feeNote && <><br />{feeNote}</>}
          </p>
          <div className="form-group" style={{ maxWidth: 220 }}>
            <label className="form-label">Academic Year</label>
            <input type="text" className="form-control" value={feeYear} onChange={handleFeeYearChange} placeholder="e.g. 2026" />
          </div>
          {categories.map((c) => (
            <div className="form-group" key={c.id} style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <label className="form-label" style={{ flex: '1 1 auto', margin: 0 }}>{c.categoryName}</label>
              <input
                type="number"
                min="0"
                step="0.01"
                className="form-control"
                style={{ maxWidth: 160 }}
                value={feeAmounts[c.id] ?? ''}
                onChange={(e) => handleFeeAmountChange(c.id, e.target.value)}
                placeholder={String(c.baseAmount ?? '0.00')}
              />
            </div>
          ))}
        </div>
      </Modal>
    </div>
  );
};

export default DepartmentPage;
