import React, { useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAttendance, markAttendance, bulkMarkAttendance, getCourseStats } from '../services/attendanceService';
import { exportToCSV } from '../utils/exportUtils';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'studentId', label: 'Student ID' },
  { key: 'courseId', label: 'Course ID' },
  { key: 'date', label: 'Date' },
  { key: 'status', label: 'Status' },
];

const AttendancePage = () => {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [filterCourse, setFilterCourse] = useState('');
  const [filterDate, setFilterDate] = useState('');
  const [markModal, setMarkModal] = useState(false);
  const [bulkModal, setBulkModal] = useState(false);
  const [markForm, setMarkForm] = useState({ studentId: '', courseId: '', date: '', status: 'PRESENT' });
  const [bulkForm, setBulkForm] = useState({ courseId: '', date: '', status: 'PRESENT' });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [stats, setStats] = useState(null);

  const handleFetch = async () => {
    if (!filterCourse || !filterDate) { setError('Please enter both Course ID and Date.'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await getAttendance(filterCourse, filterDate);
      setRecords(res.data || []);
      try {
        const statsRes = await getCourseStats(filterCourse);
        setStats(statsRes.data.stats || []);
      } catch (err) {
        console.error("Failed to fetch stats", err);
        setStats(null);
      }
    } catch {
      setError('Failed to fetch attendance records.');
    } finally {
      setLoading(false);
    }
  };

  const handleMark = async () => {
    if (!markForm.studentId || !markForm.courseId || !markForm.date) {
      setFormError('All fields are required.');
      return;
    }
    setSaving(true);
    try {
      await markAttendance(markForm);
      setMarkModal(false);
      setMarkForm({ studentId: '', courseId: '', date: '', status: 'PRESENT' });
      if (filterCourse && filterDate) handleFetch();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to mark attendance.');
    } finally {
      setSaving(false);
    }
  };

  const handleBulk = async () => {
    if (!bulkForm.courseId || !bulkForm.date) {
      setFormError('Course ID and Date are required.');
      return;
    }
    setSaving(true);
    try {
      await bulkMarkAttendance(bulkForm);
      setBulkModal(false);
      setBulkForm({ courseId: '', date: '', status: 'PRESENT' });
      if (filterCourse && filterDate) handleFetch();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to bulk mark attendance.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📋 Attendance</h1>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={() => { setMarkForm({ studentId: '', courseId: filterCourse, date: filterDate, status: 'PRESENT' }); setFormError(''); setMarkModal(true); }}>
            Mark Attendance
          </button>
          <button className="btn btn-secondary" onClick={() => { setBulkForm({ courseId: filterCourse, date: filterDate, status: 'PRESENT' }); setFormError(''); setBulkModal(true); }}>
            Bulk Mark
          </button>
          {records.length > 0 && (
            <button className="btn btn-secondary" onClick={() => exportToCSV(
              ['Student ID', 'Course ID', 'Date', 'Status'],
              records.map(r => [r.studentId, r.courseId, r.date, r.status]),
              'attendance_export'
            )}>⬇ Export CSV</button>
          )}
        </div>
      </div>

      <div className="filter-bar">
        <input
          type="text"
          className="form-control"
          placeholder="Course ID"
          value={filterCourse}
          onChange={(e) => setFilterCourse(e.target.value)}
        />
        <input
          type="date"
          className="form-control"
          value={filterDate}
          onChange={(e) => setFilterDate(e.target.value)}
        />
        <button className="btn btn-primary btn-sm" onClick={handleFetch}>Fetch</button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {stats && stats.length > 0 && (
        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-header">
            <h3>📊 Attendance Analytics</h3>
          </div>
          <div className="card-body">
            <div style={{ display: 'flex', gap: 16, overflowX: 'auto', paddingBottom: 8 }}>
              {stats.map(s => (
                <div key={s.studentId} style={{
                  border: '1px solid var(--border)',
                  borderRadius: 8,
                  padding: 12,
                  minWidth: 150,
                  backgroundColor: s.isLow ? '#fff1f0' : 'var(--surface)',
                  borderColor: s.isLow ? '#ffa39e' : 'var(--border)'
                }}>
                  <div style={{ fontWeight: 600 }}>{s.studentName}</div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                    ID: {s.studentId}
                  </div>
                  <div style={{
                    fontSize: '1.25rem',
                    fontWeight: 'bold',
                    color: s.isLow ? '#cf1322' : 'var(--primary)',
                    marginTop: 8
                  }}>
                    {s.percentage.toFixed(1)}%
                  </div>
                  {s.isLow && <div style={{ fontSize: '0.75rem', color: '#cf1322', marginTop: 4, fontWeight: 500 }}>⚠️ Low Attendance</div>}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading attendance…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={records} emptyMessage="No attendance records. Select a course and date to load." />
      )}

      <Modal isOpen={markModal} title="Mark Attendance" onClose={() => setMarkModal(false)} onSubmit={handleMark} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {[{ name: 'studentId', label: 'Student ID' }, { name: 'courseId', label: 'Course ID' }, { name: 'date', label: 'Date', type: 'date' }].map(({ name, label, type = 'text' }) => (
          <div className="form-group" key={name}>
            <label className="form-label">{label}</label>
            <input type={type} name={name} className="form-control" value={markForm[name]} onChange={(e) => setMarkForm((p) => ({ ...p, [name]: e.target.value }))} />
          </div>
        ))}
        <div className="form-group">
          <label className="form-label">Status</label>
          <select className="form-control" value={markForm.status} onChange={(e) => setMarkForm((p) => ({ ...p, status: e.target.value }))}>
            <option value="PRESENT">Present</option>
            <option value="ABSENT">Absent</option>
            <option value="LATE">Late</option>
          </select>
        </div>
      </Modal>

      <Modal isOpen={bulkModal} title="Bulk Mark Attendance" onClose={() => setBulkModal(false)} onSubmit={handleBulk} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {[{ name: 'courseId', label: 'Course ID' }, { name: 'date', label: 'Date', type: 'date' }].map(({ name, label, type = 'text' }) => (
          <div className="form-group" key={name}>
            <label className="form-label">{label}</label>
            <input type={type} name={name} className="form-control" value={bulkForm[name]} onChange={(e) => setBulkForm((p) => ({ ...p, [name]: e.target.value }))} />
          </div>
        ))}
        <div className="form-group">
          <label className="form-label">Status</label>
          <select className="form-control" value={bulkForm.status} onChange={(e) => setBulkForm((p) => ({ ...p, status: e.target.value }))}>
            <option value="PRESENT">Present</option>
            <option value="ABSENT">Absent</option>
            <option value="LATE">Late</option>
          </select>
        </div>
      </Modal>
    </div>
  );
};

export default AttendancePage;
