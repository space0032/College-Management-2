import React, { useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getTimetable, saveTimetableEntry, deleteTimetableEntry } from '../services/timetableService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'day', label: 'Day' },
  { key: 'timeSlot', label: 'Time Slot' },
  { key: 'subject', label: 'Subject' },
  { key: 'room', label: 'Room' },
  { key: 'faculty', label: 'Faculty' },
];

const EMPTY_FORM = { day: '', timeSlot: '', subject: '', room: '', faculty: '' };
const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

const TimetablePage = () => {
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [dept, setDept] = useState('');
  const [semester, setSemester] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const handleFetch = async () => {
    if (!dept || !semester) { setError('Please enter both Department and Semester.'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await getTimetable(dept, semester);
      setEntries(res.data || []);
    } catch {
      setError('Failed to load timetable.');
    } finally {
      setLoading(false);
    }
  };

  const handleFormChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setFormError('');
  };

  const handleSave = async () => {
    if (!form.day || !form.timeSlot || !form.subject) { setFormError('Day, time slot, and subject are required.'); return; }
    setSaving(true);
    try {
      await saveTimetableEntry({ ...form, department: dept, semester });
      setModalOpen(false);
      setForm(EMPTY_FORM);
      if (dept && semester) handleFetch();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to save entry.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (row) => {
    if (!window.confirm('Delete this timetable entry?')) return;
    try {
      await deleteTimetableEntry(row.id);
      setEntries((prev) => prev.filter((e) => e.id !== row.id));
    } catch {
      setError('Failed to delete entry.');
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">🗓️ Timetable</h1>
        <button className="btn btn-primary" onClick={() => { setForm(EMPTY_FORM); setFormError(''); setModalOpen(true); }}>
          + Add Entry
        </button>
      </div>

      <div className="filter-bar">
        <input type="text" className="form-control" placeholder="Department" value={dept} onChange={(e) => setDept(e.target.value)} />
        <input type="text" className="form-control" placeholder="Semester" value={semester} onChange={(e) => setSemester(e.target.value)} />
        <button className="btn btn-primary btn-sm" onClick={handleFetch}>Load</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading timetable…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={entries} onDelete={handleDelete} emptyMessage="Select department and semester to load timetable." />
      )}

      <Modal isOpen={modalOpen} title="Add Timetable Entry" onClose={() => setModalOpen(false)} onSubmit={handleSave} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Day</label>
          <select name="day" className="form-control" value={form.day} onChange={handleFormChange}>
            <option value="">Select day</option>
            {DAYS.map((d) => <option key={d} value={d}>{d}</option>)}
          </select>
        </div>
        {[{ name: 'timeSlot', label: 'Time Slot', placeholder: 'e.g. 09:00 - 10:00' }, { name: 'subject', label: 'Subject' }, { name: 'room', label: 'Room' }, { name: 'faculty', label: 'Faculty' }].map(({ name, label, placeholder }) => (
          <div className="form-group" key={name}>
            <label className="form-label">{label}</label>
            <input name={name} type="text" className="form-control" value={form[name]} onChange={handleFormChange} placeholder={placeholder || `Enter ${label.toLowerCase()}`} />
          </div>
        ))}
      </Modal>
    </div>
  );
};

export default TimetablePage;
