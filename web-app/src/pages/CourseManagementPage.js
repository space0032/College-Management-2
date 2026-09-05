import React, { useEffect, useReducer, useCallback, useMemo, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllCourses, createCourse, updateCourse, deleteCourse } from '../services/courseService';
import { getDepartments } from '../services/departmentService';
import { getSpecializations } from '../services/specializationService';
import { exportToCSV } from '../utils/exportUtils';
import SessionManager from '../utils/SessionManager';
import { CONFIG } from '../config';

const EMPTY_FORM = { name: '', code: '', credits: '', department: '', semester: '', courseType: 'CORE', specialization: '', capacity: 60 };

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
  const [specOptions, setSpecOptions] = useState([]);
  const { courses, loading, error, search, page, hasMore, pageSize, modalOpen, form, editId, formError, saving, filterDept, filterSem } = state;

  const canCreate = SessionManager.hasPermission('CREATE_COURSE');
  const canUpdate = SessionManager.hasPermission('UPDATE_COURSE');
  const canDelete = SessionManager.hasPermission('DELETE_COURSE');
  const fetchCourses = useCallback(async (pageNum = 1, append = false, q = '') => {
    dispatch({ type: 'FETCH_START' });
    try {
      const res = await getAllCourses(pageNum, pageSize, q);
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
    fetchCourses(1, false, '');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetchCourses]);

  // Debounced server search — QAHC0905 is now discoverable without Load More.
  useEffect(() => {
    if (!search) return;
    const t = setTimeout(() => fetchCourses(1, false, search), 350);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search]);

  useEffect(() => {
    getDepartments().then(res => setDepartmentOptions(res.data || [])).catch(() => setDepartmentOptions([]));
    getSpecializations().then(res => setSpecOptions(res.data || [])).catch(() => setSpecOptions([]));
  }, []);

  const handleLoadMore = useCallback(() => {
    if (!loading && hasMore) {
      fetchCourses(page + 1, true, search);
    }
  }, [fetchCourses, loading, hasMore, page, search]);

  const handleFormChange = (e) => {
    dispatch({ type: 'SET_FORM', name: e.target.name, value: e.target.value });
    // Clear stale track when department changes
    if (e.target.name === 'department' && form.specialization) {
      dispatch({ type: 'SET_FORM', name: 'specialization', value: '' });
    }
  };

  const handleSave = async () => {
    if (!form.name.trim() || !form.code.trim() || !form.department.trim() || !form.semester) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Name, code, department, and semester are required.' });
      return;
    }    if (!Number.isFinite(Number(form.credits)) || Number(form.credits) <= 0) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Credits must be greater than zero.' });
      return;
    }
    if (form.capacity !== '' && form.capacity !== null && (!Number.isFinite(Number(form.capacity)) || Number(form.capacity) < 0)) {
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Capacity must be zero or more.' });
      return;
    }
    dispatch({ type: 'SAVING_START' });
    try {
      const payload = {
        ...form,
        credits: Number(form.credits),
        semester: Number(form.semester),
        capacity: form.capacity === '' || form.capacity === null ? 60 : Number(form.capacity),
        courseType: (form.courseType || 'CORE').toUpperCase(),
        specialization: (form.specialization || '').trim(),
      };
      if (editId) {
        await updateCourse(editId, payload);
      } else {
        await createCourse(payload);
      }
      dispatch({ type: 'SAVING_DONE' });
      // Clear search/filters so the saved record stays discoverable without
      // manually loading unrelated pages (search only covers loaded rows).
      dispatch({ type: 'SET_SEARCH', payload: '' });
      dispatch({ type: 'SET_FILTER_DEPT', payload: '' });
      dispatch({ type: 'SET_FILTER_SEM', payload: '' });
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

  const departments = useMemo(() => {
    if (departmentOptions.length > 0) return [...new Set(departmentOptions.map(d => d.name).filter(Boolean))].sort();
    return [...new Set(courses.map(c => c.department).filter(Boolean))].sort();
  }, [courses, departmentOptions]);
  const semesters = useMemo(() => ['1', '2', '3', '4', '5', '6', '7', '8'], []);
  const deptTracks = useMemo(() => specOptions.filter(s => !form.department || s.departmentName === form.department || departmentOptions.find(d => d.name === form.department && d.id === s.departmentId)), [specOptions, form.department, departmentOptions]);

  // Server already filters by search; client applies only dept/sem.
  const filtered = useMemo(() => courses
    .filter(c => !filterDept || c.department === filterDept)
    .filter(c => !filterSem || String(c.semester) === filterSem), [courses, filterDept, filterSem]);

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
    { key: 'specialization', label: 'Track', render: (v) => (v ? <span style={{ background: '#faf5ff', color: '#6b46c1', padding: '2px 10px', borderRadius: '12px', fontWeight: '600', fontSize: '0.83rem' }}>{v}</span> : <span style={{ color: '#a0aec0' }}>—</span>) },
    { key: 'courseType', label: 'Type', render: (v) => (
      <span style={{ background: v === 'ELECTIVE' ? '#fefcbf' : '#e9d8fd', color: v === 'ELECTIVE' ? '#744210' : '#553c9a', padding: '2px 10px', borderRadius: '12px', fontWeight: '700', fontSize: '0.78rem' }}>{v || 'CORE'}</span>
    ) },
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
              ['ID', 'Name', 'Code', 'Credits', 'Department', 'Track', 'Type', 'Semester'],
              filtered.map(c => [c.id, c.name, c.code, c.credits, c.department, c.specialization || '', c.courseType || 'CORE', c.semester]),
              'courses_export'
            )}>⬇ Export CSV</button>
          )}
          {canCreate && <button className="btn btn-primary" onClick={() => dispatch({ type: 'OPEN_MODAL' })}>+ Add Course</button>}
        </div>
      </div>

      <div style={{ display: 'flex', gap: '10px', marginBottom: '18px', alignItems: 'center', flexWrap: 'wrap' }}>
        <input type="text" placeholder="Search all courses by name or code…" value={search} onChange={e => {
            const v = e.target.value;
            dispatch({ type: 'SET_SEARCH', payload: v });
            if (!v) fetchCourses(1, false, '');
          }}
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
        <span style={{ fontSize: '0.78rem', color: '#718096' }}>Search covers all courses across pages.</span>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      <DataTable
        columns={COLUMNS}
        data={filtered}
        loading={loading}
        onEdit={canUpdate ? (row) => dispatch({ type: 'OPEN_MODAL', form: row, editId: row.id }) : undefined}
        onDelete={canDelete ? handleDelete : undefined}
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
          <div className="form-group">
            <label className="form-label">Subject Type</label>
            <select name="courseType" className="form-control" value={form.courseType || 'CORE'} onChange={handleFormChange}>
              <option value="CORE">CORE (auto-enrolled)</option>
              <option value="ELECTIVE">ELECTIVE (via subject registration)</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Track / Specialization</label>
            <select name="specialization" className="form-control" value={form.specialization || ''} onChange={handleFormChange}>
              <option value="">Common (all tracks)</option>
              {form.specialization && !deptTracks.some(s => s.name === form.specialization) && (
                <option value={form.specialization}>{form.specialization} (legacy value)</option>
              )}
              {deptTracks.map(s => <option key={s.id} value={s.name}>{s.name}{s.code ? ` (${s.code})` : ''}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Capacity</label>
            <input name="capacity" type="number" min="0" step="1" className="form-control" value={form.capacity ?? 60} onChange={handleFormChange} />
          </div>
        </div>
      </Modal>
    </div>
  );
};

export default CourseManagementPage;
