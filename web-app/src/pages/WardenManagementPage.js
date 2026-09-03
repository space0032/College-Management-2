import React, { useEffect, useState } from 'react';
import { getWardens, addWarden } from '../services/featureService';

const WardenManagementPage = () => {
  const [wardens, setWardens] = useState([]);
  const [form, setForm] = useState({ name: '', email: '', phone: '', hostelId: '' });
  const [showForm, setShowForm] = useState(false);

  const load = async () => {
    try {
      const res = await getWardens();
      setWardens(res?.data || []);
    } catch (err) {
      setWardens([]);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name) return;
    try {
      const res = await addWarden({ ...form, hostelId: Number(form.hostelId || 0) });
      const w = res?.data;
      alert(`Warden created${w?.username ? ` — login: ${w.username} / password123` : ''}`);
      setForm({ name: '', email: '', phone: '', hostelId: '' });
      setShowForm(false);
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to create warden');
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Warden Management</h1>
          <p>Manage hostel wardens and their accounts</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>{showForm ? 'Cancel' : '+ Add Warden'}</button>
      </div>

      {showForm && (
        <div className="card" style={{ padding: '20px', marginBottom: '24px' }}>
          <h3>Add Warden</h3>
          <p style={{ color: '#718096' }}>A WARDEN user account is created automatically (default password: password123).</p>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Name</label>
              <input className="form-control" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Email</label>
              <input className="form-control" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Phone</label>
              <input className="form-control" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Hostel ID</label>
              <input className="form-control" type="number" value={form.hostelId} onChange={(e) => setForm({ ...form, hostelId: e.target.value })} />
            </div>
            <button className="btn btn-primary" type="submit">Create Warden</button>
          </form>
        </div>
      )}

      <div className="card" style={{ padding: '20px' }}>
        <h3>Wardens</h3>
        {wardens.length === 0 ? (
          <p style={{ color: '#718096' }}>No wardens found.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Hostel</th>
                <th>Username</th>
              </tr>
            </thead>
            <tbody>
              {wardens.map((w) => (
                <tr key={w.id}>
                  <td>{w.name}</td>
                  <td>{w.email}</td>
                  <td>{w.phone}</td>
                  <td>{w.hostelName || w.hostelId || '—'}</td>
                  <td>{w.username || '—'}</td>
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
