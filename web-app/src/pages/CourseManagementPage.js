import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllCourses, createCourse, updateCourse, deleteCourse } from '../services/courseService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Name' },
  { key: 'code', label: 'Code' },
  { key: 'credits', label: 'Credits' },
  { key: 'department', label: 'Department' },
  { key: 'semester', label: 'Semester' },
];

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

  const openAdd = () => { setForm(EMPTY_FORM); setEditId(null); setModalOpen(true); };
  const openEdit = (row) => {
    setForm({ name: row.name || '', code: row.code || '', credits: String(row.credits || ''), department: row.department || '', semester: String(row.semester || '') });
    setEditId(row.id);
    setModalOpen(true);
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

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📚 Course Management</h1>
        <button className="btn btn-primary" onClick={openAdd}>+ Add Course</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading courses…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={courses} onEdit={openEdit} onDelete={handleDelete} emptyMessage="No courses found." />
      )}

      <Modal
        isOpen={modalOpen}
        title={editId ? 'Edit Course' : 'Add Course'}
        onClose={() => setModalOpen(false)}
        onSubmit={handleSave}
        submitLabel={saving ? 'Saving…' : 'Save'}
      >
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {[
          { field: 'name', label: 'Course Name' },
          { field: 'code', label: 'Course Code' },
          { field: 'credits', label: 'Credits' },
          { field: 'department', label: 'Department' },
          { field: 'semester', label: 'Semester' },
        ].map(({ field, label }) => (
          <div className="form-group" key={field}>
            <label className="form-label" htmlFor={`course-${field}`}>{label}</label>
            <input
              id={`course-${field}`}
              name={field}
              type={['credits', 'semester'].includes(field) ? 'number' : 'text'}
              className="form-control"
              value={form[field]}
              onChange={handleFormChange}
              placeholder={`Enter ${label.toLowerCase()}`}
            />
          </div>
        ))}
      </Modal>
    </div>
  );
};

export default CourseManagementPage;
