import React, { useEffect, useMemo, useCallback, useState, useRef } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllFaculty, createFaculty, updateFaculty, deleteFaculty, searchFaculty } from '../services/facultyService';
import { getDepartments } from '../services/departmentService';
import { exportToCSV } from '../utils/exportUtils';
import { CONFIG } from '../config';

const EMPTY_FORM = { name: '', email: '', phone: '', department: '', qualification: '', password: '' };

const initialState = {
  faculty: [],
  loading: false,
  error: '',
  search: '',
  page: 1,
  hasMore: true,
  pageSize: CONFIG.PAGINATION.DEFAULT_PAGE_SIZE,
  totalCount: 0,
  modalOpen: false,
  form: EMPTY_FORM,
  editId: null,
  formError: '',
  saving: false,
  filterDept: '',
  createdCredentials: null
};

function facultyReducer(state, action) {
  switch (action.type) {
    case 'FETCH_START': return { ...state, loading: true, error: '' };
    case 'FETCH_SUCCESS': return {
      ...state,
      loading: false,
      faculty: action.append ? [...state.faculty, ...action.payload] : action.payload,
      totalCount: action.total,
      hasMore: (action.append ? state.faculty.length + action.payload.length : action.payload.length) < action.total,
      page: action.page || state.page
    };
    case 'FETCH_ERROR': return { ...state, loading: false, error: action.payload };
    case 'SET_SEARCH': return { ...state, search: action.payload };
    case 'SET_FILTER_DEPT': return { ...state, filterDept: action.payload };
    case 'OPEN_MODAL': return { ...state, modalOpen: true, form: action.form || EMPTY_FORM, editId: action.editId || null, formError: '' };
    case 'CLOSE_MODAL': return { ...state, modalOpen: false };
    case 'SET_FORM': return { ...state, form: { ...state.form, [action.name]: action.value }, formError: '' };
    case 'SET_FORM_ERROR': return { ...state, formError: action.payload, saving: false };
    case 'SAVING_START': return { ...state, saving: true };
    case 'SAVING_DONE': return { ...state, saving: false, modalOpen: false };
    case 'SHOW_CREDENTIALS': return { ...state, saving: false, modalOpen: false, createdCredentials: action.payload };
    case 'CLOSE_CREDENTIALS': return { ...state, createdCredentials: null };
    default: return state;
  }
}

const FacultyManagementPage = () => {
  const [state, dispatch] = React.useReducer(facultyReducer, initialState);
  const [departmentOptions, setDepartmentOptions] = useState([]);
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const firstRender = useRef(true);
  const { faculty, loading, error, search, page, hasMore, pageSize, totalCount, modalOpen, form, editId, formError, saving, filterDept, createdCredentials } = state;

  const fetchFaculty = React.useCallback(async (pageNum = 1, append = false) => {
    dispatch({ type: 'FETCH_START' });
    try {
      const res = await getAllFaculty(pageNum, pageSize);
      dispatch({
        type: 'FETCH_SUCCESS',
        payload: res.data || [],
        total: parseInt(res.headers['x-total-count'] || '0'),
        page: pageNum,
        append
      });
    } catch {
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to load faculty.' });
    }
  }, [pageSize]);

  useEffect(() => {
    fetchFaculty(1, false);
  }, [fetchFaculty]);

  useEffect(() => {
    getDepartments().then(res => setDepartmentOptions(res.data || [])).catch(() => setDepartmentOptions([]));
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(search), 300);
    return () => clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    if (firstRender.current) { firstRender.current = false; return; }
    const query = debouncedSearch.trim();
    if (!query) {
      fetchFaculty(1, false);
    } else {
      dispatch({ type: 'FETCH_START' });
      searchFaculty(query)
        .then(res => dispatch({ type: 'FETCH_SUCCESS', payload: res.data || [], total: (res.data || []).length, page: 1, append: false }))
        .catch(() => dispatch({ type: 'FETCH_ERROR', payload: 'Search failed.' }));
    }
  }, [debouncedSearch, search, fetchFaculty]);

  const handleLoadMore = () => {
    if (!loading && hasMore) {
      fetchFaculty(page + 1, true);
    }
  };

  const handleFormChange = React.useCallback((e) => {
    dispatch({ type: 'SET_FORM', name: e.target.name, value: e.target.value });
  }, []);

  const openAdd = React.useCallback(() => {
    dispatch({ type: 'OPEN_MODAL', form: { ...EMPTY_FORM } });
  }, []);

  const openEdit = useCallback((row) => {
    dispatch({ type: 'OPEN_MODAL', form: {
      name: row.name || '', email: row.email || '', phone: row.phone || '',
      department: row.department || '', qualification: row.qualification || ''
    }, editId: row.id });
  }, []);

  const handleSave = useCallback(async () => {
    if (!form.name.trim() || !form.email.trim()) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Name and email are required.' });
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Enter a valid email address.' });
      return;
    }
    if (form.phone && !/^\+?[0-9\s-]{7,15}$/.test(form.phone.trim())) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Enter a valid phone number.' });
      return;
    }
    dispatch({ type: 'SAVING_START' });
    try {
      if (editId) {
        await updateFaculty(editId, form);
        dispatch({ type: 'SAVING_DONE' });
      } else {
        const res = await createFaculty(form);
        dispatch({ type: 'SAVING_DONE' });
        dispatch({ type: 'SHOW_CREDENTIALS', payload: res.data });
      }
      fetchFaculty(1, false);
    } catch (err) {
      dispatch({ type: 'SET_FORM_ERROR', payload: err.response?.data?.error || err.response?.data?.message || 'Failed to save faculty.' });
    }
  }, [form, editId, fetchFaculty]);

  const handleDelete = useCallback(async (id) => {
    if (!window.confirm('Delete this faculty member?')) return;
    try {
      await deleteFaculty(id);
      fetchFaculty(1, false);
    } catch {
      alert('Failed to delete.');
    }
  }, [fetchFaculty]);


  const COLUMNS = [
    { key: 'username', label: 'Faculty ID', render: (v, row) => <strong>{v || `FAC${String(row.id).padStart(3, '0')}`}</strong> },
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

  const depts = useMemo(() => [...new Set(faculty.map(f => f.department).filter(Boolean))].sort(), [faculty]);
  const phds = useMemo(() => faculty.filter(f => (f.qualification || '').toUpperCase().includes('PHD')).length, [faculty]);
  const handleExport = useCallback(() => {
    exportToCSV(
      ['Faculty ID', 'Name', 'Email', 'Phone', 'Department', 'Qualification'],
      faculty.map(f => [f.username || `FAC${String(f.id).padStart(3, '0')}`, f.name, f.email, f.phone, f.department, f.qualification]),
      'faculty_export'
    );
  }, [faculty]);

  const filteredFaculty = useMemo(() => {
    return faculty.filter(f => !filterDept || f.department === filterDept);
  }, [faculty, filterDept]);

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
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>{totalCount}</div>
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
          <div style={{ fontSize: '0.9rem', color: '#666' }}>Average Experience</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#9f7aea' }}>N/A</div>
        </div>
      </div>

      <div className="card" style={{ padding: '15px', marginBottom: '20px', display: 'flex', gap: '15px', background: 'white', borderRadius: '12px' }}>
        <div style={{ display: 'flex', gap: '10px', flex: 1 }}>
          <input
            type="text"
            className="form-control"
            placeholder="Search faculty as you type by name, email, or dept..."
            value={search}
            onChange={(e) => dispatch({ type: 'SET_SEARCH', payload: e.target.value })}
          />
        </div>
        <select
          className="form-control"
          style={{ width: '200px' }}
          value={filterDept}
          onChange={(e) => dispatch({ type: 'SET_FILTER_DEPT', payload: e.target.value })}
        >
          <option value="">All Departments</option>
          {depts.map(d => <option key={d} value={d}>{d}</option>)}
        </select>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading && faculty.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div className="spinner"></div>
          <p style={{ marginTop: '15px', color: '#666' }}>Loading faculty data...</p>
        </div>
      ) : (
        <>
          <div className="data-table-container">
            <DataTable columns={COLUMNS} data={filteredFaculty} emptyMessage="No faculty members found." />
          </div>

          {hasMore && (
            <div style={{ textAlign: 'center', marginTop: '20px', marginBottom: '20px' }}>
              <button
                className="btn btn-secondary"
                onClick={handleLoadMore}
                disabled={loading}
              >
                {loading ? 'Loading...' : `Load More (${faculty.length} of ${totalCount})`}
              </button>
            </div>
          )}
        </>
      )}

      <Modal
        isOpen={modalOpen}
        title={editId ? 'Edit Faculty Member' : 'Add New Faculty Member'}
        onClose={() => dispatch({ type: 'CLOSE_MODAL' })}
        onSubmit={handleSave}
        submitLabel={saving ? 'Saving...' : 'Save Faculty'}
      >
        {formError && <div className="alert alert-danger" style={{ marginBottom: '15px' }}>{formError}</div>}
        <div className="form-grid">
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label>Full Name *</label>
            <input name="name" type="text" className="form-control" value={form.name} onChange={handleFormChange} required />
          </div>
          <div className="form-group">
            <label>Email Address *</label>
            <input name="email" type="email" className="form-control" value={form.email} onChange={handleFormChange} required />
          </div>
          <div className="form-group">
            <label>Phone Number</label>
            <input name="phone" type="tel" inputMode="tel" pattern="[+]?[0-9\- ]{7,15}" className="form-control" value={form.phone} onChange={handleFormChange} />
          </div>
          <div className="form-group">
            <label>Department *</label>
            <select name="department" required className="form-control" value={form.department} onChange={handleFormChange}>
              <option value="">Select department</option>
              {departmentOptions.map(d => <option key={d.id} value={d.name}>{d.name} ({d.code})</option>)}
            </select>
          </div>
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label>Highest Qualification</label>
            <input name="qualification" type="text" className="form-control" value={form.qualification} onChange={handleFormChange} placeholder="e.g. PhD in Computer Science" />
          </div>
          {!editId && (
            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
              <label>Account Password</label>
              <input name="password" type="text" className="form-control" value={form.password} onChange={handleFormChange} placeholder="Leave empty for default: 123" />
              <small style={{ color: '#718096', fontSize: '0.8rem' }}>A FACULTY login account is created automatically. Username shown after saving.</small>
            </div>
          )}
        </div>
      </Modal>

      {createdCredentials && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 9999 }}>
          <div style={{ background: 'white', borderRadius: '16px', padding: '32px', maxWidth: '420px', width: '90%', boxShadow: '0 20px 60px rgba(0,0,0,0.3)' }}>
            <div style={{ textAlign: 'center', marginBottom: '20px' }}>
              <h2 style={{ margin: 0, color: '#1a202c' }}>Faculty Account Created</h2>
              <p style={{ color: '#718096', margin: '5px 0 0' }}>Share these credentials with the faculty member</p>
            </div>
            <div style={{ background: '#f7fafc', borderRadius: '12px', padding: '20px', border: '1px solid #e2e8f0', marginBottom: '20px' }}>
              <div style={{ marginBottom: '12px' }}>
                <div style={{ fontSize: '0.75rem', color: '#a0aec0', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Username</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 'bold', color: '#2d3748', fontFamily: 'monospace' }}>{createdCredentials.username}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: '#a0aec0', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Password</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 'bold', color: '#e53e3e', fontFamily: 'monospace' }}>{createdCredentials.password}</div>
              </div>
            </div>
            <button
              onClick={() => dispatch({ type: 'CLOSE_CREDENTIALS' })}
              style={{ width: '100%', padding: '12px', border: 'none', borderRadius: '8px', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white', fontWeight: 'bold', fontSize: '1rem', cursor: 'pointer' }}
            >
              Got it!
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default FacultyManagementPage;
