import './Opportunities.css';
import SessionManager from '../utils/SessionManager';
import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getDrives, addDrive, deleteDrive, getCompanies, addCompany, deleteCompany, getApplicationsForStudent, getApplicationsForDrive, applyForDrive, updateAppStatus } from '../services/placementService';
import { exportToCSV, exportToExcel } from '../utils/exportUtils';

const localDate = () => { const now = new Date(); return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`; };

const COMPANY_COLS = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Company Name' },
  { key: 'industry', label: 'Industry' },
  { key: 'website', label: 'Website' },
];

const DRIVE_COLS = [
  { key: 'id', label: 'ID' },
  { key: 'companyName', label: 'Company' },
  { key: 'jobRole', label: 'Role' },
  { key: 'driveDate', label: 'Date' },
  { key: 'packageLpa', label: 'CTC (LPA)' },
  { key: 'eligibilityCriteria', label: 'Eligibility' },
  { key: 'hasApplied', label: 'Status', render: (_, d) => d.hasApplied ? <span className="badge badge-success">Applied</span> : <span className="badge badge-secondary">Not Applied</span> }
];

const APPLICATION_COLS = [
  { key: 'id', label: 'App ID' },
  { key: 'studentName', label: 'Student', render: (v, r) => (
    <span>
      {v || r.studentId}
      {(r.enrollmentId || r.enrollmentNumber || r.username) && <span style={{ fontSize: '0.75rem', fontFamily: 'monospace', color: '#718096', marginLeft: '6px' }}>({r.enrollmentId || r.enrollmentNumber || r.username})</span>}
    </span>
  )},
  { key: 'status', label: 'Status', render: (v) => <span className={`badge badge-${v === 'OFFERED' ? 'success' : v === 'REJECTED' ? 'danger' : 'primary'}`}>{v}</span> },
];

const EMPTY_COMPANY = { name: '', industry: '', website: '' };
const EMPTY_DRIVE = { companyId: '', jobRole: '', driveDate: '', deadline: '', packageLpa: '', eligibilityCriteria: '', description: '' };

const PlacementPage = () => {
  const user = SessionManager.getUser() || {};
  const canCreate = SessionManager.hasPermission('CREATE_PLACEMENT');
  const canDelete = SessionManager.hasPermission('DELETE_PLACEMENT');
  const canUpdateStatus = SessionManager.hasPermission('UPDATE_PLACEMENT');
  const isStudent = SessionManager.hasRole('STUDENT');
  const canApply = isStudent && SessionManager.hasPermission('VIEW_PLACEMENT');

  const [tab, setTab] = useState('drives');
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
  const [pending, setPending] = useState(false);
  const [appsLoading, setAppsLoading] = useState(false);
  const [appsError, setAppsError] = useState('');
  const appsRequest = React.useRef(0);
  const [search, setSearch] = useState('');

  const fetchAll = React.useCallback(() => {
    setLoading(true);
    setError('');
    return Promise.all([getCompanies(), getDrives(), isStudent && user.username ? getApplicationsForStudent(user.username) : Promise.resolve({ data: [] })])
      .then(([c, d, a]) => {
        setCompanies(c.data || []);
        const apps = a.data || [];
        // Map hasApplied boolean to drives
        const mappedDrives = (d.data || []).map(drive => ({
          ...drive,
          hasApplied: apps.some(app => String(app.driveId) === String(drive.id))
        }));
        setDrives(mappedDrives);
      })
      .catch(() => setError('Failed to load placement data.'))
      .finally(() => setLoading(false));
  }, [user.username, isStudent]);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  const handleAddCompany = async () => {
    if (saving) return;
    if (!companyForm.name.trim()) { setFormError('Company name is required.'); return; }
    setSaving(true);
    try {
      await addCompany({ ...companyForm, name: companyForm.name.trim() });
      setModalOpen(false);
      setCompanyForm(EMPTY_COMPANY);
      fetchAll();
    } catch (err) {
      setFormError(err.response?.data?.error || err.response?.data?.message || 'Failed to add company.');
    } finally {
      setSaving(false);
    }
  };

  const handleAddDrive = async () => {
    if (saving) return;
    if (!driveForm.companyId || !driveForm.jobRole.trim() || !driveForm.driveDate || !driveForm.deadline) { setFormError('Company, role, drive date, and deadline are required.'); return; }
    if (!driveForm.packageLpa || isNaN(parseFloat(driveForm.packageLpa)) || parseFloat(driveForm.packageLpa) < 0) {
      setFormError('Package (CTC) must be a valid number (e.g. 12.5 for 12.5 LPA).');
      return;
    }
    if (driveForm.deadline > driveForm.driveDate) { setFormError('Application deadline must be on or before the drive date.'); return; }
    setSaving(true);
    try {
      await addDrive({ ...driveForm, jobRole: driveForm.jobRole.trim(), companyId: Number(driveForm.companyId), packageLpa: Number(driveForm.packageLpa) });
      setModalOpen(false);
      setDriveForm(EMPTY_DRIVE);
      fetchAll();
    } catch (err) {
      setFormError(err.response?.data?.error || err.response?.data?.message || 'Failed to add drive.');
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
    if (pending) return;
    if (!user.username) { alert('Could not identify your account. Please log in again.'); return; }
    if (!window.confirm(`Apply for ${drive.jobRole} at ${drive.companyName}?`)) return;
    setPending(true);
    try {
      await applyForDrive({ driveId: drive.id, enrollmentId: user.username });
      fetchAll();
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to apply.');
    } finally { setPending(false); }
  };

  const loadApplicationsForDrive = async (drive) => {
    const request = ++appsRequest.current;
    setApplications([]);
    setAppsError('');
    setAppsLoading(true);
    setSelectedDrive(drive);
    setViewAppsModal(true);
    try {
      const res = await getApplicationsForDrive(drive.id);
      if (request === appsRequest.current) setApplications(res.data || []);
    } catch {
      if (request === appsRequest.current) setAppsError('Could not load applications. Please retry.');
    } finally { if (request === appsRequest.current) setAppsLoading(false); }
  };

  const handleUpdateStatus = async (appId, newStatus) => {
    if (pending) return;
    setPending(true);
    try {
      await updateAppStatus(appId, newStatus);
      // reload apps
      const res = await getApplicationsForDrive(selectedDrive.id);
      setApplications(res.data || []);
    } catch (err) {
      setAppsError('Failed to update status. Please retry.');
    } finally { setPending(false); }
  };

  const extendedDriveCols = [
    ...DRIVE_COLS.filter(col => isStudent || col.key !== 'hasApplied'),
    { key: 'deadline', label: 'Apply by' },
    {
      key: 'actions', label: 'Actions', render: (_, drive) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          {canApply && !drive.hasApplied && <button disabled={pending || Boolean(drive.deadline && drive.deadline < localDate())} className="btn btn-sm btn-primary" onClick={() => handleApply(drive)}>{drive.deadline && drive.deadline < localDate() ? 'Closed' : 'Apply'}</button>}
          {!isStudent && <button className="btn btn-sm btn-secondary" onClick={() => loadApplicationsForDrive(drive)}>View applications</button>}
        </div>
      )
    }
  ];

  const openModal = () => { setCompanyForm(EMPTY_COMPANY); setDriveForm(EMPTY_DRIVE); setFormError(''); setModalOpen(true); };

  return (
    <div className="opportunities-page">
      <div className="page-header">
        <div><h1 className="page-title">Placements</h1><p className="page-subtitle">Explore recruiters, upcoming drives and career opportunities.</p></div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={() => {
            if (tab === 'companies') {
              exportToCSV(['ID', 'Name', 'Industry', 'Website'], companies.map(c => [c.id, c.name, c.industry, c.website]), 'companies_export');
            } else {
              exportToCSV(['ID', 'Company', 'Role', 'Date', 'CTC', 'Eligibility'], drives.map(d => [d.id, d.companyName, d.jobRole, d.driveDate, d.packageLpa, d.eligibilityCriteria]), 'placement_drives_export');
            }
          }}>⬇ Export CSV</button>
          <button className="btn btn-secondary" onClick={() => {
            if (tab === 'companies') {
              exportToExcel(['ID', 'Name', 'Industry', 'Website'], companies.map(c => [c.id, c.name, c.industry, c.website]), 'companies_export');
            } else {
              exportToExcel(['ID', 'Company', 'Role', 'Date', 'CTC', 'Eligibility'], drives.map(d => [d.id, d.companyName, d.jobRole, d.driveDate, d.packageLpa, d.eligibilityCriteria]), 'placement_drives_export');
            }
          }}>⬇ Export Excel</button>
          {canCreate && <button className="btn btn-primary" onClick={openModal}>+ Add {tab === 'companies' ? 'Company' : 'Drive'}</button>}
        </div>
      </div>

      <div className="opportunity-stats">
        <div><span>Recruiting companies</span><strong>{loading ? '...' : companies.length}</strong></div>
        <div><span>Placement drives</span><strong>{loading ? '...' : drives.length}</strong></div>
        <div><span>Upcoming drives</span><strong>{loading ? '...' : drives.filter(d => d.driveDate >= localDate()).length}</strong></div>
      </div>
      <div className="opportunity-toolbar"><label htmlFor="placement-search">Search opportunities</label><input id="placement-search" className="form-control" type="search" value={search} onChange={e => setSearch(e.target.value)} placeholder="Company, role or industry" /></div>
      <div className="tab-buttons">
        <button className={`btn btn-tab ${tab === 'companies' ? 'active' : ''}`} onClick={() => setTab('companies')}>🏢 Companies</button>
        <button className={`btn btn-tab ${tab === 'drives' ? 'active' : ''}`} onClick={() => setTab('drives')}>📅 Placement Drives</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error} <button className="btn btn-secondary btn-sm" onClick={fetchAll}>Retry</button></div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading…</span></div>
      ) : tab === 'companies' ? (
        <DataTable columns={COMPANY_COLS} data={companies.filter(c => `${c.name} ${c.industry}`.toLowerCase().includes(search.toLowerCase()))} onDelete={canDelete ? handleDeleteCompany : undefined} emptyMessage={search ? "No companies match your search." : "No companies added yet."} />
      ) : (
        <DataTable columns={extendedDriveCols} data={drives.filter(d => `${d.companyName} ${d.jobRole}`.toLowerCase().includes(search.toLowerCase()))} onDelete={canDelete ? handleDeleteDrive : undefined} emptyMessage={search ? "No drives match your search." : "No placement drives added yet."} />
      )}

      <Modal
        isOpen={modalOpen}
        title={tab === 'companies' ? 'Add Company' : 'Add Placement Drive'}
        onClose={() => setModalOpen(false)}
        onSubmit={tab === 'companies' ? handleAddCompany : handleAddDrive}
        submitLabel="Save"
        submitting={saving}
      >
        <form onSubmit={e => { e.preventDefault(); tab === 'companies' ? handleAddCompany() : handleAddDrive(); }}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {tab === 'companies' ? (
          <>
            {[{ name: 'name', label: 'Company Name', required: true }, { name: 'industry', label: 'Industry' }, { name: 'website', label: 'Website', type: 'url' }].map(({ name, label, type = 'text', required = false }) => (
              <div className="form-group" key={name}>
                <label className="form-label" htmlFor={`placement-${name}`}>{label}{required ? ' *' : ''}</label>
                <input id={`placement-${name}`} name={name} type={type} required={required} className="form-control" value={companyForm[name]} onChange={(e) => setCompanyForm((p) => ({ ...p, [name]: e.target.value }))} placeholder={`Enter ${label.toLowerCase()}`} />
              </div>
            ))}
          </>
        ) : (
          <>
            <div className="form-group">
              <label className="form-label" htmlFor="placement-company">Company *</label>
              <select id="placement-company" className="form-control" required value={driveForm.companyId} onChange={(e) => setDriveForm((p) => ({ ...p, companyId: e.target.value }))}>
                <option value="">Select company</option>
                {companies.map(company => <option key={company.id} value={company.id}>{company.name}</option>)}
              </select>
            </div>
            {[{ name: 'jobRole', label: 'Role', required: true }, { name: 'driveDate', label: 'Drive Date', type: 'date', required: true }, { name: 'deadline', label: 'Application Deadline', type: 'date', required: true }, { name: 'packageLpa', label: 'CTC (LPA)', type: 'number', required: true }, { name: 'eligibilityCriteria', label: 'Eligibility' }, { name: 'description', label: 'Description' }].map(({ name, label, type = 'text', required = false }) => (
              <div className="form-group" key={name}>
                <label className="form-label" htmlFor={`placement-${name}`}>{label}{required ? ' *' : ''}</label>
                <input id={`placement-${name}`} name={name} type={type} required={required} min={type === 'number' ? '0' : undefined} step={name === 'packageLpa' ? '0.1' : undefined} className="form-control" value={driveForm[name]} onChange={(e) => setDriveForm((p) => ({ ...p, [name]: e.target.value }))} placeholder={type === 'date' ? '' : `Enter ${label.toLowerCase()}`} />
              </div>
            ))}
          </>
        )}
        </form>
      </Modal>

      <Modal isOpen={viewAppsModal} title={`Applications: ${selectedDrive?.jobRole} at ${selectedDrive?.companyName}`} onClose={() => { appsRequest.current++; setViewAppsModal(false); }} submitting={pending}>
        {appsLoading ? <p role="status">Loading applications...</p> : appsError ? <div role="alert">{appsError} <button className="btn btn-secondary" onClick={() => loadApplicationsForDrive(selectedDrive)}>Retry</button></div> : applications.length === 0 ? <p>No applications yet.</p> : (
          <DataTable
            columns={[
              ...APPLICATION_COLS,
              {
                key: 'update', label: 'Update Status', render: (_, app) => (
                  canUpdateStatus ? (
                  <select
                    className="form-control"
                    style={{ width: '120px', padding: '4px' }}
                    disabled={pending}
                    aria-label={`Status for ${app.studentName || app.studentId}`}
                    value={app.status}
                    onChange={(e) => handleUpdateStatus(app.id, e.target.value)}>
                    <option value="APPLIED">Applied</option>
                    <option value="INTERVIEWING">Interviewing</option>
                    <option value="OFFERED">Offered</option>
                    <option value="REJECTED">Rejected</option>
                  </select>
                  ) : (
                    <span>{app.status}</span>
                  )
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
