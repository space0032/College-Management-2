import React, { useEffect, useState } from 'react';
import SessionManager from '../utils/SessionManager';
import { getAllComplaints, getStudentComplaints, createComplaint, updateComplaintStatus } from '../services/featureService';

const HostelComplaintsPage = () => {
  const user = SessionManager.getUser() || {};
  const isStudent = user.role === 'STUDENT';
  const [complaints, setComplaints] = useState([]);
  const [form, setForm] = useState({ title: '', description: '', category: 'General' });

  const load = async () => {
    try {
      if (isStudent) {
        const res = await getStudentComplaints(user.id);
        setComplaints(res?.data || []);
      } else {
        const res = await getAllComplaints();
        setComplaints(res?.data || []);
      }
    } catch (err) {
      setComplaints([]);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.id, isStudent]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title) return;
    try {
      await createComplaint({ studentId: user.id, ...form });
      setForm({ title: '', description: '', category: 'General' });
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to submit complaint');
    }
  };

  const handleStatus = async (id, status) => {
    try {
      await updateComplaintStatus(id, { status, resolvedBy: user.id });
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to update complaint');
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Hostel Complaints</h1>
          <p>File and track hostel complaints</p>
        </div>
      </div>

      {isStudent && (
        <div className="card" style={{ padding: '20px', marginBottom: '24px' }}>
          <h3>Submit a Complaint</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Title</label>
              <input className="form-control" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Short title" required />
            </div>
            <div className="form-group">
              <label className="form-label">Category</label>
              <select className="form-control" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
                <option>General</option>
                <option>Maintenance</option>
                <option>Sanitation</option>
                <option>Security</option>
                <option>Food</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Description</label>
              <textarea className="form-control" rows="3" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <button className="btn btn-primary" type="submit">Submit Complaint</button>
          </form>
        </div>
      )}

      <div className="card" style={{ padding: '20px' }}>
        <h3>Complaints</h3>
        {complaints.length === 0 ? (
          <p style={{ color: '#718096' }}>No complaints found.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                {!isStudent && <th>Student</th>}
                <th>Title</th>
                <th>Category</th>
                <th>Status</th>
                <th>Remarks</th>
                {!isStudent && <th>Action</th>}
              </tr>
            </thead>
            <tbody>
              {complaints.map((c) => (
                <tr key={c.id}>
                  {!isStudent && <td>{c.studentName || c.studentId}</td>}
                  <td>{c.title}</td>
                  <td>{c.category}</td>
                  <td><span className={`badge badge-${c.status === 'OPEN' ? 'warning' : 'success'}`}>{c.status}</span></td>
                  <td>{c.remarks || '—'}</td>
                  {!isStudent && (
                    <td>
                      {c.status === 'OPEN' ? (
                        <button className="btn btn-secondary" onClick={() => handleStatus(c.id, 'RESOLVED')}>Resolve</button>
                      ) : (
                        <button className="btn btn-secondary" onClick={() => handleStatus(c.id, 'OPEN')}>Reopen</button>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default HostelComplaintsPage;
