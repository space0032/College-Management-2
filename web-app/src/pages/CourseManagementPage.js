import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllCourses, createCourse, updateCourse, deleteCourse } from '../services/courseService';
import { exportToCSV } from '../utils/exportUtils';
import SessionManager from '../utils/SessionManager';

const EMPTY_FORM = { name: '', code: '', credits: '', department: '', semester: '' };

const CourseManagementPage = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [searchQ, setSearchQ] = useState('');
  const [filterDept, setFilterDept] = useState('');
  const [filterSem, setFilterSem] = useState('');

  const userRole = SessionManager.getUserRole() || 'STUDENT';
  const canManage = userRole === 'ADMIN' || userRole === 'FACULTY';

  const fetchCourses = () => {
    setLoading(true);
    getAllCourses()
      .then((res) => setCourses(res.data || []))
      .catch(() => setError('Failed to load courses.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchCourses(); }, []);

  const handleFormChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setFormError('');
  };

  const openAdd = () => { setForm(EMPTY_FORM); setEditId(null); setFormError(''); setModalOpen(true); };
  const openEdit = (row) => {
    setForm({ name: row.name || '', code: row.code || '', credits: String(row.credits || ''), department: row.department || '', semester: String(row.semester || '') });
    setEditId(row.id); setFormError(''); setModalOpen(true);
  };

  const handleSave = async () => {
    if (!form.name || !form.code) { setFormError('Name and code are required.'); return; }
    setSaving(true);
    try {
      if (editId) {
        await updateCourse(editId, form);
      } else {
        await createCourse(form);
      }
      setModalOpen(false);
      fetchCourses();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to save course.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (row) => {
    if (!window.confirm(`Delete course "${row.name}"?`)) return;
    try {
      await deleteCourse(row.id);
      fetchCourses();
    } catch {
      setError('Failed to delete course.');
    }
  };

  // Derived data
  const departments = [...new Set(courses.map(c => c.department).filter(Boolean))].sort();
  const semesters = [...new Set(courses.map(c => String(c.semester)).filter(Boolean))].sort((a, b) => Number(a) - Number(b));

  const filtered = courses
    .filter(c => !searchQ || (c.name || '').toLowerCase().includes(searchQ.toLowerCase()) || (c.code || '').toLowerCase().includes(searchQ.toLowerCase()))
    .filter(c => !filterDept || c.department === filterDept)
    .filter(c => !filterSem || String(c.semester) === filterSem);

  const totalCredits = filtered.reduce((s, c) => s + (Number(c.credits) || 0), 0);
  const avgCredits = filtered.length ? (totalCredits / filtered.length).toFixed(1) : '0';

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
          {canManage && <button className="btn btn-primary" onClick={openAdd}>+ Add Course</button>}
        </div>
      </div>

      {/* Stats row */}
      <div style={{ display: 'flex', gap: '12px', marginBottom: '18px', flexWrap: 'wrap' }}>
        {[
          { label: 'Total Courses', value: courses.length, color: '#2b6cb0', bg: '#ebf8ff' },
          { label: 'Departments', value: departments.length, color: '#276749', bg: '#f0fff4' },
          { label: 'Filtered', value: filtered.length, color: '#c05621', bg: '#fffaf0' },
          { label: 'Avg Credits', value: avgCredits, color: '#9f7aea', bg: '#faf5ff' },
          { label: 'Total Credits', value: totalCredits, color: '#4a5568', bg: '#f7fafc' },
        ].map(s => (
          <div key={s.label} style={{ padding: '8px 16px', background: s.bg, borderRadius: '8px', minWidth: '100px', textAlign: 'center' }}>
            <div style={{ fontWeight: 'bold', fontSize: '1.2rem', color: s.color }}>{s.value}</div>
            <div style={{ fontSize: '0.73rem', color: '#718096' }}>{s.label}</div>
          </div>
        ))}
      </div>

      {/* Search + Filters */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '18px', alignItems: 'center', flexWrap: 'wrap' }}>
        <input type="text" placeholder="Search by name or code…" value={searchQ} onChange={e => setSearchQ(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', flex: '1 1 200px', fontSize: '0.87rem' }} />
        <select value={filterDept} onChange={e => setFilterDept(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '0.87rem' }}>
          <option value="">All Departments</option>
          {departments.map(d => <option key={d} value={d}>{d}</option>)}
        </select>
        <select value={filterSem} onChange={e => setFilterSem(e.target.value)}
          style={{ padding: '8px 12px', border: '1px solid #e2e8f0', borderRadius: '8px', fontSize: '0.87rem' }}>
          <option value="">All Semesters</option>
          {semesters.map(s => <option key={s} value={s}>Semester {s}</option>)}
        </select>
        {(searchQ || filterDept || filterSem) && (
          <button className="btn btn-secondary btn-sm" onClick={() => { setSearchQ(''); setFilterDept(''); setFilterSem(''); }}>
            ✕ Clear
          </button>
        )}
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading courses…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={filtered} onEdit={canManage ? openEdit : undefined} onDelete={canManage ? handleDelete : undefined} emptyMessage="No courses found." />
      )}

      <Modal isOpen={modalOpen} title={editId ? 'Edit Course' : 'Add Course'} onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-grid">
          {[
            { field: 'name', label: 'Course Name *', gridCol: '1 / -1' },
            { field: 'code', label: 'Course Code *' },
            { field: 'credits', label: 'Credits', type: 'number' },
          ].map(({ field, label, type, gridCol }) => (
            <div className="form-group" key={field} style={gridCol ? { gridColumn: gridCol } : {}}>
              <label className="form-label" htmlFor={`course-${field}`}>{label}</label>
              <input id={`course-${field}`} name={field} type={type || 'text'} className="form-control" value={form[field]} onChange={handleFormChange} placeholder={`Enter ${label.replace(' *', '').toLowerCase()}`} />
            </div>
          ))}
          <div className="form-group">
            <label className="form-label">Department</label>
            <input name="department" type="text" className="form-control" value={form.department} onChange={handleFormChange} placeholder="e.g. Computer Science" list="dept-list" />
            <datalist id="dept-list">{departments.map(d => <option key={d} value={d} />)}</datalist>
          </div>
          <div className="form-group">
            <label className="form-label">Semester</label>
            <select name="semester" className="form-control" value={form.semester} onChange={handleFormChange}>
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
