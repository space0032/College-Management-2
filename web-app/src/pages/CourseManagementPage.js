import React, { useEffect, useReducer, useCallback, useMemo, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllCourses, createCourse, updateCourse, deleteCourse } from '../services/courseService';
import { getDepartments } from '../services/departmentService';
import { exportToCSV } from '../utils/exportUtils';
import SessionManager from '../utils/SessionManager';
import { CONFIG } from '../config';

const EMPTY_FORM = { name: '', code: '', credits: '', department: '', semester: '' };

const initialState = {
  courses: [],
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
  filterSem: ''
};

function courseReducer(state, action) {
  switch (action.type) {
    case 'FETCH_START':
      return { ...state, loading: true, error: '' };
    case 'FETCH_SUCCESS':
      return {
        ...state,
        loading: false,
        courses: action.append ? [...state.courses, ...action.payload] : action.payload,
        totalCount: action.total,
        hasMore: (action.append ? state.courses.length + action.payload.length : action.payload.length) < action.total,
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

const CourseManagementPage = () => {
  const [state, dispatch] = useReducer(courseReducer, initialState);
  const [departmentOptions, setDepartmentOptions] = useState([]);
  const { courses, loading, error, search, page, hasMore, pageSize, modalOpen, form, editId, formError, saving, filterDept, filterSem } = state;

  const userRole = SessionManager.getUserRole() || 'STUDENT';
  const canManage = userRole === 'ADMIN' || userRole === 'FACULTY';
  const fetchCourses = useCallback(async (pageNum = 1, append = false) => {
    dispatch({ type: 'FETCH_START' });
    try {
      const res = await getAllCourses(pageNum, pageSize);
      dispatch({
        type: 'FETCH_SUCCESS',
        payload: res.data || [],
        total: parseInt(res.headers['x-total-count'] || '0'),
        page: pageNum,
        append
      });
    } catch {
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to load courses.' });
    }
  }, [pageSize]);

  useEffect(() => {
    fetchCourses(1, false);
  }, [fetchCourses]);

  useEffect(() => {
    getDepartments().then(res => setDepartmentOptions(res.data || [])).catch(() => setDepartmentOptions([]));
  }, []);

  const handleLoadMore = useCallback(() => {
    if (!loading && hasMore) {
      fetchCourses(page + 1, true);
    }
  }, [fetchCourses, loading, hasMore, page]);

  const handleFormChange = (e) => {
    dispatch({ type: 'SET_FORM', name: e.target.name, value: e.target.value });
  };

  const handleSave = async () => {
    if (!form.name.trim() || !form.code.trim() || !form.department.trim() || !form.semester) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Name, code, department, and semester are required.' });
      return;
    }
    if (!Number.isFinite(Number(form.credits)) || Number(form.credits) <= 0) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Credits must be greater than zero.' });
      return;
    }
    dispatch({ type: 'SAVING_START' });
    try {
      if (editId) {
        await updateCourse(editId, form);
      } else {
        await createCourse(form);
      }
      dispatch({ type: 'SAVING_DONE' });
      fetchCourses(1, false);
    } catch (err) {
      dispatch({ type: 'SET_FORM_ERROR', payload: err.response?.data?.error || err.response?.data?.message || 'Failed to save course.' });
    }
  };

  const handleDelete = async (row) => {
    if (!window.confirm(`Delete course "${row.name}"?`)) return;
    try {
      await deleteCourse(row.id);
      fetchCourses(1, false);
    } catch {
      dispatch({ type: 'FETCH_ERROR', payload: 'Failed to delete course.' });
    }
  };

  const departments = useMemo(() => [...new Set(courses.map(c => c.department).filter(Boolean))].sort(), [courses]);
  const semesters = useMemo(() => [...new Set(courses.map(c => String(c.semester)).filter(Boolean))].sort((a, b) => Number(a) - Number(b)), [courses]);

  const filtered = useMemo(() => courses
    .filter(c => !search || (c.name || '').toLowerCase().includes(search.toLowerCase()) || (c.code || '').toLowerCase().includes(search.toLowerCase()))
    .filter(c => !filterDept || c.department === filterDept)
    .filter(c => !filterSem || String(c.semester) === filterSem), [courses, search, filterDept, filterSem]);

  const COLUMNS = [
    { key: 'id', label: 'ID' },
    { key: 'name', label: 'Course Name' },
    {
      key: 'code', label: 'Code', render: (v) => (
        <span style={{ background: '#ebf8ff', color: '#2b6cb0', padding: '2px 10px', borderRadius: '12px', fontFamily: 'monospace', fontWeight: '700', fontSize: '0.83rem' }}>{v}</span>
      )
    },
    {
      key: 'credits', label: 'Credits', render: (v) => (
        <span style={{ background: '#f0fff4', color: '#276749', padding: '2px 10px', borderRadius: '12px', fontWeight: '600', fontSize: '0.83rem' }}>{v}</span>
      )
    },
    { key: 'department', label: 'Department' },
    {
      key: 'semester', label: 'Semester', render: (v) => (
        <span style={{ background: '#fffaf0', color: '#c05621', padding: '2px 10px', borderRadius: '12px', fontWeight: '600', fontSize: '0.83rem' }}>Sem {v}</span>
      )
    },
  ];

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📚 Course Management</h1>
        <div style={{ display: 'flex', gap: '8px' }}>
          {filtered.length > 0 && (
            <button className="btn btn-secondary" onClick={() => exportToCSV(
              ['ID', 'Name', 'Code', 'Credits', 'Department', 'Semester'],
              filtered.map(c => [c.id, c.name, c.code, c.credits, c.department, c.semester]),
              'courses_export'
            )}>⬇ Export CSV</button>
          )}
          {canManage && <button className="btn btn-primary" onClick={() => dispatch({ type: 'OPEN_MODAL' })}>+ Add Course</button>}
        </div>
      </div>

      <div style={{ display: 'flex', gap: '10px', marginBottom: '18px', alignItems: 'center', flexWrap: 'wrap' }}>
        <input type="text" placeholder="Search by name or code…" value={search} onChange={e => dispatch({ type: 'SET_SEARCH', payload: e.target.value })}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', flex: '1 1 200px', fontSize: '0.87rem' }} />
        <select value={filterDept} onChange={e => dispatch({ type: 'SET_FILTER_DEPT', payload: e.target.value })}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '0.87rem' }}>
          <option value="">All Departments</option>
          {departments.map(d => <option key={d} value={d}>{d}</option>)}
        </select>
        <select value={filterSem} onChange={e => dispatch({ type: 'SET_FILTER_SEM', payload: e.target.value })}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '0.87rem' }}>
          <option value="">All Semesters</option>
          {semesters.map(s => <option key={s} value={s}>Semester {s}</option>)}
        </select>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      <DataTable
        columns={COLUMNS}
        data={filtered}
        loading={loading}
        onEdit={canManage ? (row) => dispatch({ type: 'OPEN_MODAL', form: row, editId: row.id }) : undefined}
        onDelete={canManage ? handleDelete : undefined}
        emptyMessage="No courses found."
      />

      {hasMore && (
        <div style={{ display: 'flex', justifyContent: 'center', marginTop: 20 }}>
          <button className="btn btn-secondary" onClick={handleLoadMore} disabled={loading}>
            {loading ? 'Loading...' : 'Load More Courses'}
          </button>
        </div>
      )}

      <Modal isOpen={modalOpen} title={editId ? 'Edit Course' : 'Add Course'} onClose={() => dispatch({ type: 'CLOSE_MODAL' })} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-grid">
          <div className="form-group" style={{ gridColumn: '1 / -1' }}>
            <label className="form-label">Course Name *</label>
            <input name="name" type="text" className="form-control" required value={form.name} onChange={handleFormChange} placeholder="Enter course name" />
          </div>
          <div className="form-group">
            <label className="form-label">Course Code *</label>
            <input name="code" type="text" className="form-control" required value={form.code} onChange={handleFormChange} placeholder="Enter code" />
          </div>
          <div className="form-group">
            <label className="form-label">Credits *</label>
            <input name="credits" type="number" min="1" step="1" required className="form-control" value={form.credits} onChange={handleFormChange} />
          </div>
          <div className="form-group">
            <label className="form-label">Department *</label>
            <select name="department" required className="form-control" value={form.department} onChange={handleFormChange}>
              <option value="">Select department</option>
              {departmentOptions.map(d => <option key={d.id} value={d.name}>{d.name} ({d.code})</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Semester *</label>
            <select name="semester" required className="form-control" value={form.semester} onChange={handleFormChange}>
              <option value="">Select semester</option>
              {[1, 2, 3, 4, 5, 6, 7, 8].map(s => <option key={s} value={s}>Semester {s}</option>)}
            </select>
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default CourseManagementPage;
