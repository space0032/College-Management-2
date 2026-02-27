import React, { useEffect, useRef, useCallback, useMemo } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllStudents, createStudent, updateStudent, deleteStudent, searchStudents } from '../services/studentService';
import { exportToCSV } from '../utils/exportUtils';
import { CONFIG } from '../config';

const EMPTY_FORM = { name: '', email: '', phone: '', course: '', department: '', semester: '' };

const initialState = {
  students: [],
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
  viewMode: 'table',
  filterDept: '',
  filterSem: ''
};

function studentReducer(state, action) {
  switch (action.type) {
    case 'FETCH_START':
      return { ...state, loading: true, error: '' };
    case 'FETCH_SUCCESS':
      return {
        ...state,
        loading: false,
        students: action.append ? [...state.students, ...action.payload] : action.payload,
        totalCount: action.total,
        hasMore: (action.append ? state.students.length + action.payload.length : action.payload.length) < action.total,
        page: action.page || state.page
      };
    case 'FETCH_ERROR':
      return { ...state, loading: false, error: action.payload };
    case 'SET_SEARCH':
      return { ...state, search: action.payload };
    case 'SET_FILTER_DEPT':
      return { ...state, filterDept: action.payload };
    case 'SET_FILTER_SEM':
      return { ...state, filterSem: action.payload };
    case 'SET_VIEW_MODE':
      return { ...state, viewMode: action.payload };
    case 'OPEN_MODAL':
      return { ...state, modalOpen: true, form: action.form || EMPTY_FORM, editId: action.editId || null, formError: '' };
    case 'CLOSE_MODAL':
      return { ...state, modalOpen: false };
    case 'SET_FORM':
      return { ...state, form: { ...state.form, [action.name]: action.value }, formError: '' };
    case 'SET_FORM_ERROR':
      return { ...state, formError: action.payload, saving: false };
    case 'SAVING_START':
      return { ...state, saving: true };
    case 'SAVING_DONE':
      return { ...state, saving: false, modalOpen: false };
    default:
      return state;
  }
}

const StudentManagementPage = () => {
  const [state, dispatch] = React.useReducer(studentReducer, initialState);
  const { students, loading, error, search, page, hasMore, pageSize, totalCount, modalOpen, form, editId, formError, saving, viewMode, filterDept, filterSem } = state;

  const searchDebounce = useRef(null);

  const fetchStudents = useCallback(async (pageNum = 1, append = false) => {
    dispatch({ type: 'FETCH_START' });
    try {
      const res = await getAllStudents(pageNum, pageSize);
      dispatch({
        type: 'FETCH_SUCCESS',
        payload: res.data || [],
        total: parseInt(res.headers['x-total-count'] || '0'),
        page: pageNum,
        append
      });
    } catch {
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to load students.' });
    }
  }, [pageSize]);

  useEffect(() => {
    fetchStudents(1, false);
  }, [fetchStudents]);

  const handleLoadMore = useCallback(() => {
    if (!loading && hasMore) {
      fetchStudents(page + 1, true);
    }
  }, [fetchStudents, loading, hasMore, page]);

  const handleSearch = useCallback(async (query) => {
    const q = query !== undefined ? query : search;
    if (!q.trim()) return fetchStudents(1, false);
    dispatch({ type: 'FETCH_START' });
    try {
      const res = await searchStudents(q);
      dispatch({ type: 'FETCH_SUCCESS', payload: res.data || [], total: (res.data || []).length, page: 1, append: false });
    } catch {
      dispatch({ type: 'FETCH_ERROR', payload: 'Search failed.' });
    }
  }, [search, fetchStudents]);

  const handleSearchChange = useCallback((e) => {
    const val = e.target.value;
    dispatch({ type: 'SET_SEARCH', payload: val });
    clearTimeout(searchDebounce.current);
    searchDebounce.current = setTimeout(() => handleSearch(val), CONFIG.ANIMATION.DEBOUNCE_DELAY);
  }, [handleSearch]);

  const handleFormChange = useCallback((e) => {
    dispatch({ type: 'SET_FORM', name: e.target.name, value: e.target.value });
  }, []);

  const openAdd = useCallback(() => {
    dispatch({ type: 'OPEN_MODAL' });
  }, []);

  const openEdit = useCallback((row) => {
    dispatch({ type: 'OPEN_MODAL', form: { ...row }, editId: row.id });
  }, []);

  const handleSave = useCallback(async () => {
    if (!form.name || !form.email) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Name and email are required.' });
      return;
    }
    dispatch({ type: 'SAVING_START' });
    try {
      if (editId) {
        await updateStudent(editId, form);
      } else {
        await createStudent(form);
      }
      dispatch({ type: 'SAVING_DONE' });
      fetchStudents(1, false);
    } catch (err) {
      dispatch({ type: 'SET_FORM_ERROR', payload: err.response?.data?.message || 'Failed to save student.' });
    }
  }, [form, editId, fetchStudents]);

  const handleDelete = useCallback(async (id) => {
    if (!window.confirm('Are you sure you want to delete this student?')) return;
    try {
      await deleteStudent(id);
      fetchStudents(1, false);
    } catch {
      alert('Failed to delete student.');
    }
  }, [fetchStudents]);

  const filteredStudents = useMemo(() => {
    return students.filter(s => {
      const matchesDept = !filterDept || s.department === filterDept;
      const matchesSem = !filterSem || s.semester?.toString() === filterSem;
      return matchesDept && matchesSem;
    });
  }, [students, filterDept, filterSem]);

  const handleExport = useCallback(() => {
    exportToCSV(
      ['ID', 'Name', 'Email', 'Phone', 'Course', 'Department', 'Semester'],
      filteredStudents.map(s => [s.id, s.name, s.email, s.phone, s.course, s.department, s.semester]),
      'students_export'
    );
  }, [filteredStudents]);

  // Stats
  const totalStudents = totalCount;
  const depts = useMemo(() => [...new Set(students.map(s => s.department).filter(Boolean))], [students]);
  const avgSem = useMemo(() => students.length > 0
    ? (students.reduce((acc, s) => acc + (parseInt(s.semester) || 0), 0) / students.length).toFixed(1)
    : 0, [students]);

  const COLUMNS = useMemo(() => [
    { key: 'id', label: 'ID' },
    {
      key: 'name', label: 'Name', render: (v, row) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            width: '32px', height: '32px', borderRadius: '50%',
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '0.8rem', fontWeight: 'bold'
          }}>
            {v.charAt(0)}
          </div>
          <span>{v}</span>
        </div>
      )
    },
    { key: 'email', label: 'Email' },
    { key: 'course', label: 'Course' },
    {
      key: 'department', label: 'Department', render: (v) => (
        <span className="badge badge-primary">{v}</span>
      )
    },
    {
      key: 'semester', label: 'Semester', render: (v) => (
        <span className="badge badge-secondary">S{v}</span>
      )
    },
    {
      key: 'actions', label: 'Actions', render: (_, row) => (
        <div style={{ display: 'flex', gap: '5px' }}>
          <button className="btn btn-sm btn-secondary" onClick={() => openEdit(row)}>Edit</button>
          <button className="btn btn-sm btn-danger" onClick={() => handleDelete(row.id)}>Delete</button>
        </div>
      )
    }
  ], [handleDelete, openEdit]);

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">🎓 Student Management</h1>
          <p className="page-subtitle">Manage student records, enrollments, and academic status</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={handleExport}>⬇ Export CSV</button>
          <button className="btn btn-primary" onClick={openAdd}>+ Add Student</button>
        </div>
      </div>

      {/* Stats Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '25px' }}>
        <div className="stat-card" style={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
          <div style={{ fontSize: '0.9rem', opacity: 0.8 }}>Total Students</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>{totalStudents}</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '0.9rem', color: '#666' }}>Active Departments</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: 'var(--primary-color)' }}>{depts.length}</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '0.9rem', color: '#666' }}>Average Semester</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#f6ad55' }}>{avgSem}</div>
        </div>
        <div className="stat-card">
          <div style={{ fontSize: '0.9rem', color: '#666' }}>Filtered Students</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#48bb78' }}>{filteredStudents.length}</div>
        </div>
      </div>

      {/* Controls Bar */}
      <div className="card" style={{ padding: '15px', marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'white', borderRadius: '12px', border: '1px solid #edf2f7' }}>
        <form onSubmit={(e) => { e.preventDefault(); handleSearch(search); }} style={{ display: 'flex', gap: '10px', flex: 1, maxWidth: '500px' }}>
          <input
            type="text"
            className="form-control"
            placeholder="Search by name, email, or ID..."
            value={search}
            onChange={handleSearchChange}
          />
          <button type="submit" className="btn btn-primary">Search</button>
        </form>

        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
          <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
            <span style={{ fontSize: '0.9rem', color: '#666' }}>Filter:</span>
            <select
              className="form-control"
              style={{ width: '150px' }}
              value={filterDept}
              onChange={(e) => dispatch({ type: 'SET_FILTER_DEPT', payload: e.target.value })}
            >
              <option value="">All Departments</option>
              {depts.map(d => <option key={d} value={d}>{d}</option>)}
            </select>
            <select
              className="form-control"
              style={{ width: '130px' }}
              value={filterSem}
              onChange={(e) => dispatch({ type: 'SET_FILTER_SEM', payload: e.target.value })}
            >
              <option value="">All Semesters</option>
              {[1, 2, 3, 4, 5, 6, 7, 8].map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>

          <div style={{ height: '20px', width: '1px', background: '#e2e8f0' }} />

          <div style={{ display: 'flex', background: '#f7fafc', padding: '3px', borderRadius: '8px' }}>
            <button
              onClick={() => dispatch({ type: 'SET_VIEW_MODE', payload: 'table' })}
              style={{
                border: 'none', padding: '6px 12px', borderRadius: '6px', cursor: 'pointer',
                background: viewMode === 'table' ? 'white' : 'transparent',
                boxShadow: viewMode === 'table' ? '0 2px 4px rgba(0,0,0,0.05)' : 'none',
                color: viewMode === 'table' ? 'var(--primary-color)' : '#718096',
                fontWeight: viewMode === 'table' ? '600' : 'normal'
              }}
            >
              Table
            </button>
            <button
              onClick={() => dispatch({ type: 'SET_VIEW_MODE', payload: 'grid' })}
              style={{
                border: 'none', padding: '6px 12px', borderRadius: '6px', cursor: 'pointer',
                background: viewMode === 'grid' ? 'white' : 'transparent',
                boxShadow: viewMode === 'grid' ? '0 2px 4px rgba(0,0,0,0.05)' : 'none',
                color: viewMode === 'grid' ? 'var(--primary-color)' : '#718096',
                fontWeight: viewMode === 'grid' ? '600' : 'normal'
              }}
            >
              Grid
            </button>
          </div>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {loading && students.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <div className="spinner"></div>
          <p style={{ marginTop: '15px', color: '#666' }}>Loading students...</p>
        </div>
      ) : (
        <>
          {viewMode === 'table' ? (
            <div className="data-table-container">
              <DataTable columns={COLUMNS} data={filteredStudents} emptyMessage="No students found." />
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px' }}>
              {filteredStudents.map(student => (
                <StudentCard
                  key={student.id}
                  student={student}
                  onEdit={openEdit}
                  onDelete={handleDelete}
                />
              ))}
              {filteredStudents.length === 0 && (
                <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
                  No students found matching filters.
                </div>
              )}
            </div>
          )}

          {hasMore && (
            <div style={{ textAlign: 'center', marginTop: '30px', marginBottom: '30px' }}>
              <button
                className="btn btn-secondary"
                onClick={handleLoadMore}
                disabled={loading}
                style={{ minWidth: '200px' }}
              >
                {loading ? 'Loading...' : `Load More (${students.length} of ${totalCount})`}
              </button>
            </div>
          )}
        </>
      )}

      <Modal
        isOpen={modalOpen}
        title={editId ? 'Edit Student' : 'Add New Student'}
        onClose={() => dispatch({ type: 'CLOSE_MODAL' })}
        onSubmit={handleSave}
        submitLabel={saving ? 'Saving...' : 'Save Student'}
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
            <input name="department" type="text" value={form.department} onChange={handleFormChange} placeholder="e.g. CS" />
          </div>
          <div className="form-group">
            <label>Course</label>
            <input name="course" type="text" value={form.course} onChange={handleFormChange} placeholder="e.g. B.Tech" />
          </div>
          <div className="form-group">
            <label>Semester</label>
            <input name="semester" type="number" min="1" max="8" value={form.semester} onChange={handleFormChange} />
          </div>
        </div>
      </Modal>
    </div>
  );
};

// Memoized Student Card for better grid performance
const StudentCard = React.memo(({ student, onEdit, onDelete }) => (
  <div className="stat-card" style={{ display: 'flex', flexDirection: 'column', position: 'relative' }}>
    <div style={{ position: 'absolute', top: '15px', right: '15px', display: 'flex', gap: '5px' }}>
      <button
        onClick={() => onEdit(student)}
        style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a0aec0' }}
        title="Edit"
      >
        ✏️
      </button>
      <button
        onClick={() => onDelete(student.id)}
        style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a0aec0' }}
        title="Delete"
      >
        🗑️
      </button>
    </div>

    <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '15px' }}>
      <div style={{
        width: '50px', height: '50px', borderRadius: '12px',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: '1.2rem', fontWeight: 'bold'
      }}>
        {student.name ? student.name.charAt(0) : '?'}
      </div>
      <div>
        <div style={{ fontWeight: 'bold', color: '#2d3748' }}>{student.name}</div>
        <div style={{ fontSize: '0.8rem', color: '#718096' }}>ID: {student.id}</div>
      </div>
    </div>

    <div style={{ fontSize: '0.85rem', color: '#4a5568', marginBottom: '10px' }}>
      📧 {student.email}
    </div>

    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: 'auto' }}>
      <span className="badge badge-primary">{student.department}</span>
      <span className="badge badge-secondary">{student.course}</span>
      <span className="badge badge-warning">Sem {student.semester}</span>
    </div>
  </div>
));

export default StudentManagementPage;
