import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllFaculty, createFaculty, updateFaculty, deleteFaculty, searchFaculty } from '../services/facultyService';
import { exportToCSV } from '../utils/exportUtils';

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
  const [filterDept, setFilterDept] = useState('');

  const fetchFaculty = async () => {
    setLoading(true);
    try {
      const res = await getAllFaculty();
      setFaculty(res.data || []);
    } catch {
      setError('Failed to load faculty.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFaculty();
  }, []);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
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

  const openAdd = () => {
    setForm(EMPTY_FORM);
    setEditId(null);
    setModalOpen(true);
  };

  const openEdit = (row) => {
    setForm({ ...row });
    setEditId(row.id);
    setModalOpen(true);
  };

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

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this faculty member?')) return;
    try {
      await deleteFaculty(id);
      fetchFaculty();
    } catch {
      alert('Failed to delete.');
    }
  };

  const handleExport = () => {
    exportToCSV(
      ['ID', 'Name', 'Email', 'Phone', 'Department', 'Qualification'],
      filteredFaculty.map(f => [f.id, f.name, f.email, f.phone, f.department, f.qualification]),
      'faculty_export'
    );
  };

  const filteredFaculty = faculty.filter(f => !filterDept || f.department === filterDept);

  // Stats
  const totalFaculty = faculty.length;
  const depts = [...new Set(faculty.map(f => f.department).filter(Boolean))];
  const phds = faculty.filter(f => (f.qualification || '').toUpperCase().includes('PHD')).length;

  const COLUMNS = [
    { key: 'id', label: 'ID' },
    {
      key: 'name', label: 'Name', render: (v, row) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '32px', height: '32px', borderRadius: '50%',
            background: 'linear-gradient(135deg, #48bb78 0%, #38a169 100%)',
            color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '0.8rem', fontWeight: 'bold'
          }}>
            {v.charAt(0)}
          </div>
          <strong>{v}</strong>
        </div>
      )
    },
    { key: 'email', label: 'Email' },
    {
      key: 'department', label: 'Department', render: (v) => (
        <span className="badge badge-primary">{v}</span>
      )
    },
    {
      key: 'qualification', label: 'Qualification', render: (v) => {
        const isPhD = (v || '').toUpperCase().includes('PHD');
        return (
          <span className={`badge ${isPhD ? 'badge-warning' : 'badge-secondary'}`} style={isPhD ? { fontWeight: 'bold' } : {}}>
            {v}
          </span>
        );
      }
    },
    {
      key: 'actions', label: 'Actions', render: (_, row) => (
        <div style={{ display: 'flex', gap: '5px' }}>
          <button className="btn btn-sm btn-secondary" onClick={() => openEdit(row)}>Edit</button>
          <button className="btn btn-sm btn-danger" onClick={() => handleDelete(row.id)}>Delete</button>
        </div>
      )
    }
  ];

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">👩‍🏫 Faculty Management</h1>
          <p className="page-subtitle">Manage faculty staff, qualifications, and department assignments</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={handleExport}>⬇ Export CSV</button>
          <button className="btn btn-primary" onClick={openAdd}>+ Add Faculty</button>
        </div>
      </div>

      {/* Stats Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '25px' }}>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg, #48bb78 0%, #38a169 100%)', color: 'white' }}>
          <div style={{ fontSize: '0.9rem', opacity: 0.8 }}>Total Faculty</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>{totalFaculty}</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '0.9rem', color: '#666' }}>Active Departments</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3182ce' }}>{depts.length}</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '0.9rem', color: '#666' }}>PhD Holders</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#f6ad55' }}>{phds}</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '0.9rem', color: '#666' }}>Avg. Exp (Simulated)</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#9f7aea' }}>8.4 Yrs</div>
        </div>
      </div>

      <div className="card" style={{ padding: '15px', marginBottom: '20px', display: 'flex', gap: '15px', background: 'white', borderRadius: '12px' }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '10px', flex: 1 }}>
          <input
            type="text"
            className="form-control"
            placeholder="Search faculty by name, email, or dept..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button type="submit" className="btn btn-primary">Search</button>
        </form>
        <select
          className="form-control"
          style={{ width: '200px' }}
          value={filterDept}
          onChange={(e) => setFilterDept(e.target.value)}
        >
          <option value="">All Departments</option>
          {depts.map(d => <option key={d} value={d}>{d}</option>)}
        </select>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div className="spinner"></div>
          <p style={{ marginTop: '15px', color: '#666' }}>Loading faculty data...</p>
        </div>
      ) : (
        <div className="data-table-container">
          <DataTable columns={COLUMNS} data={filteredFaculty} emptyMessage="No faculty members found." />
        </div>
      )}

      <Modal
        isOpen={modalOpen}
        title={editId ? 'Edit Faculty Member' : 'Add New Faculty Member'}
        onClose={() => setModalOpen(false)}
        onSubmit={handleSave}
        submitLabel={saving ? 'Saving...' : 'Save Faculty'}
      >
        {formError && <div className="alert alert-danger" style={{ marginBottom: '15px' }}>{formError}</div>}
        <div className="form-grid">
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label>Full Name *</label>
            <input name="name" type="text" value={form.name} onChange={handleFormChange} required />
          </div>
          <div className="form-group">
            <label>Email Address *</label>
            <input name="email" type="email" value={form.email} onChange={handleFormChange} required />
          </div>
          <div className="form-group">
            <label>Phone Number</label>
            <input name="phone" type="text" value={form.phone} onChange={handleFormChange} />
          </div>
          <div className="form-group">
            <label>Department</label>
            <input name="department" type="text" value={form.department} onChange={handleFormChange} placeholder="e.g. Mathematics" />
          </div>
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label>Highest Qualification</label>
            <input name="qualification" type="text" value={form.qualification} onChange={handleFormChange} placeholder="e.g. PhD in Computer Science" />
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default FacultyManagementPage;
