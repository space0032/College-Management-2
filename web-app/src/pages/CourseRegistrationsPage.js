import React, { useEffect, useState } from 'react';
import SessionManager from '../utils/SessionManager';
import { getPendingRegistrations, registerForCourse, approveRegistration, rejectRegistration } from '../services/featureService';
import { getAllStudents } from '../services/studentService';

const CourseRegistrationsPage = () => {
  const user = SessionManager.getUser() || {};
  const isStudent = user.role === 'STUDENT';
  const [pending, setPending] = useState([]);
  const [students, setStudents] = useState([]);
  const [form, setForm] = useState({ enrollmentId: user.username || '', courseId: '' });

  useEffect(() => {
    getAllStudents().then(res => setStudents((res.data || []).map(s => ({ id: s.id, name: s.name, username: s.username })))).catch(() => {});
  }, []);

  const load = async () => {
    if (isStudent) return;
    try {
      const res = await getPendingRegistrations();
      setPending(res?.data || []);
    } catch (err) {
      setPending([]);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isStudent]);

  const handleRequest = async (e) => {
    e.preventDefault();
    if (!form.courseId) return;
    try {
      const res = await registerForCourse({ enrollmentId: form.enrollmentId, courseId: Number(form.courseId) });
      alert(res?.data?.message || 'Registration requested');
      setForm({ enrollmentId: user.username || '', courseId: '' });
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to register');
    }
  };

  const handleAction = async (id, action) => {
    try {
      if (action === 'approve') await approveRegistration(id);
      else await rejectRegistration(id);
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Action failed');
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Course Registration</h1>
          <p>{isStudent ? 'Request elective course registration' : 'Review and manage registration requests'}</p>
        </div>
      </div>

      {!isStudent && (
        <div className="card" style={{ padding: '20px', marginBottom: '24px' }}>
          <h3>Pending Requests</h3>
          {pending.length === 0 ? (
            <p style={{ color: '#718096' }}>No pending course registration requests.</p>
          ) : (
            <table className="table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Course</th>
                  <th>Code</th>
                  <th>Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {pending.map((r) => (
                  <tr key={r.id}>
                    <td>{r.studentName}</td>
                    <td>{r.courseName}</td>
                    <td>{r.courseCode}</td>
                    <td>{r.date}</td>
                    <td>
                      <button className="btn btn-success" onClick={() => handleAction(r.id, 'approve')}>Approve</button>{' '}
                      <button className="btn btn-secondary" onClick={() => handleAction(r.id, 'reject')}>Reject</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      <div className="card" style={{ padding: '20px' }}>
        <h3>{isStudent ? 'Request Registration' : 'Register a Student'}</h3>
        <form onSubmit={handleRequest} style={{ maxWidth: '420px' }}>
          <div className="form-group">
            <label className="form-label">Enrollment No.</label>
            <select className="form-control" value={form.enrollmentId} onChange={(e) => setForm({ ...form, enrollmentId: e.target.value })} required>
              <option value="">Select student / enrollment…</option>
              {students.map(s => <option key={s.id} value={s.username}>{s.name} ({s.username})</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Course ID</label>
            <input className="form-control" type="number" value={form.courseId} onChange={(e) => setForm({ ...form, courseId: e.target.value })} required />
          </div>
          <button className="btn btn-primary" type="submit">Submit Request</button>
        </form>
      </div>
    </div>
  );
};

export default CourseRegistrationsPage;
