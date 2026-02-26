import SessionManager from '../utils/SessionManager';
import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getDrives, addDrive, deleteDrive, getCompanies, addCompany, deleteCompany, getApplicationsForStudent, getApplicationsForDrive, applyForDrive, updateAppStatus } from '../services/placementService';
import { exportToCSV } from '../utils/exportUtils';

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
  { key: 'hasApplied', label: 'Status', render: (_, d) => d.hasApplied ? <span className="badge badge-success">Applied</span> : <span className="badge badge-secondary">Not Applied</span> }
];

const APPLICATION_COLS = [
  { key: 'id', label: 'App ID' },
  { key: 'studentName', label: 'Student' },
  { key: 'status', label: 'Status', render: (v) => <span className={`badge badge-${v === 'OFFERED' ? 'success' : v === 'REJECTED' ? 'danger' : 'primary'}`}>{v}</span> },
];

const EMPTY_COMPANY = { name: '', industry: '', website: '' };
const EMPTY_DRIVE = { companyName: '', role: '', date: '', ctc: '', eligibility: '' };

const PlacementPage = () => {
  const user = SessionManager.getUser() || {};
  const canManage = SessionManager.hasRole('ADMIN') || user.role === 'FACULTY';

  const [tab, setTab] = useState('companies');
  const [companies, setCompanies] = useState([]);
  const [drives, setDrives] = useState([]);
  const [applications, setApplications] = useState([]);
  const [selectedDrive, setSelectedDrive] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [viewAppsModal, setViewAppsModal] = useState(false);
  const [companyForm, setCompanyForm] = useState(EMPTY_COMPANY);
  const [driveForm, setDriveForm] = useState(EMPTY_DRIVE);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchAll = React.useCallback(() => {
    setLoading(true);
    Promise.all([getCompanies(), getDrives(), user.id ? getApplicationsForStudent(user.id) : Promise.resolve({ data: [] })])
      .then(([c, d, a]) => {
        setCompanies(c.data || []);
        const apps = a.data || [];
        // Map hasApplied boolean to drives
        const mappedDrives = (d.data || []).map(drive => ({
          ...drive,
          hasApplied: apps.some(app => app.driveId === drive.id)
        }));
        setDrives(mappedDrives);
      })
      .catch(() => setError('Failed to load placement data.'))
      .finally(() => setLoading(false));
  }, [user.id]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

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
    if (driveForm.ctc !== undefined && driveForm.ctc !== '' && isNaN(parseFloat(driveForm.ctc))) {
      setFormError('Package (CTC) must be a valid number (e.g. 12.5 for 12.5 LPA).');
      return;
    }
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

  const handleApply = async (drive) => {
    if (!user.id) { alert('Could not identify your account. Please log in again.'); return; }
    if (!window.confirm(`Apply for ${drive.role} at ${drive.companyName}?`)) return;
    try {
      await applyForDrive({ driveId: drive.id, studentId: user.id });
      fetchAll();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to apply.');
    }
  };

  const loadApplicationsForDrive = async (drive) => {
    setSelectedDrive(drive);
    setViewAppsModal(true);
    try {
      const res = await getApplicationsForDrive(drive.id);
      setApplications(res.data || []);
    } catch {
      setApplications([]);
    }
  };

  const handleUpdateStatus = async (appId, newStatus) => {
    try {
      await updateAppStatus(appId, newStatus);
      // reload apps
      const res = await getApplicationsForDrive(selectedDrive.id);
      setApplications(res.data || []);
    } catch (err) {
      alert('Failed to update status');
    }
  };

  const extendedDriveCols = [
    ...DRIVE_COLS,
    {
      key: 'actions', label: 'Actions', render: (_, drive) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          {!drive.hasApplied && <button className="btn btn-sm btn-primary" onClick={() => handleApply(drive)}>Apply</button>}
          <button className="btn btn-sm btn-secondary" onClick={() => loadApplicationsForDrive(drive)}>View Apps</button>
        </div>
      )
    }
  ];

  const openModal = () => { setFormError(''); setModalOpen(true); };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">💼 Placements</h1>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={() => {
            if (tab === 'companies') {
              exportToCSV(['ID', 'Name', 'Industry', 'Website'], companies.map(c => [c.id, c.name, c.industry, c.website]), 'companies_export');
            } else {
              exportToCSV(['ID', 'Company', 'Role', 'Date', 'CTC', 'Eligibility'], drives.map(d => [d.id, d.companyName, d.role, d.date, d.ctc, d.eligibility]), 'placement_drives_export');
            }
          }}>⬇ Export CSV</button>
          {canManage && <button className="btn btn-primary" onClick={openModal}>+ Add {tab === 'companies' ? 'Company' : 'Drive'}</button>}
        </div>
      </div>

      <div className="tab-buttons">
        <button className={`btn btn-tab ${tab === 'companies' ? 'active' : ''}`} onClick={() => setTab('companies')}>🏢 Companies</button>
        <button className={`btn btn-tab ${tab === 'drives' ? 'active' : ''}`} onClick={() => setTab('drives')}>📅 Placement Drives</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading…</span></div>
      ) : tab === 'companies' ? (
        <DataTable columns={COMPANY_COLS} data={companies} onDelete={canManage ? handleDeleteCompany : undefined} emptyMessage="No companies added yet." />
      ) : (
        <DataTable columns={extendedDriveCols} data={drives} onDelete={canManage ? handleDeleteDrive : undefined} emptyMessage="No placement drives added yet." />
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

      <Modal isOpen={viewAppsModal} title={`Applications: ${selectedDrive?.role} at ${selectedDrive?.companyName}`} onClose={() => setViewAppsModal(false)}>
        {applications.length === 0 ? <p>No applications yet.</p> : (
          <DataTable
            columns={[
              ...APPLICATION_COLS,
              {
                key: 'update', label: 'Update Status', render: (_, app) => (
                  <select
                    className="form-control"
                    style={{ width: '120px', padding: '4px' }}
                    value={app.status}
                    onChange={(e) => handleUpdateStatus(app.id, e.target.value)}>
                    <option value="APPLIED">Applied</option>
                    <option value="INTERVIEWING">Interviewing</option>
                    <option value="OFFERED">Offered</option>
                    <option value="REJECTED">Rejected</option>
                  </select>
                )
              }
            ]}
            data={applications}
          />
        )}
      </Modal>
    </div>
  );
};

export default PlacementPage;
