import React, { useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAttendance, markAttendance, bulkMarkAttendance, getCourseStats } from '../services/attendanceService';
import { getAllStudents } from '../services/studentService';
import { exportToCSV } from '../utils/exportUtils';
import { BarChart, Bar, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, ReferenceLine } from 'recharts';

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
  const [bulkStudents, setBulkStudents] = useState([]); // List for the rich grid
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

  const handleFetchStudentsForBulk = async () => {
    if (!bulkForm.courseId) return;
    try {
      const res = await getAllStudents(); // In a real app, this would be filtered by course
      // Simulate filtering for demo:
      const courseStudents = (res.data || []).map(s => ({ ...s, status: bulkForm.status }));
      setBulkStudents(courseStudents);
    } catch {
      setFormError('Failed to load student list.');
    }
  };

  const handleBulk = async () => {
    if (!bulkForm.courseId || !bulkForm.date || bulkStudents.length === 0) {
      setFormError('Course, Date, and Student list are required.');
      return;
    }
    setSaving(true);
    try {
      // If the backend has a real batch endpoint, we'd use it.
      // Otherwise, we loop or use bulkMarkAttendance if it supports student lists.
      await bulkMarkAttendance({
        ...bulkForm,
        studentIds: bulkStudents.filter(s => s.status === 'PRESENT').map(s => s.id)
      });
      setBulkModal(false);
      setBulkForm({ courseId: '', date: '', status: 'PRESENT' });
      setBulkStudents([]);
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
            <div style={{ width: '100%', height: 250, marginBottom: '20px' }}>
              <ResponsiveContainer>
                <BarChart data={stats} margin={{ top: 20, right: 30, left: -20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="studentName" axisLine={false} tickLine={false} tick={{ fontSize: 10 }} />
                  <YAxis axisLine={false} tickLine={false} domain={[0, 100]} />
                  <Tooltip cursor={{ fill: '#f1f5f9' }} contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} />
                  <ReferenceLine y={75} stroke="#ef4444" strokeDasharray="3 3" label={{ position: 'insideTopLeft', value: 'Min 75%', fill: '#ef4444', fontSize: 12 }} />
                  <Bar dataKey="percentage" radius={[4, 4, 0, 0]} barSize={40}>
                    {
                      stats.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.isLow ? '#ef4444' : '#10b981'} />
                      ))
                    }
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
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

      <Modal isOpen={bulkModal} title="Bulk Mark Attendance" onClose={() => setBulkModal(false)} onSubmit={handleBulk} submitLabel={saving ? 'Saving…' : 'Submit Batch'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div style={{ display: 'flex', gap: '15px', marginBottom: '15px' }}>
          <div className="form-group" style={{ flex: 1 }}>
            <label className="form-label">Course ID</label>
            <input type="text" className="form-control" value={bulkForm.courseId} onChange={(e) => setBulkForm((p) => ({ ...p, courseId: e.target.value }))} />
          </div>
          <div className="form-group" style={{ flex: 1 }}>
            <label className="form-label">Date</label>
            <input type="date" className="form-control" value={bulkForm.date} onChange={(e) => setBulkForm((p) => ({ ...p, date: e.target.value }))} />
          </div>
          <div style={{ alignSelf: 'flex-end', paddingBottom: '16px' }}>
            <button type="button" className="btn btn-secondary btn-sm" onClick={handleFetchStudentsForBulk}>Load Class List</button>
          </div>
        </div>

        {bulkStudents.length > 0 && (
          <div style={{ maxHeight: '300px', overflowY: 'auto', border: '1px solid #edf2f7', borderRadius: '8px' }}>
            <table className="data-table" style={{ fontSize: '0.85rem' }}>
              <thead style={{ position: 'sticky', top: 0, zIndex: 1, background: 'white' }}>
                <tr><th>Student</th><th>Status</th></tr>
              </thead>
              <tbody>
                {bulkStudents.map((s, idx) => (
                  <tr key={s.id}>
                    <td>{s.name} <span style={{ color: '#94a3b8', fontSize: '0.7rem' }}>(ID: {s.id})</span></td>
                    <td>
                      <select
                        value={s.status}
                        onChange={(e) => {
                          const newList = [...bulkStudents];
                          newList[idx].status = e.target.value;
                          setBulkStudents(newList);
                        }}
                        style={{ padding: '2px', fontSize: '0.8rem' }}
                      >
                        <option value="PRESENT">Present</option>
                        <option value="ABSENT">Absent</option>
                        <option value="LATE">Late</option>
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default AttendancePage;
