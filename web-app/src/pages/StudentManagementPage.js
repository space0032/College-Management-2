import React, { useEffect, useRef, useCallback, useMemo, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllStudents, createStudent, updateStudent, deleteStudent, searchStudents, downloadStudentTemplate } from '../services/studentService';
import { getDepartments } from '../services/departmentService';
import { getAllCourses } from '../services/courseService';
import { getHostels, getRooms, getAllocations, allocateRoom, vacateRoom } from '../services/hostelService';
import SessionManager from '../utils/SessionManager';
import { exportToCSV } from '../utils/exportUtils';
import { CONFIG } from '../config';

const EMPTY_FORM = { name: '', email: '', phone: '', address: '', batch: '', course: '', department: '', semester: '', password: '', isHostelite: false, hostelId: '', roomId: '' };

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
  fieldErrors: {},
  saving: false,
  viewMode: 'table',
  filterDept: '',
  filterSem: '',
  createdCredentials: null
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
      return { ...state, modalOpen: true, form: action.form || EMPTY_FORM, editId: action.editId || null, formError: '', fieldErrors: {} };
    case 'CLOSE_MODAL':
      return { ...state, modalOpen: false, fieldErrors: {} };
    case 'SET_FORM': {
      const nextForm = { ...state.form, [action.name]: action.value };
      // Clear stale course when department changes
      if (action.name === 'department' && state.form.course) {
        nextForm.course = '';
      }
      // Clear stale hostel/room when hostelite is unchecked
      if (action.name === 'isHostelite' && !action.value) {
        nextForm.hostelId = '';
        nextForm.roomId = '';
      }
      const nextErrors = { ...state.fieldErrors };
      delete nextErrors[action.name];
      return { ...state, form: nextForm, formError: '', fieldErrors: nextErrors };
    }
    case 'SET_FIELD_ERRORS':
      return { ...state, fieldErrors: action.payload, saving: false };
    case 'SET_FORM_ERROR':
      return { ...state, formError: action.payload, saving: false };
    case 'SAVING_START':
      return { ...state, saving: true };
    case 'SAVING_DONE':
      return { ...state, saving: false, modalOpen: false };
    case 'SHOW_CREDENTIALS':
      return { ...state, saving: false, modalOpen: false, createdCredentials: action.payload };
    case 'CLOSE_CREDENTIALS':
      return { ...state, createdCredentials: null };
    default:
      return state;
  }
}

const StudentManagementPage = () => {
  const [state, dispatch] = React.useReducer(studentReducer, initialState);
  const [departmentOptions, setDepartmentOptions] = useState([]);
  const [courseOptions, setCourseOptions] = useState([]);
  const [hostelOptions, setHostelOptions] = useState([]);
  const [roomOptions, setRoomOptions] = useState([]);
  const [allocationList, setAllocationList] = useState([]);
  const { students, loading, error, search, page, hasMore, pageSize, totalCount, modalOpen, form, editId, formError, fieldErrors, saving, viewMode, filterDept, filterSem, createdCredentials } = state;
  const [showPassword, setShowPassword] = useState(false);

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

  useEffect(() => {
    Promise.all([getDepartments(), getAllCourses(1, 500)])
      .then(([departmentRes, courseRes]) => {
        setDepartmentOptions(departmentRes.data || []);
        setCourseOptions(courseRes.data || []);
      })
      .catch(() => {
        setDepartmentOptions([]);
        setCourseOptions([]);
      });
    Promise.all([getHostels(), getRooms(), getAllocations()])
      .then(([hostelRes, roomRes, allocRes]) => {
        setHostelOptions(hostelRes.data || []);
        setRoomOptions(roomRes.data || []);
        setAllocationList(allocRes.data || []);
      })
      .catch(() => {
        setHostelOptions([]);
        setRoomOptions([]);
        setAllocationList([]);
      });
  }, []);

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
    const { name, value } = e.target;
    dispatch({ type: 'SET_FORM', name, value });
  }, []);

  const validateField = useCallback((name, value, currentForm = form) => {
    const v = (value ?? currentForm[name] ?? '').toString().trim();
    switch (name) {
      case 'name':
        if (!v) return 'Full name is required.';
        if (v.length < 2) return 'Enter at least 2 characters.';
        return '';
      case 'email':
        if (!v) return 'Email is required.';
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) return 'Enter a valid email address.';
        return '';
      case 'phone':
        if (!v) return '';
        if (!/^\+?[0-9\s-]{7,15}$/.test(v)) return 'Enter a valid phone number.';
        return '';
      case 'department':
        return v ? '' : 'Department is required.';
      case 'course':
        return v ? '' : 'Course is required.';
      case 'semester': {
        if (!v) return 'Semester is required.';
        const n = Number(v);
        if (!Number.isInteger(n) || n < 1 || n > 8) return 'Semester must be between 1 and 8.';
        return '';
      }
      case 'hostelId':
        return currentForm.isHostelite && !v ? 'Select a hostel.' : '';
      case 'roomId':
        return currentForm.isHostelite && !v ? 'Select a room.' : '';
      default:
        return '';
    }
  }, [form]);

  const handleFieldBlur = useCallback((e) => {
    const { name, value } = e.target;
    const msg = validateField(name, value);
    if (msg) dispatch({ type: 'SET_FIELD_ERRORS', payload: { ...fieldErrors, [name]: msg } });
  }, [validateField, fieldErrors]);

  const openAdd = useCallback(() => {
    dispatch({ type: 'OPEN_MODAL', form: { ...EMPTY_FORM } });
    setShowPassword(false);
  }, []);

  const openEdit = useCallback((row) => {
    const alloc = (allocationList || []).find(a => a.studentId === row.id);
    const currentRoom = roomOptions.find(r => r.id === alloc?.roomId);
    dispatch({ type: 'OPEN_MODAL', form: {
      name: row.name || '', email: row.email || '', phone: row.phone || '',
      address: row.address || '', batch: row.batch || '',
      course: row.course || '', department: row.department || '', semester: row.semester || '',
      isHostelite: row.isHostelite || row.hostelite || !!alloc,
      hostelId: currentRoom ? String(currentRoom.hostelId) : '',
      roomId: alloc ? String(alloc.roomId) : ''
    }, editId: row.id });
    setShowPassword(false);
  }, [allocationList, roomOptions]);

  const syncAllocation = useCallback(async (studentId, nextForm) => {
    const wantHostel = !!nextForm.isHostelite;
    const currentUser = SessionManager.getUser() || {};
    const existing = (allocationList || []).find(a => a.studentId === studentId);
    if (wantHostel) {
      const nextRoomId = nextForm.roomId ? Number(nextForm.roomId) : null;
      if (!nextRoomId) {
        if (existing) await vacateRoom(existing.id);
        return;
      }
      if (existing) {
        if (existing.roomId !== nextRoomId) {
          await vacateRoom(existing.id);
          await allocateRoom({
            studentId,
            roomId: nextRoomId,
            checkInDate: new Date().toISOString().split('T')[0],
            remarks: 'Room changed during student edit',
            allocatedBy: currentUser.id || null
          });
        }
      } else {
        await allocateRoom({
          studentId,
          roomId: nextRoomId,
          checkInDate: new Date().toISOString().split('T')[0],
          remarks: 'Allocated during student edit',
          allocatedBy: currentUser.id || null
        });
      }
    } else {
      if (existing) await vacateRoom(existing.id);
    }
  }, [allocationList]);

  const handleSave = useCallback(async () => {
    const fieldsToCheck = ['name', 'email', 'phone', 'department', 'course', 'semester'];
    if (form.isHostelite) fieldsToCheck.push('hostelId', 'roomId');
    const errors = {};
    fieldsToCheck.forEach((f) => {
      const msg = validateField(f, form[f], form);
      if (msg) errors[f] = msg;
    });
    if (Object.keys(errors).length > 0) {
      dispatch({ type: 'SET_FIELD_ERRORS', payload: errors });
      dispatch({ type: 'SET_FORM_ERROR', payload: 'Please fix the highlighted fields.' });
      return;
    }
    const wantHostel = !!form.isHostelite;
    dispatch({ type: 'SAVING_START' });
    const currentUser = SessionManager.getUser() || {};
    const payload = { ...form, isHostelite: wantHostel };
    try {
      if (editId) {
        await updateStudent(editId, payload);
        await syncAllocation(editId, form);
        dispatch({ type: 'SAVING_DONE' });
      } else {
        const res = await createStudent(payload);
        const newId = res.data?.id;
        if (wantHostel && newId) {
          await allocateRoom({
            studentId: newId,
            roomId: Number(form.roomId),
            checkInDate: new Date().toISOString().split('T')[0],
            remarks: 'Allocated during student creation',
            allocatedBy: currentUser.id || null
          });
        }
        dispatch({ type: 'SAVING_DONE' });
        dispatch({ type: 'SHOW_CREDENTIALS', payload: res.data });
      }
      fetchStudents(1, false);
    } catch (err) {
      if (editId) {
        dispatch({ type: 'SET_FORM_ERROR', payload: err.response?.data?.error || err.response?.data?.message || 'Failed to save student.' });
      } else {
        dispatch({ type: 'SET_FORM_ERROR', payload: err.response?.data?.error || err.response?.data?.message || 'Student created but room allocation failed. Please allocate the room in the Hostel section.' });
      }
    }
  }, [form, editId, fetchStudents, syncAllocation, validateField]);

  const handleDelete = useCallback(async (id) => {
    if (!window.confirm('Are you sure you want to delete this student?')) return;
    try {
      await deleteStudent(id);
      fetchStudents(1, false);
    } catch (err) {
      alert(err.response?.data?.error || err.response?.data?.message || 'Failed to delete student.');
    }
  }, [fetchStudents]);

  const filteredStudents = useMemo(() => {
    return students.filter(s => {
      const query = search.trim().toLowerCase();
      const matchesSearch = !query || [s.name, s.email, s.username, s.course, s.department]
        .some(value => String(value || '').toLowerCase().includes(query));
      const matchesDept = !filterDept || s.department === filterDept;
      const matchesSem = !filterSem || s.semester?.toString() === filterSem;
      return matchesSearch && matchesDept && matchesSem;
    });
  }, [students, search, filterDept, filterSem]);

  const handleExport = useCallback(() => {
    exportToCSV(
      ['ID', 'Name', 'Email', 'Phone', 'Course', 'Department', 'Semester'],
      filteredStudents.map(s => [s.id, s.name, s.email, s.phone, s.course, s.department, s.semester]),
      'students_export'
    );
  }, [filteredStudents]);

  const fileInputRef = useRef(null);

  const handleDownloadTemplate = useCallback(async () => {
    try {
      const res = await downloadStudentTemplate();
      const blob = new Blob([res.data], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'student_import_template.csv';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch {
      alert('Failed to download template. Please try again.');
    }
  }, []);

  const handleImport = useCallback(async (event) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    try {
      const text = await file.text();
      const rows = text.split(/\r?\n/).filter(line => line.trim());
      if (rows.length < 2) {
        alert('CSV must contain a header row followed by data rows.');
        return;
      }
      const header = parseCSVLine(rows[0]).map(h => h.trim().toLowerCase());
      let success = 0, failed = 0;
      for (let i = 1; i < rows.length; i++) {
        const cols = parseCSVLine(rows[i]).map(c => c.trim());
        const record = {};
        header.forEach((h, idx) => {
          record[h] = cols[idx] || '';
        });
        const payload = {
          name: record.name || '',
          email: record.email || '',
          phone: record.phone || '',
          course: record.course || '',
          department: record.department || '',
          semester: record.semester || '',
          password: record.password || '',
          isHostelite: /^(yes|true|1|y)$/i.test(record.hostelite || record.is_hostelite || '')
        };
        try {
          await createStudent(payload);
          success++;
        } catch {
          failed++;
        }
      }
      alert(`Import complete: ${success} created, ${failed} failed.`);
      fetchStudents(1, false);
    } catch (err) {
      alert('Failed to read CSV file.');
    }
  }, [fetchStudents]);

  const parseCSVLine = (line) => {
    const result = [];
    let current = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (inQuotes) {
        if (ch === '"') {
          if (line[i + 1] === '"') { current += '"'; i++; }
          else inQuotes = false;
        } else current += ch;
      } else if (ch === '"') inQuotes = true;
      else if (ch === ',') { result.push(current); current = ''; }
      else current += ch;
    }
    result.push(current);
    return result;
  };

  // Stats
  const totalStudents = totalCount;
  const depts = useMemo(() => [...new Set(students.map(s => s.department).filter(Boolean))], [students]);
  const avgSem = useMemo(() => students.length > 0
    ? (students.reduce((acc, s) => acc + (parseInt(s.semester) || 0), 0) / students.length).toFixed(1)
    : 0, [students]);

  const COLUMNS = useMemo(() => [
    {
      key: 'username', label: 'Enrollment No.', render: (v) => (
        <span style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#2d3748' }}>{v || 'N/A'}</span>
      )
    },
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
          <button className="btn btn-secondary" onClick={handleDownloadTemplate}>📄 Download Template</button>
          <button className="btn btn-secondary" onClick={() => fileInputRef.current?.click()}>⬆ Import CSV</button>
          <button className="btn btn-primary" onClick={openAdd}>+ Add Student</button>
          <input ref={fileInputRef} type="file" accept=".csv,text/csv" style={{ display: 'none' }} onChange={handleImport} />
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
        size="large"
        onClose={() => { if (!saving) dispatch({ type: 'CLOSE_MODAL' }); }}
        onSubmit={handleSave}
        submitting={saving}
        submitLabel="Save Student"
      >
        {formError && <div className="alert alert-danger" style={{ marginBottom: '15px' }}>{formError}</div>}
        <form onSubmit={(e) => { e.preventDefault(); handleSave(); }}>
          {/* Personal section */}
          <div className="form-section">
            <h3 className="form-section-title"><span aria-hidden="true">👤</span> Personal Information</h3>
            <div className="form-grid">
              <div className="form-group form-span-2">
                <label className="form-label" htmlFor="student-name">Full Name *</label>
                <input id="student-name" name="name" className={`form-control${fieldErrors.name ? ' is-invalid' : ''}`} type="text" autoComplete="name" placeholder="e.g. Aarav Sharma" value={form.name} onChange={handleFormChange} onBlur={handleFieldBlur} required aria-invalid={!!fieldErrors.name} aria-describedby={fieldErrors.name ? 'student-name-error' : undefined} />
                {fieldErrors.name && <small id="student-name-error" className="field-error">{fieldErrors.name}</small>}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="student-email">Email Address *</label>
                <input id="student-email" name="email" className={`form-control${fieldErrors.email ? ' is-invalid' : ''}`} type="email" autoComplete="email" placeholder="student@college.edu" value={form.email} onChange={handleFormChange} onBlur={handleFieldBlur} required aria-invalid={!!fieldErrors.email} aria-describedby={fieldErrors.email ? 'student-email-error' : undefined} />
                {fieldErrors.email && <small id="student-email-error" className="field-error">{fieldErrors.email}</small>}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="student-phone">Phone Number</label>
                <input id="student-phone" name="phone" className={`form-control${fieldErrors.phone ? ' is-invalid' : ''}`} type="tel" inputMode="tel" autoComplete="tel" placeholder="+91 98765 43210" value={form.phone} onChange={handleFormChange} onBlur={handleFieldBlur} aria-invalid={!!fieldErrors.phone} aria-describedby={fieldErrors.phone ? 'student-phone-error' : undefined} />
                {fieldErrors.phone ? <small id="student-phone-error" className="field-error">{fieldErrors.phone}</small> : <small className="field-hint">Optional · 7–15 digits, spaces/-/+ allowed</small>}
              </div>
              <div className="form-group form-span-2">
                <label className="form-label" htmlFor="student-address">Address</label>
                <input id="student-address" name="address" className="form-control" type="text" autoComplete="street-address" placeholder="Street, city, PIN" value={form.address || ''} onChange={handleFormChange} />
              </div>
            </div>
          </div>

          {/* Academic section */}
          <div className="form-section">
            <h3 className="form-section-title"><span aria-hidden="true">🎓</span> Academic Details</h3>
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label" htmlFor="student-dept">Department *</label>
                <select id="student-dept" name="department" className={`form-control${fieldErrors.department ? ' is-invalid' : ''}`} required value={form.department} onChange={handleFormChange} onBlur={handleFieldBlur} aria-invalid={!!fieldErrors.department}>
                  <option value="">Select department</option>
                  {form.department && !departmentOptions.some(d => d.name === form.department) && (
                    <option value={form.department}>{form.department} (legacy value)</option>
                  )}
                  {departmentOptions.map(d => <option key={d.id} value={d.name}>{d.name} ({d.code})</option>)}
                </select>
                {fieldErrors.department && <small className="field-error">{fieldErrors.department}</small>}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="student-course">Course *</label>
                <select id="student-course" name="course" className={`form-control${fieldErrors.course ? ' is-invalid' : ''}`} required value={form.course} onChange={handleFormChange} onBlur={handleFieldBlur} aria-invalid={!!fieldErrors.course}>
                  <option value="">Select course</option>
                  {form.course && !courseOptions.some(c => c.name === form.course) && (
                    <option value={form.course}>{form.course} (legacy value)</option>
                  )}
                  {courseOptions.filter(c => !form.department || c.department === form.department).map(c => (
                    <option key={c.id} value={c.name}>{c.name} ({c.code})</option>
                  ))}
                </select>
                {fieldErrors.course ? <small className="field-error">{fieldErrors.course}</small> : (form.department && courseOptions.filter(c => !form.department || c.department === form.department).length === 0 ? <small className="field-hint">No courses found for this department.</small> : null)}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="student-sem">Semester *</label>
                <select id="student-sem" name="semester" className={`form-control${fieldErrors.semester ? ' is-invalid' : ''}`} required value={form.semester} onChange={handleFormChange} onBlur={handleFieldBlur} aria-invalid={!!fieldErrors.semester}>
                  <option value="">Select semester</option>
                  {[1, 2, 3, 4, 5, 6, 7, 8].map(s => <option key={s} value={s}>Semester {s}</option>)}
                </select>
                {fieldErrors.semester && <small className="field-error">{fieldErrors.semester}</small>}
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="student-batch">Batch</label>
                <input id="student-batch" name="batch" className="form-control" type="text" placeholder="e.g. 2023-2027" value={form.batch || ''} onChange={handleFormChange} />
                <small className="field-hint">Optional · e.g. 2023-2027</small>
              </div>
            </div>
          </div>

          {/* Account + hostel section */}
          <div className="form-section">
            <h3 className="form-section-title"><span aria-hidden="true">🔑</span> Account & Hostel</h3>
            {!editId && (
              <div className="form-grid">
                <div className="form-group form-span-2">
                  <label className="form-label" htmlFor="student-password">Password</label>
                  <div className="password-row">
                    <input id="student-password" name="password" className="form-control" type={showPassword ? 'text' : 'password'} autoComplete="new-password" value={form.password || ''} onChange={handleFormChange} placeholder="Leave empty for default: 123" />
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowPassword(v => !v)} aria-pressed={showPassword}>{showPassword ? 'Hide' : 'Show'}</button>
                    <button type="button" className="btn btn-secondary btn-sm" onClick={() => dispatch({ type: 'SET_FORM', name: 'password', value: Math.random().toString(36).slice(2, 10) })}>Generate</button>
                  </div>
                  <small className="field-hint">Leave empty for default: 123 · Enrollment number is auto-generated as username.</small>
                </div>
              </div>
            )}
            <div className="hostel-toggle">
              <label className="hostel-toggle-label">
                <input
                  type="checkbox"
                  checked={!!form.isHostelite}
                  onChange={(e) => dispatch({ type: 'SET_FORM', name: 'isHostelite', value: e.target.checked })}
                />
                Is Hostelite?
              </label>
              <small className="field-hint">Hostelite students are assigned a hostel room and hostel fees.</small>
            </div>
            {form.isHostelite && (
              <div className="form-grid" style={{ marginTop: '12px' }}>
                <div className="form-group">
                  <label className="form-label" htmlFor="student-hostel">Hostel *</label>
                  <select
                    id="student-hostel"
                    className={`form-control${fieldErrors.hostelId ? ' is-invalid' : ''}`}
                    required
                    value={form.hostelId}
                    onChange={(e) => dispatch({ type: 'SET_FORM', name: 'hostelId', value: e.target.value })}
                    onBlur={(e) => { const msg = validateField('hostelId', e.target.value); if (msg) dispatch({ type: 'SET_FIELD_ERRORS', payload: { ...fieldErrors, hostelId: msg } }); }}
                  >
                    <option value="">Select hostel</option>
                    {hostelOptions.map(h => <option key={h.id} value={h.id}>{h.name}</option>)}
                  </select>
                  {fieldErrors.hostelId && <small className="field-error">{fieldErrors.hostelId}</small>}
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="student-room">Room *</label>
                  <select
                    id="student-room"
                    className={`form-control${fieldErrors.roomId ? ' is-invalid' : ''}`}
                    required
                    value={form.roomId}
                    onChange={(e) => dispatch({ type: 'SET_FORM', name: 'roomId', value: e.target.value })}
                    onBlur={(e) => { const msg = validateField('roomId', e.target.value); if (msg) dispatch({ type: 'SET_FIELD_ERRORS', payload: { ...fieldErrors, roomId: msg } }); }}
                    disabled={!form.hostelId}
                  >
                    <option value="">Select room</option>
                    {roomOptions
                      .filter(r => !form.hostelId || r.hostelId === Number(form.hostelId))
                      .filter(r => r.occupiedCount != null && r.capacity != null ? r.occupiedCount < r.capacity : true)
                      .map(r => <option key={r.id} value={r.id}>{r.roomNumber} ({r.hostelName})</option>)}
                  </select>
                  {fieldErrors.roomId ? <small className="field-error">{fieldErrors.roomId}</small> : (
                    <small className="field-hint">
                      {!form.hostelId ? 'Select a hostel first.' :
                        roomOptions.filter(r => r.hostelId === Number(form.hostelId) && r.occupiedCount != null && r.capacity != null ? r.occupiedCount < r.capacity : true).length === 0
                          ? 'No available rooms in this hostel.' : 'Only rooms with available capacity are shown.'}
                    </small>
                  )}
                </div>
              </div>
            )}
          </div>
        </form>
      </Modal>

      {/* Credentials dialog */}
      <Modal
        isOpen={!!createdCredentials}
        title="Student Created Successfully!"
        onClose={() => dispatch({ type: 'CLOSE_CREDENTIALS' })}
        onSubmit={() => dispatch({ type: 'CLOSE_CREDENTIALS' })}
        submitLabel="Got it!"
      >
        {createdCredentials && (
          <>
            <p style={{ color: '#718096', margin: '0 0 15px' }}>Share these credentials with the student</p>
            <div className="credentials-box">
              {[
                { label: 'Enrollment Number', value: createdCredentials.enrollmentNumber },
                { label: 'Username', value: createdCredentials.username },
                { label: 'Password', value: createdCredentials.password, secret: true }
              ].map((row) => (
                <div key={row.label} className="credentials-row">
                  <div>
                    <div className="credentials-label">{row.label}</div>
                    <div className={`credentials-value${row.secret ? ' credentials-secret' : ''}`}>{row.value}</div>
                  </div>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    onClick={() => { try { navigator.clipboard?.writeText(String(row.value ?? '')); } catch { /* clipboard unavailable */ } }}
                  >
                    Copy
                  </button>
                </div>
              ))}
            </div>
            <div className="credentials-warning">⚠ Please save these credentials! The password cannot be recovered.</div>
          </>
        )}
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
        <div style={{ fontSize: '0.8rem', color: '#718096', fontFamily: 'monospace' }}>{student.username || 'N/A'}</div>
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
