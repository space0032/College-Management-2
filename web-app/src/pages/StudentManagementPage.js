import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllStudents, createStudent, searchStudents } from '../services/studentService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'name', label: 'Name' },
  { key: 'email', label: 'Email' },
  { key: 'course', label: 'Course' },
  { key: 'department', label: 'Department' },
  { key: 'semester', label: 'Semester' },
];

const EMPTY_FORM = { name: '', email: '', phone: '', course: '', department: '', semester: '' };

const StudentManagementPage = () => {
  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchStudents = () => {
    setLoading(true);
    getAllStudents()
      .then((res) => setStudents(res.data || []))
      .catch(() => setError('Failed to load students.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchStudents(); }, []);

  const handleSearch = async () => {
    if (!search.trim()) return fetchStudents();
    setLoading(true);
    try {
      const res = await searchStudents(search);
      setStudents(res.data || []);
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

  const handleAdd = async () => {
    if (!form.name || !form.email) { setFormError('Name and email are required.'); return; }
    setSaving(true);
    try {
      await createStudent(form);
      setModalOpen(false);
      setForm(EMPTY_FORM);
      fetchStudents();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create student.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🎓 Student Management</h1>
        <div className="page-actions">
          <div className="search-bar">
            <input
              type="text"
              className="form-control"
              placeholder="Search students…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button className="btn btn-secondary btn-sm" onClick={handleSearch}>Search</button>
          </div>
          <button className="btn btn-primary" onClick={() => { setForm(EMPTY_FORM); setModalOpen(true); }}>
            + Add Student
          </button>
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading students…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={students} emptyMessage="No students found." />
      )}

      <Modal
        isOpen={modalOpen}
        title="Add Student"
        onClose={() => setModalOpen(false)}
        onSubmit={handleAdd}
        submitLabel={saving ? 'Saving…' : 'Save'}
      >
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {['name', 'email', 'phone', 'course', 'department', 'semester'].map((field) => (
          <div className="form-group" key={field}>
            <label className="form-label" htmlFor={`student-${field}`}>
              {field.charAt(0).toUpperCase() + field.slice(1)}
            </label>
            <input
              id={`student-${field}`}
              name={field}
              type={field === 'email' ? 'email' : 'text'}
              className="form-control"
              value={form[field]}
              onChange={handleFormChange}
              placeholder={`Enter ${field}`}
            />
          </div>
        ))}
      </Modal>
    </div>
  );
};

export default StudentManagementPage;
