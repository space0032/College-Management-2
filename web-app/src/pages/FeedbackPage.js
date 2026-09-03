import React, { useEffect, useState } from 'react';
import SessionManager from '../utils/SessionManager';
import { submitFeedback, getStudentFeedback } from '../services/featureService';

const FeedbackPage = () => {
  const user = SessionManager.getUser() || {};
  const isStudent = user.role === 'STUDENT';
  const [feedback, setFeedback] = useState([]);
  const [queryId, setQueryId] = useState(isStudent ? user.id || '' : '');
  const [form, setForm] = useState({ facultyId: '', feedbackText: '', category: 'General', private: false });

  const load = async () => {
    if (!queryId) return;
    try {
      const res = await getStudentFeedback(Number(queryId));
      setFeedback(res?.data || []);
    } catch (err) {
      setFeedback([]);
    }
  };

  useEffect(() => {
    if (isStudent) load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isStudent]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.facultyId || !form.feedbackText) return;
    try {
      await submitFeedback({ studentId: user.id, facultyId: Number(form.facultyId), feedbackText: form.feedbackText, category: form.category, private: form.private });
      setForm({ facultyId: '', feedbackText: '', category: 'General', private: false });
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to submit feedback');
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Student Feedback</h1>
          <p>{isStudent ? 'Share anonymous feedback about faculty' : 'Review student feedback'}</p>
        </div>
      </div>

      {isStudent && (
        <div className="card" style={{ padding: '20px', marginBottom: '24px' }}>
          <h3>Submit Feedback</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Faculty ID</label>
              <input className="form-control" type="number" value={form.facultyId} onChange={(e) => setForm({ ...form, facultyId: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Category</label>
              <select className="form-control" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
                <option>General</option>
                <option>Teaching</option>
                <option>Course Content</option>
                <option>Other</option>
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Feedback</label>
              <textarea className="form-control" rows="3" value={form.feedbackText} onChange={(e) => setForm({ ...form, feedbackText: e.target.value })} required />
            </div>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
              <input type="checkbox" checked={form.private} onChange={(e) => setForm({ ...form, private: e.target.checked })} />
              Keep private
            </label>
            <button className="btn btn-primary" type="submit">Submit Feedback</button>
          </form>
        </div>
      )}

      <div className="card" style={{ padding: '20px' }}>
        <h3>Feedback History</h3>
        {!isStudent && (
          <div style={{ display: 'flex', gap: '12px', marginBottom: '16px' }}>
            <input className="form-control" type="number" placeholder="Student ID" value={queryId} onChange={(e) => setQueryId(e.target.value)} style={{ maxWidth: '200px' }} />
            <button className="btn btn-secondary" onClick={load}>Load</button>
          </div>
        )}
        {feedback.length === 0 ? (
          <p style={{ color: '#718096' }}>No feedback found.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                <th>Faculty</th>
                <th>Category</th>
                <th>Feedback</th>
                <th>Private</th>
              </tr>
            </thead>
            <tbody>
              {feedback.map((f) => (
                <tr key={f.id}>
                  <td>{f.facultyName || f.facultyId}</td>
                  <td>{f.category}</td>
                  <td>{f.feedbackText}</td>
                  <td>{f.private ? 'Yes' : 'No'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default FeedbackPage;
