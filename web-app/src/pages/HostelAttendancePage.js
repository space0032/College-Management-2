import React, { useEffect, useState } from 'react';
import SessionManager from '../utils/SessionManager';
import { getHostelAttendanceByDate, markHostelAttendance } from '../services/featureService';
import { getAllStudents } from '../services/studentService';
import Modal from '../components/Modal';
import { toast } from '../components/Toast';
import { getErrorMessage, getSuccessRefId } from '../utils/error';
import { SkeletonTable } from '../components/Skeleton';

const today = () => new Date().toISOString().slice(0, 10);

const HostelAttendancePage = () => {
  const user = SessionManager.getUser() || {};
  const [date, setDate] = useState(today());
  const [records, setRecords] = useState([]);
  const [form, setForm] = useState({ enrollmentId: '', hostelId: '', status: 'PRESENT', remarks: '' });
  const [students, setStudents] = useState([]);
  const [markOpen, setMarkOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');

  const canMark = user.role === 'ADMIN' || user.role === 'WARDEN';
  const isDirty = Boolean(form.enrollmentId || form.hostelId || form.remarks);

  const load = async (signal) => {
    setLoading(true);
    setLoadError('');
    try {
      const res = await getHostelAttendanceByDate(date, signal);
      setRecords(res?.data || []);
    } catch (err) {
      if (err?.code === 'ERR_CANCELED' || err?.name === 'CanceledError') return;
      setRecords([]);
      setLoadError(err?.response?.data?.error || 'Could not load attendance for this date.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal);
    getAllStudents().then(res => setStudents((res.data || []).map(s => ({ id: s.id, name: s.name, username: s.username })))).catch(() => {});
    return () => controller.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [date]);

  const handleMark = async () => {
    if (!form.enrollmentId) return;
    setSaving(true);
    try {
      await markHostelAttendance({ ...form, enrollmentId: form.enrollmentId, hostelId: Number(form.hostelId || 0), date, remarks: form.remarks || '' });
      setForm({ enrollmentId: '', hostelId: '', status: 'PRESENT', remarks: '' });
      setMarkOpen(false);
      toast.success('Hostel attendance marked.', { refId: getSuccessRefId() });
      load();
    } catch (err) {
      const { message, status, refId } = getErrorMessage(err, 'Could not mark hostel attendance.');
      toast.error(message, { refId, details: { status } });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Hostel Attendance</h1>
          <p>Mark and review daily hostel attendance</p>
        </div>
        {canMark && (
          <div className="page-actions">
            <button className="btn btn-primary" onClick={() => setMarkOpen(true)}>+ Mark Attendance</button>
          </div>
        )}
      </div>

      <Modal
        isOpen={markOpen}
        title="Mark Hostel Attendance"
        onClose={() => setMarkOpen(false)}
        onSubmit={handleMark}
        submitLabel="Mark"
        submitting={saving}
        submitDisabled={!form.enrollmentId}
        isDirty={isDirty}
        size="medium"
      >
        <form onSubmit={(e) => { e.preventDefault(); handleMark(); }}>
          <div className="form-grid">
            <div className="form-group form-span-2">
              <label className="form-label">Enrollment No. *</label>
              <select className="form-control" value={form.enrollmentId} onChange={(e) => setForm({ ...form, enrollmentId: e.target.value })} required>
                <option value="">Select student / enrollment…</option>
                {students.map(s => <option key={s.id} value={s.username}>{s.name} ({s.username})</option>)}
              </select>
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
            <div className="form-group form-span-2">
              <label className="form-label">Remarks</label>
              <input className="form-control" value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} placeholder="Optional note" />
            </div>
            <div className="form-group form-span-2">
              <label className="form-label">Date</label>
              <input className="form-control" value={date} disabled />
            </div>
          </div>
        </form>
      </Modal>

      <div className="card" style={{ padding: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3>Attendance for</h3>
          <input className="form-control" type="date" value={date} onChange={(e) => setDate(e.target.value)} style={{ width: 'auto' }} />
        </div>
        {loadError && (
          <div className="retry-bar" role="alert">
            <span>{loadError}</span>
            <button className="btn btn-secondary btn-sm" onClick={() => load()}>Retry</button>
          </div>
        )}
        {loading ? (
          <SkeletonTable rows={5} cols={4} />
        ) : records.length === 0 ? (
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
                  <td>{r.studentName || r.enrollmentId || r.enrollmentNumber || r.username || r.studentId || 'N/A'}</td>
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
