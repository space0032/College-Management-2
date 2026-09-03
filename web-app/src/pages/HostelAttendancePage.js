import React, { useEffect, useState } from 'react';
import SessionManager from '../utils/SessionManager';
import { getHostelAttendanceByDate, markHostelAttendance } from '../services/featureService';

const today = () => new Date().toISOString().slice(0, 10);

const HostelAttendancePage = () => {
  const user = SessionManager.getUser() || {};
  const [date, setDate] = useState(today());
  const [records, setRecords] = useState([]);
  const [form, setForm] = useState({ studentId: '', hostelId: '', status: 'PRESENT', remarks: '' });

  const canMark = user.role === 'ADMIN' || user.role === 'WARDEN';

  const load = async () => {
    try {
      const res = await getHostelAttendanceByDate(date);
      setRecords(res?.data || []);
    } catch (err) {
      setRecords([]);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [date]);

  const handleMark = async (e) => {
    e.preventDefault();
    if (!form.studentId) return;
    try {
      await markHostelAttendance({ ...form, studentId: Number(form.studentId), hostelId: Number(form.hostelId || 0), date, remarks: form.remarks || '' });
      setForm({ studentId: '', hostelId: '', status: 'PRESENT', remarks: '' });
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to mark attendance');
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Hostel Attendance</h1>
          <p>Mark and review daily hostel attendance</p>
        </div>
      </div>

      {canMark && (
        <div className="card" style={{ padding: '20px', marginBottom: '24px' }}>
          <h3>Mark Attendance</h3>
          <form onSubmit={handleMark} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr auto', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Student ID</label>
              <input className="form-control" type="number" value={form.studentId} onChange={(e) => setForm({ ...form, studentId: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Hostel ID</label>
              <input className="form-control" type="number" value={form.hostelId} onChange={(e) => setForm({ ...form, hostelId: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Status</label>
              <select className="form-control" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                <option>PRESENT</option>
                <option>ABSENT</option>
                <option>LEAVE</option>
                <option>LATE</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Remarks</label>
              <input className="form-control" value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} />
            </div>
            <button className="btn btn-primary" type="submit" style={{ alignSelf: 'end' }}>Mark</button>
          </form>
        </div>
      )}

      <div className="card" style={{ padding: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3>Attendance for</h3>
          <input className="form-control" type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ width: 'auto' }} />
        </div>
        {records.length === 0 ? (
          <p style={{ color: '#718096' }}>No attendance records for this date.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Hostel</th>
                <th>Status</th>
                <th>Remarks</th>
              </tr>
            </thead>
            <tbody>
              {records.map((r, i) => (
                <tr key={r.id || i}>
                  <td>{r.studentName || r.enrollmentId || r.studentId}</td>
                  <td>{r.hostelId}</td>
                  <td><span className="badge badge-secondary">{r.status}</span></td>
                  <td>{r.remarks || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default HostelAttendancePage;
