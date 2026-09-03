import React, { useEffect, useState } from 'react';
import SessionManager from '../utils/SessionManager';
import { getPendingBookRequests, getStudentBookRequests, createBookRequest, approveBookRequest, rejectBookRequest } from '../services/featureService';

const BookRequestsPage = () => {
  const user = SessionManager.getUser() || {};
  const isStudent = user.role === 'STUDENT';
  const [requests, setRequests] = useState([]);
  const [form, setForm] = useState({ bookId: '', loanPeriodDays: '14', remarks: '' });

  const load = async () => {
    try {
      const res = isStudent ? await getStudentBookRequests(user.username) : await getPendingBookRequests();
      setRequests(res?.data || []);
    } catch (err) {
      setRequests([]);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isStudent, user.username]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.bookId) return;
    try {
      await createBookRequest({ enrollmentId: user.username, bookId: Number(form.bookId), loanPeriodDays: Number(form.loanPeriodDays), remarks: form.remarks });
      setForm({ bookId: '', loanPeriodDays: '14', remarks: '' });
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to create book request');
    }
  };

  const handleAction = async (id, action) => {
    try {
      if (action === 'approve') await approveBookRequest(id);
      else await rejectBookRequest(id, '');
      load();
    } catch (err) {
      alert(err.response?.data?.error || 'Action failed');
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <div>
          <h1>Library Book Requests</h1>
          <p>{isStudent ? 'Request books from the library' : 'Approve or reject book requests'}</p>
        </div>
      </div>

      {isStudent && (
        <div className="card" style={{ padding: '20px', marginBottom: '24px' }}>
          <h3>New Book Request</h3>
          <form onSubmit={handleSubmit} style={{ maxWidth: '420px' }}>
            <div className="form-group">
              <label className="form-label">Book ID</label>
              <input className="form-control" type="number" value={form.bookId} onChange={(e) => setForm({ ...form, bookId: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Loan Period (days)</label>
              <input className="form-control" type="number" value={form.loanPeriodDays} onChange={(e) => setForm({ ...form, loanPeriodDays: e.target.value })} />
            </div>
            <div className="form-group">
              <label className="form-label">Remarks</label>
              <input className="form-control" value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} />
            </div>
            <button className="btn btn-primary" type="submit">Request Book</button>
          </form>
        </div>
      )}

      <div className="card" style={{ padding: '20px' }}>
        <h3>{isStudent ? 'My Requests' : 'Pending Requests'}</h3>
        {requests.length === 0 ? (
          <p style={{ color: '#718096' }}>No book requests found.</p>
        ) : (
          <table className="table">
            <thead>
              <tr>
                {!isStudent && <th>Student</th>}
                <th>Book</th>
                <th>Status</th>
                <th>Request Date</th>
                {!isStudent && <th>Action</th>}
              </tr>
            </thead>
            <tbody>
              {requests.map((r) => (
                <tr key={r.id}>
                  {!isStudent && <td>{r.studentName}</td>}
                  <td>{r.bookTitle || r.bookId}</td>
                  <td><span className="badge badge-secondary">{r.status}</span></td>
                  <td>{r.requestDate ? new Date(r.requestDate).toLocaleDateString() : '—'}</td>
                  {!isStudent && (
                    <td>
                      <button className="btn btn-success" onClick={() => handleAction(r.id, 'approve')}>Approve</button>{' '}
                      <button className="btn btn-secondary" onClick={() => handleAction(r.id, 'reject')}>Reject</button>
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

export default BookRequestsPage;
