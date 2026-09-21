import React, { useEffect, useState } from 'react';
import { getWardens, addWarden, updateWarden, deleteWarden } from '../services/featureService';
import { getHostels } from '../services/hostelService';
import Modal from '../components/Modal';
import './WardenManagementPage.css';

const emptyForm = { id: null, name: '', email: '', phone: '', hostelId: '' };
const assigned = w => Number(w.hostelId) > 0;
const initials = name => (name || '?').trim().split(/\s+/).slice(0, 2).map(p => p[0]).join('').toUpperCase();

export default function WardenManagementPage() {
  const [wardens, setWardens] = useState([]);
  const [hostels, setHostels] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [initialForm, setInitialForm] = useState(emptyForm);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [hostelError, setHostelError] = useState('');
  const [formError, setFormError] = useState('');
  const [notice, setNotice] = useState('');
  const [saving, setSaving] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');
  const [search, setSearch] = useState('');
  const [assignment, setAssignment] = useState('all');
  const [hostelFilter, setHostelFilter] = useState('');
  const editMode = form.id !== null;

  const load = async () => {
    setLoading(true); setLoadError('');
    try { const res = await getWardens(); setWardens(res?.data || []); }
    catch (err) { setLoadError(err?.response?.data?.error || 'Could not load wardens. Please try again.'); }
    finally { setLoading(false); }
  };
  const loadHostels = async () => {
    setHostelError('');
    try { const res = await getHostels(); setHostels(res?.data || []); }
    catch { setHostelError('Hostel options could not be loaded. Existing assignments will be preserved.'); }
  };
  useEffect(() => { load(); loadHostels(); }, []);

  const openForm = w => {
    const next = w ? { id: w.id, name: w.name || '', email: w.email || '', phone: w.phone || '', hostelId: w.hostelId || '' } : emptyForm;
    setForm(next); setInitialForm(next); setFormError(''); setShowForm(true);
  };
  const handleSubmit = async event => {
    event?.preventDefault();
    if (saving) return;
    if (!form.name.trim()) { setFormError('Enter the warden’s name.'); return; }
    setSaving(true); setFormError('');
    try {
      const payload = { ...form, name: form.name.trim(), email: form.email.trim(), phone: form.phone.trim(), hostelId: Number(form.hostelId || 0) };
      if (editMode) { await updateWarden(form.id, payload); setNotice(`${payload.name}’s profile has been updated.`); }
      else {
        const res = await addWarden(payload);
        setNotice(res?.data?.username ? `${payload.name} added. Login username: ${res.data.username}. Initial password: 123. Ask the warden to change it after first login.` : `${payload.name} added successfully.`);
      }
      setShowForm(false); await load();
    } catch (err) { setFormError(err.response?.data?.error || 'Failed to save warden. Please try again.'); }
    finally { setSaving(false); }
  };
  const handleDelete = async () => {
    if (deleting) return;
    setDeleting(true); setDeleteError('');
    try {
      await deleteWarden(deleteTarget.id);
      setNotice(`${deleteTarget.name} and their login account have been deleted.`);
      setDeleteTarget(null); await load();
    } catch (err) { setDeleteError(err.response?.data?.error || 'Failed to delete warden. Please try again.'); }
    finally { setDeleting(false); }
  };
  const hostelName = w => w.hostelName || hostels.find(h => String(h.id) === String(w.hostelId))?.name || (assigned(w) ? `Hostel #${w.hostelId}` : 'Unassigned');
  const query = search.trim().toLowerCase();
  const assignedCount = wardens.filter(assigned).length;
  const visible = wardens.filter(w =>
    (!query || [w.name, w.email, w.phone, w.username, hostelName(w)].some(v => (v || '').toLowerCase().includes(query))) &&
    (assignment === 'all' || (assignment === 'assigned' ? assigned(w) : !assigned(w))) &&
    (!hostelFilter || String(w.hostelId) === hostelFilter)
  ).sort((a, b) => (a.name || '').localeCompare(b.name || ''));
  const hasFilters = Boolean(query || assignment !== 'all' || hostelFilter);
  const clearFilters = () => { setSearch(''); setAssignment('all'); setHostelFilter(''); };

  return <div className="page-container warden-page">
    <header className="page-header warden-header">
      <div><span className="warden-eyebrow">HOSTEL ADMINISTRATION</span><h1>Warden Management</h1><p>Manage your warden team, contact details, and hostel assignments.</p></div>
      <button className="btn btn-primary" onClick={() => openForm()}>+ Add Warden</button>
    </header>
    <div className="warden-stats" aria-label="Warden overview">
      {[
        ['Total wardens', wardens.length, 'Your hostel management team', 'all'],
        ['Assigned', assignedCount, 'Wardens with a hostel assignment', 'assigned'],
        ['Unassigned', wardens.length - assignedCount, 'Wardens awaiting an assignment', 'unassigned'],
      ].map(([label, count, hint, value]) => <button key={value} className={`warden-stat ${assignment === value ? 'is-selected' : ''}`} aria-pressed={assignment === value} onClick={() => { setAssignment(value); setHostelFilter(''); }}>
        <span className="warden-stat-label">{label}<span aria-hidden="true">↗</span></span><strong>{loading || loadError ? '—' : count}</strong><span className="warden-muted">{hint}</span>
      </button>)}
    </div>
    {notice && <div className="warden-notice" role="status"><span>{notice}</span><button className="btn btn-secondary btn-sm" onClick={() => setNotice('')} aria-label="Dismiss notification">Dismiss</button></div>}
    <section className="card warden-directory" aria-labelledby="warden-directory-title">
      <div className="warden-directory-heading"><div><h2 id="warden-directory-title">Warden directory</h2><p className="warden-muted">Contact information and account details in one place.</p></div><button className="btn btn-secondary btn-sm" onClick={load} disabled={loading}>{loading ? 'Refreshing…' : 'Refresh'}</button></div>
      <div className="warden-toolbar">
        <div className="warden-search"><label htmlFor="warden-search">Search wardens</label><input id="warden-search" className="form-control" type="search" placeholder="Search name, email, phone, or username…" value={search} onChange={e => setSearch(e.target.value)} /></div>
        <div><label htmlFor="warden-assignment">Assignment</label><select id="warden-assignment" className="form-control" value={assignment} onChange={e => setAssignment(e.target.value)}><option value="all">All wardens</option><option value="assigned">Assigned</option><option value="unassigned">Unassigned</option></select></div>
        <div><label htmlFor="warden-hostel-filter">Hostel</label><select id="warden-hostel-filter" className="form-control" value={hostelFilter} onChange={e => setHostelFilter(e.target.value)}><option value="">All hostels</option>{hostels.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}</select></div>
      </div>
      {hostelError && <div className="warden-warning" role="alert">{hostelError}<button className="btn btn-secondary btn-sm" onClick={loadHostels}>Retry hostels</button></div>}
      {loading ? <div className="warden-empty" role="status"><div className="spinner" /><h3>Loading your warden team…</h3></div> : loadError ? <div className="warden-empty" role="alert"><h3>Unable to load wardens</h3><p>{loadError}</p><button className="btn btn-primary" onClick={load}>Try again</button></div> : visible.length === 0 ?
        <div className="warden-empty"><div className="warden-empty-symbol" aria-hidden="true">{hasFilters ? '⌕' : '+'}</div><h3>{hasFilters ? 'No matching wardens' : 'Build your warden team'}</h3><p>{hasFilters ? 'Try another search or clear your filters to see the full directory.' : 'Add your first warden to manage their account and hostel assignment.'}</p><button className="btn btn-primary" onClick={hasFilters ? clearFilters : () => openForm()}>{hasFilters ? 'Clear filters' : 'Add Warden'}</button></div> : <>
        <div className="warden-results" role="status"><span>Showing {visible.length} of {wardens.length} wardens</span>{hasFilters && <button className="btn btn-secondary btn-sm" onClick={clearFilters}>Clear filters</button>}</div>
        <div className="warden-table-wrap"><table className="warden-table"><thead><tr>{['Warden', 'Contact details', 'Hostel assignment', 'Login account', 'Actions'].map(label => <th key={label} scope="col">{label}</th>)}</tr></thead><tbody>
          {visible.map(w => <tr key={w.id}>
            <td><div className="warden-person"><span className="warden-avatar" aria-hidden="true">{initials(w.name)}</span><div><strong>{w.name}</strong><span className="warden-muted">Warden #{w.id}</span></div></div></td>
            <td><div className="warden-contact">{w.email ? <a href={`mailto:${w.email}`}>{w.email}</a> : <span className="warden-muted">No email provided</span>}{w.phone ? <a className="warden-muted" href={`tel:${w.phone}`}>{w.phone}</a> : <span className="warden-muted">No phone provided</span>}</div></td>
            <td><span className={`warden-assignment ${assigned(w) ? 'assigned' : 'unassigned'}`}><span aria-hidden="true">●</span> {hostelName(w)}</span></td>
            <td>{w.username ? <code className="warden-username">{w.username}</code> : <span className="warden-muted">Not available</span>}</td>
            <td><div className="warden-actions"><button className="btn btn-secondary btn-sm" onClick={() => openForm(w)} aria-label={`Edit ${w.name}`}>Edit</button><button className="btn btn-sm warden-delete" onClick={() => { setDeleteError(''); setDeleteTarget(w); }} aria-label={`Delete ${w.name}`}>Delete</button></div></td>
          </tr>)}
        </tbody></table></div>
      </>}
    </section>
    <Modal isOpen={showForm} title={editMode ? 'Edit Warden' : 'Add Warden'} onClose={() => setShowForm(false)} onSubmit={handleSubmit} submitLabel={editMode ? 'Save changes' : 'Create warden'} submitting={saving} isDirty={JSON.stringify(form) !== JSON.stringify(initialForm)}>
      <form className="warden-form" onSubmit={handleSubmit}>
        <p className="warden-muted">{editMode ? 'Update contact details and manage this warden’s hostel assignment.' : 'Add a team member. A warden login account will be created automatically.'}</p>
        {formError && <div className="warden-warning" role="alert">{formError}</div>}
        <fieldset disabled={saving}><legend>Personal details</legend>
          <div className="form-group"><label className="form-label" htmlFor="warden-name">Full name *</label><input id="warden-name" className="form-control" autoComplete="name" placeholder="Enter full name" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required /></div>
          <div className="warden-form-grid">
            <div className="form-group"><label className="form-label" htmlFor="warden-email">Email address *</label><input id="warden-email" className="form-control" type="email" autoComplete="email" placeholder="name@college.edu" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required /></div>
            <div className="form-group"><label className="form-label" htmlFor="warden-phone">Phone <span className="warden-muted">(optional)</span></label><input id="warden-phone" className="form-control" type="tel" autoComplete="tel" placeholder="Enter phone number" pattern="\+?[0-9\- ]{7,15}" title="Use 7–15 digits, spaces or hyphens, with an optional leading +." value={form.phone} onChange={e => setForm({ ...form, phone: e.target.value })} /></div>
          </div>
          <div className="form-group"><label className="form-label" htmlFor="warden-hostel">Hostel assignment</label><select id="warden-hostel" className="form-control" value={form.hostelId} disabled={Boolean(hostelError)} onChange={e => setForm({ ...form, hostelId: e.target.value })} aria-describedby="warden-hostel-help"><option value="">Unassigned</option>{form.hostelId && !hostels.some(h => String(h.id) === String(form.hostelId)) && <option value={form.hostelId}>Current hostel #{form.hostelId}</option>}{hostels.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}</select><p id="warden-hostel-help" className="warden-field-hint">You can leave this unassigned and choose a hostel later.</p></div>
        </fieldset>
        {hostelError && <div className="warden-warning" role="alert">{hostelError}<button type="button" className="btn btn-secondary btn-sm" onClick={loadHostels}>Retry</button></div>}
        {!editMode && <div className="warden-account-note"><strong>Login account</strong><p>The initial password is 123. Ask the warden to change it after their first login. Their username will appear after creation.</p></div>}
      </form>
    </Modal>
    <Modal isOpen={Boolean(deleteTarget)} title="Delete warden?" onClose={() => setDeleteTarget(null)} onSubmit={handleDelete} submitLabel="Delete warden" submitting={deleting} destructive size="sm">
      <p>This will permanently delete <strong>{deleteTarget?.name}</strong> and their login account. This action cannot be undone.</p>
      {deleteError && <div className="warden-warning" role="alert">{deleteError}</div>}
    </Modal>
  </div>;
}
