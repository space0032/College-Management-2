import React, { useEffect, useState } from 'react';
import { getWardens, addWarden, updateWarden, deleteWarden } from '../services/featureService';
import { getHostels } from '../services/hostelService';

const emptyForm = { id: null, name: '', email: '', phone: '', hostelId: '' };

const WardenManagementPage = () => {
  const [wardens, setWardens] = useState([]);
  const [hostels, setHostels] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [showForm, setShowForm] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState('');

  const load = async () => {
    setLoading(true);
    setLoadError('');
    try {
      const res = await getWardens();
      setWardens(res?.data || []);
    } catch (err) {
      setWardens([]);
      setLoadError(err?.response?.data?.error || 'Could not load wardens. Ensure the backend is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    getHostels().then((res) => setHostels(res?.data || [])).catch(() => setHostels([]));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openCreate = () => {
    setEditMode(false);
    setForm(emptyForm);
    setShowForm(true);
  };

  const openEdit = (w) => {
    setEditMode(true);
    setForm({
      id: w.id,
      name: w.name || '',
      email: w.email || '',
      phone: w.phone || '',
      hostelId: w.hostelId || '',
    });
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) return;
    setSaving(true);
    try {
      const payload = { ...form, hostelId: Number(form.hostelId || 0) };
      if (editMode) {
        await updateWarden(form.id, payload);
      } else {
        const res = await addWarden(payload);
        const w = res?.data;
        if (w?.username) {
          alert(`Warden created — login: ${w.username} / 123 (testing). Change it after first login.`);
        }
      }
      setForm(emptyForm);
      setShowForm(false);
      setEditMode(false);
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to save warden');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (w) => {
    if (!window.confirm(`Delete warden "${w.name}" and its login account? This is permanent.`)) return;
    try {
      await deleteWarden(w.id);
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to delete warden');
    }
  };

  const query = search.trim().toLowerCase();
  const visible = wardens.filter((w) =>
    !query ||
    (w.name || '').toLowerCase().includes(query) ||
    (w.email || '').toLowerCase().includes(query) ||
    (w.hostelName || '').toLowerCase().includes(query) ||
    (w.username || '').toLowerCase().includes(query)
  );

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Warden Management</h1>
          <p>Manage hostel wardens and their accounts</p>
        </div>
        <button className="btn btn-primary" onClick={() => (showForm ? setShowForm(false) : openCreate())}>
          {showForm ? 'Cancel' : '+ Add Warden'}
        </button>
      </div>

      {showForm && (
        <div className="card" style={{ padding: '20px', marginBottom: '24px' }}>
          <h3>{editMode ? 'Edit Warden' : 'Add Warden'}</h3>
          <p style={{ color: '#718096' }}>
            {editMode
              ? 'Update the warden profile and hostel assignment.'
              : 'A WARDEN user account is created automatically (default password: 123).'}
          </p>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Name *</label>
              <input
                className="form-control"
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Email *</label>
              <input
                className="form-control"
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input
                className="form-control"
                type="tel"
                inputMode="tel"
                pattern="\+?[0-9\- ]{7,15}"
                value={form.phone}
                onChange={(e) => setForm({ ...form, phone: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label className="form-label">Hostel</label>
              <select
                className="form-control"
                value={form.hostelId || ''}
                onChange={(e) => setForm({ ...form, hostelId: e.target.value })}
              >
                <option value="">— Unassigned —</option>
                {hostels.map((h) => (
                  <option key={h.id} value={h.id}>
                    {h.name}
                  </option>
                ))}
              </select>
            </div>
            <button className="btn btn-primary" type="submit" disabled={saving}>
              {saving ? 'Saving…' : editMode ? 'Save Changes' : 'Create Warden'}
            </button>
          </form>
        </div>
      )}

      <div className="card" style={{ padding: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 style={{ margin: 0 }}>Wardens ({visible.length})</h3>
          <input
            className="form-control"
            type="text"
            placeholder="Search name, email, hostel…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ width: '260px' }}
          />
        </div>

        {loadError && (
          <div className="retry-bar" role="alert">
            <span>{loadError}</span>
            <button className="btn btn-secondary btn-sm" onClick={load}>Retry</button>
          </div>
        )}

        {loading ? (
          <p style={{ color: '#718096' }}>Loading wardens…</p>
        ) : visible.length === 0 ? (
          <p style={{ color: '#718096' }}>{loadError ? '' : search ? 'No wardens match your search.' : 'No wardens found.'}</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Hostel</th>
                <th>Username</th>
                <th style={{ width: '150px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((w) => (
                <tr key={w.id}>
                  <td>{w.name}</td>
                  <td>{w.email || '—'}</td>
                  <td>{w.phone || '—'}</td>
                  <td>{w.hostelName || 'Unassigned'}</td>
                  <td>{w.username || '—'}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => openEdit(w)}>Edit</button>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(w)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default WardenManagementPage;