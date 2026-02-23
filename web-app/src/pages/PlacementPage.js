import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getDrives, addDrive, deleteDrive, getCompanies, addCompany, deleteCompany } from '../services/placementService';

const COMPANY_COLS = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Company Name' },
  { key: 'industry', label: 'Industry' },
  { key: 'website', label: 'Website' },
];

const DRIVE_COLS = [
  { key: 'id', label: 'ID' },
  { key: 'companyName', label: 'Company' },
  { key: 'role', label: 'Role' },
  { key: 'date', label: 'Date' },
  { key: 'ctc', label: 'CTC' },
  { key: 'eligibility', label: 'Eligibility' },
];

const EMPTY_COMPANY = { name: '', industry: '', website: '' };
const EMPTY_DRIVE = { companyName: '', role: '', date: '', ctc: '', eligibility: '' };

const PlacementPage = () => {
  const [tab, setTab] = useState('companies');
  const [companies, setCompanies] = useState([]);
  const [drives, setDrives] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [companyForm, setCompanyForm] = useState(EMPTY_COMPANY);
  const [driveForm, setDriveForm] = useState(EMPTY_DRIVE);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchAll = () => {
    setLoading(true);
    Promise.all([getCompanies(), getDrives()])
      .then(([c, d]) => { setCompanies(c.data || []); setDrives(d.data || []); })
      .catch(() => setError('Failed to load placement data.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchAll(); }, []);

  const handleAddCompany = async () => {
    if (!companyForm.name) { setFormError('Company name is required.'); return; }
    setSaving(true);
    try {
      await addCompany(companyForm);
      setModalOpen(false);
      setCompanyForm(EMPTY_COMPANY);
      fetchAll();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to add company.');
    } finally {
      setSaving(false);
    }
  };

  const handleAddDrive = async () => {
    if (!driveForm.companyName || !driveForm.role) { setFormError('Company and role are required.'); return; }
    setSaving(true);
    try {
      await addDrive(driveForm);
      setModalOpen(false);
      setDriveForm(EMPTY_DRIVE);
      fetchAll();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to add drive.');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteCompany = async (row) => {
    if (!window.confirm(`Delete company "${row.name}"?`)) return;
    try { await deleteCompany(row.id); fetchAll(); } catch { setError('Failed to delete company.'); }
  };

  const handleDeleteDrive = async (row) => {
    if (!window.confirm(`Delete drive for "${row.companyName}"?`)) return;
    try { await deleteDrive(row.id); fetchAll(); } catch { setError('Failed to delete drive.'); }
  };

  const openModal = () => { setFormError(''); setModalOpen(true); };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">💼 Placements</h1>
        <button className="btn btn-primary" onClick={openModal}>+ Add {tab === 'companies' ? 'Company' : 'Drive'}</button>
      </div>

      <div className="tab-buttons">
        <button className={`btn btn-tab ${tab === 'companies' ? 'active' : ''}`} onClick={() => setTab('companies')}>🏢 Companies</button>
        <button className={`btn btn-tab ${tab === 'drives' ? 'active' : ''}`} onClick={() => setTab('drives')}>📅 Placement Drives</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading…</span></div>
      ) : tab === 'companies' ? (
        <DataTable columns={COMPANY_COLS} data={companies} onDelete={handleDeleteCompany} emptyMessage="No companies added yet." />
      ) : (
        <DataTable columns={DRIVE_COLS} data={drives} onDelete={handleDeleteDrive} emptyMessage="No placement drives added yet." />
      )}

      <Modal
        isOpen={modalOpen}
        title={tab === 'companies' ? 'Add Company' : 'Add Placement Drive'}
        onClose={() => setModalOpen(false)}
        onSubmit={tab === 'companies' ? handleAddCompany : handleAddDrive}
        submitLabel={saving ? 'Saving…' : 'Save'}
      >
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {tab === 'companies' ? (
          <>
            {[{ name: 'name', label: 'Company Name' }, { name: 'industry', label: 'Industry' }, { name: 'website', label: 'Website' }].map(({ name, label }) => (
              <div className="form-group" key={name}>
                <label className="form-label">{label}</label>
                <input name={name} type="text" className="form-control" value={companyForm[name]} onChange={(e) => setCompanyForm((p) => ({ ...p, [name]: e.target.value }))} placeholder={`Enter ${label.toLowerCase()}`} />
              </div>
            ))}
          </>
        ) : (
          <>
            {[{ name: 'companyName', label: 'Company Name' }, { name: 'role', label: 'Role' }, { name: 'date', label: 'Date', type: 'date' }, { name: 'ctc', label: 'CTC' }, { name: 'eligibility', label: 'Eligibility' }].map(({ name, label, type = 'text' }) => (
              <div className="form-group" key={name}>
                <label className="form-label">{label}</label>
                <input name={name} type={type} className="form-control" value={driveForm[name]} onChange={(e) => setDriveForm((p) => ({ ...p, [name]: e.target.value }))} placeholder={type === 'date' ? '' : `Enter ${label.toLowerCase()}`} />
              </div>
            ))}
          </>
        )}
      </Modal>
    </div>
  );
};

export default PlacementPage;
