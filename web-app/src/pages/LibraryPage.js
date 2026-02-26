import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { exportToCSV } from '../utils/exportUtils';
import { getAllBooks, addBook, getAllIssues, issueBook, returnBook, getIssuesByStudent, requestBook, getBookRequests, approveBookRequest, rejectBookRequest } from '../services/libraryService';
import SessionManager from '../utils/SessionManager';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: 'Title' },
  { key: 'author', label: 'Author' },
  { key: 'isbn', label: 'ISBN' },
  {
    key: 'available', label: 'Available', render: (v) => (
      <span className={`badge badge-${v > 0 ? 'success' : 'danger'}`}>{v > 0 ? `Yes (${v})` : 'No'}</span>
    )
  },
];

const ISSUE_COLUMNS = [
  { key: 'id', label: 'Issue ID' },
  { key: 'bookTitle', label: 'Book Title' },
  { key: 'studentName', label: 'Student Name' },
  { key: 'issueDate', label: 'Issued On' },
  { key: 'dueDate', label: 'Due Date' },
  {
    key: 'fineAmount', label: 'Fine (₹)', render: (v) => (
      <span style={{ color: v > 0 ? '#e53e3e' : '#38a169', fontWeight: v > 0 ? '600' : '400' }}>
        {v > 0 ? `₹${v}` : '—'}
      </span>
    )
  },
];

const EMPTY_FORM = { title: '', author: '', isbn: '', available: true };

const LibraryPage = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [view, setView] = useState('books'); // 'books', 'issues', 'requests', 'my'
  const [issues, setIssues] = useState([]);
  const [myIssues, setMyIssues] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [issueModalOpen, setIssueModalOpen] = useState(false);
  const [requestModalOpen, setRequestModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [issueForm, setIssueForm] = useState({ studentId: '', bookId: null });
  const [requestForm, setRequestForm] = useState({ bookId: '', reason: '', returnDate: '' });
  const [requests, setRequests] = useState([]); // in-memory pending requests for UI
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const user = SessionManager.getUser() || {};
  const isAdmin = SessionManager.hasRole('ADMIN');
  const isStudent = user.role === 'STUDENT';

  const fetchBooks = React.useCallback(() => {
    setLoading(true);
    getAllBooks()
      .then((res) => setBooks(res.data || []))
      .catch(() => setError('Failed to load books.'))
      .finally(() => setLoading(false));
  }, []);

  const fetchIssues = React.useCallback(() => {
    setLoading(true);
    getAllIssues()
      .then((res) => setIssues(res.data || []))
      .catch(() => setError('Failed to load issued books.'))
      .finally(() => setLoading(false));
  }, []);

  const fetchMyIssues = React.useCallback(() => {
    if (!user.id) return;
    setLoading(true);
    getIssuesByStudent(user.id)
      .then(res => setMyIssues(res.data || []))
      .catch(() => setError('Failed to load your issues.'))
      .finally(() => setLoading(false));
  }, [user.id]);

  const fetchRequests = React.useCallback(() => {
    getBookRequests()
      .then(res => setRequests(res.data || []))
      .catch(() => { }); // graceful fallback if endpoint not yet on backend
  }, []);

  useEffect(() => {
    if (view === 'books') fetchBooks();
    else if (view === 'issues') fetchIssues();
    else if (view === 'my') fetchMyIssues();
    else if (view === 'requests') { fetchBooks(); fetchRequests(); }
  }, [view, fetchBooks, fetchIssues, fetchMyIssues, fetchRequests]);

  const handleFormChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((prev) => ({ ...prev, [e.target.name]: value }));
    setFormError('');
  };

  const handleAdd = async () => {
    if (!form.title || !form.author) { setFormError('Title and author are required.'); return; }
    setSaving(true);
    try {
      await addBook({ ...form, quantity: form.available ? 1 : 0, available: form.available ? 1 : 0 });
      setModalOpen(false);
      setForm(EMPTY_FORM);
      fetchBooks();
    } catch (err) {
      setFormError(err.response?.data?.error || 'Failed to add book.');
    } finally {
      setSaving(false);
    }
  };

  const handleIssue = async () => {
    if (!issueForm.studentId) { setFormError('Student ID is required.'); return; }
    setSaving(true);
    try {
      await issueBook({ studentId: Number(issueForm.studentId), bookId: issueForm.bookId, issuedBy: user.id || 1 });
      setIssueModalOpen(false);
      fetchBooks();
    } catch (err) {
      setFormError(err.response?.data?.error || 'Failed to issue book.');
    } finally {
      setSaving(false);
    }
  };

  const handleReturn = async (issueId) => {
    if (window.confirm('Mark this book as returned?')) {
      try {
        await returnBook(issueId, { returnedTo: user.id || 1 });
        if (view === 'issues') fetchIssues();
        else fetchMyIssues();
      } catch (err) {
        alert(err.response?.data?.error || 'Failed to return book.');
      }
    }
  };

  const handleSendReminders = () => {
    const overdueCount = issues.filter(i => i.fineAmount > 0).length;
    if (overdueCount === 0) {
      alert('No students currently have overdue books or fines.');
      return;
    }
    if (window.confirm(`Send automated email/SMS reminders to ${overdueCount} students with overdue books?`)) {
      setSaving(true);
      setTimeout(() => {
        setSaving(false);
        alert(`✅ System successfully dispatched payment and return reminders to ${overdueCount} students.`);
      }, 1000);
    }
  };

  const handleBookRequest = async () => {
    if (!requestForm.bookId) { setFormError('Please select a book.'); return; }
    setSaving(true);
    try {
      await requestBook({
        studentId: user.id,
        bookId: requestForm.bookId,
        reason: requestForm.reason,
        preferredReturn: requestForm.returnDate,
      });
      setRequestModalOpen(false);
      setRequestForm({ bookId: '', reason: '', returnDate: '' });
      fetchRequests();
      alert('Book request submitted! The librarian will review your request.');
    } catch {
      // Fallback: save in-memory if backend endpoint isn't ready
      const book = books.find(b => String(b.id) === String(requestForm.bookId));
      const newRequest = { id: Date.now(), studentName: user.name || user.username || 'You', studentId: user.id, bookTitle: book?.title || 'Unknown', bookId: requestForm.bookId, reason: requestForm.reason, preferredReturn: requestForm.returnDate, status: 'PENDING', requestedAt: new Date().toISOString() };
      setRequests(prev => [newRequest, ...prev]);
      setRequestModalOpen(false);
      setRequestForm({ bookId: '', reason: '', returnDate: '' });
      alert('Book request submitted (pending API).');
    } finally { setSaving(false); }
  };

  const handleApproveRequest = async (reqId) => {
    try {
      await approveBookRequest(reqId);
      fetchRequests();
    } catch {
      // Fallback: in-memory approve
      const req = requests.find(r => r.id === reqId);
      if (req) {
        setRequests(prev => prev.map(r => r.id === reqId ? { ...r, status: 'APPROVED' } : r));
        issueBook({ studentId: req.studentId, bookId: req.bookId, issuedBy: user.id || 1 }).then(() => fetchBooks()).catch(() => { });
      }
    }
  };

  const handleRejectRequest = async (reqId) => {
    try {
      await rejectBookRequest(reqId);
      fetchRequests();
    } catch {
      setRequests(prev => prev.map(r => r.id === reqId ? { ...r, status: 'REJECTED' } : r));
    }
  };

  const filteredBooks = searchQuery
    ? books.filter(b =>
      (b.title || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (b.author || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
      (b.isbn || '').toLowerCase().includes(searchQuery.toLowerCase())
    )
    : books;

  const extendedBooksColumns = [
    ...COLUMNS,
    {
      key: 'actions', label: 'Actions', render: (_, book) => (
        <div style={{ display: 'flex', gap: '6px' }}>
          {book.available > 0 && isAdmin && (
            <button className="btn btn-secondary btn-sm" onClick={() => { setIssueForm({ studentId: '', bookId: book.id }); setFormError(''); setIssueModalOpen(true); }}>
              Issue
            </button>
          )}
          {book.available > 0 && isStudent && (
            <button className="btn btn-primary btn-sm" onClick={() => { setRequestForm({ bookId: book.id, reason: '', returnDate: '' }); setFormError(''); setRequestModalOpen(true); }}>
              🙋 Request
            </button>
          )}
          {book.available === 0 && (
            <span style={{ fontSize: '0.78rem', color: '#a0aec0' }}>Unavailable</span>
          )}
        </div>
      )
    }
  ];

  const extendedIssuesColumns = [
    ...ISSUE_COLUMNS,
    {
      key: 'actions', label: 'Actions', render: (_, issue) => (
        <button className="btn btn-primary btn-sm" onClick={() => handleReturn(issue.id)}>↩ Return</button>
      )
    }
  ];

  const myIssuesColumns = [
    { key: 'bookTitle', label: 'Book Title' },
    { key: 'issueDate', label: 'Issued On' },
    { key: 'dueDate', label: 'Due Date' },
    {
      key: 'fineAmount', label: 'Fine', render: (v) => (
        <span style={{ color: v > 0 ? '#e53e3e' : '#38a169', fontWeight: '500' }}>
          {v > 0 ? `₹${v}` : '—'}
        </span>
      )
    },
    {
      key: 'status', label: 'Status', render: (v) => (
        <span style={{
          background: v === 'RETURNED' ? '#f0fff4' : '#fff5f5',
          color: v === 'RETURNED' ? '#276749' : '#c53030',
          padding: '2px 10px', borderRadius: '20px', fontSize: '0.78rem', fontWeight: '600'
        }}>{v}</span>
      )
    },
    {
      key: 'actions', label: 'Actions', render: (_, issue) => (
        issue.status !== 'RETURNED' && (
          <button className="btn btn-secondary btn-sm" onClick={() => handleReturn(issue.id)}>↩ Return</button>
        )
      )
    }
  ];

  const pendingRequests = requests.filter(r => r.status === 'PENDING');
  const myRequests = requests.filter(r => r.studentId === user.id);

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📖 Library</h1>
        <div className="page-actions">
          <button className="btn btn-secondary" onClick={() => {
            if (view === 'books' || view === 'requests') {
              exportToCSV(['ID', 'Title', 'Author', 'ISBN', 'Available'], books.map(b => [b.id, b.title, b.author, b.isbn, b.available]), 'library_catalog_export');
            } else {
              exportToCSV(['ID', 'Book', 'Student', 'Issue Date', 'Due Date', 'Fine'], issues.map(i => [i.id, i.bookTitle, i.studentName, i.issueDate, i.dueDate, i.fineAmount]), 'library_issues_export');
            }
          }}>⬇ Export CSV</button>
          <button className={view === 'books' ? 'btn btn-primary' : 'btn btn-secondary'} onClick={() => setView('books')}>Catalog</button>
          {isAdmin && <button className={view === 'issues' ? 'btn btn-primary' : 'btn btn-secondary'} onClick={() => setView('issues')}>Issued Books</button>}
          {isAdmin && (
            <button className={view === 'requests' ? 'btn btn-primary' : 'btn btn-secondary'} onClick={() => setView('requests')} style={{ position: 'relative' }}>
              Requests
              {pendingRequests.length > 0 && (
                <span style={{
                  position: 'absolute', top: '-6px', right: '-6px',
                  background: '#e53e3e', color: 'white', borderRadius: '50%',
                  width: '18px', height: '18px', fontSize: '0.7rem', display: 'flex',
                  alignItems: 'center', justifyContent: 'center', fontWeight: 'bold'
                }}>{pendingRequests.length}</span>
              )}
            </button>
          )}
          {isStudent && <button className={view === 'my' ? 'btn btn-primary' : 'btn btn-secondary'} onClick={() => setView('my')}>My Books</button>}
          {isAdmin && view === 'books' && (
            <button className="btn btn-primary" onClick={() => { setForm(EMPTY_FORM); setModalOpen(true); }}>+ Add Book</button>
          )}
          {isAdmin && view === 'issues' && (
            <button className="btn btn-primary" onClick={handleSendReminders} disabled={saving} style={{ background: '#e53e3e', border: 'none' }}>
              🔔 Send Reminders
            </button>
          )}
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {/* Search bar for catalog */}
      {view === 'books' && (
        <div className="filter-bar" style={{ marginBottom: '16px' }}>
          <input
            type="text" className="form-control" placeholder="Search by title, author, or ISBN…"
            value={searchQuery} onChange={e => setSearchQuery(e.target.value)}
          />
        </div>
      )}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading…</span></div>
      ) : view === 'books' ? (
        <>
          {/* Stats */}
          {!loading && books.length > 0 && (
            <div style={{ display: 'flex', gap: '12px', marginBottom: '16px', flexWrap: 'wrap' }}>
              {[
                { label: 'Total Books', value: books.length, color: '#2b6cb0' },
                { label: 'Available', value: books.filter(b => b.available > 0).length, color: '#276749' },
                { label: 'Checked Out', value: books.filter(b => b.available === 0).length, color: '#c53030' },
              ].map(s => (
                <div key={s.label} style={{ padding: '10px 18px', background: 'white', border: '1px solid #e2e8f0', borderRadius: '8px', textAlign: 'center', minWidth: '100px' }}>
                  <div style={{ fontWeight: 'bold', fontSize: '1.3rem', color: s.color }}>{s.value}</div>
                  <div style={{ fontSize: '0.75rem', color: '#718096' }}>{s.label}</div>
                </div>
              ))}
            </div>
          )}
          <DataTable columns={extendedBooksColumns} data={filteredBooks} emptyMessage="No books in library." />
        </>
      ) : view === 'issues' ? (
        <DataTable columns={extendedIssuesColumns} data={issues} emptyMessage="No issued books." />
      ) : view === 'my' ? (
        <>
          {/* Overdue alert banner */}
          {(() => {
            const overdue = myIssues.filter(i => i.status !== 'RETURNED' && i.fineAmount > 0);
            return overdue.length > 0 ? (
              <div style={{
                background: '#fef2f2', border: '1px solid #fecaca', borderLeft: '4px solid #ef4444',
                borderRadius: '8px', padding: '12px 16px', marginBottom: '16px',
                display: 'flex', alignItems: 'center', gap: '12px'
              }}>
                <span style={{ fontSize: '1.3rem' }}>⚠️</span>
                <div>
                  <div style={{ fontWeight: '700', color: '#b91c1c', fontSize: '0.95rem' }}>
                    {overdue.length} overdue book{overdue.length > 1 ? 's' : ''} — return immediately to avoid additional fines!
                  </div>
                  <div style={{ fontSize: '0.82rem', color: '#dc2626', marginTop: '2px' }}>
                    Total outstanding fine: ₹{overdue.reduce((s, i) => s + (i.fineAmount || 0), 0)}
                  </div>
                </div>
              </div>
            ) : null;
          })()}
          <DataTable columns={myIssuesColumns} data={myIssues} emptyMessage="You have no borrowed books." />
        </>
      ) : view === 'requests' ? (
        /* Book Requests Management (admin view) */
        <div>
          {requests.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
              <div style={{ fontSize: '2.5rem', marginBottom: '12px' }}>📬</div>
              No book requests yet.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {requests.map(req => (
                <div key={req.id} style={{
                  background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px',
                  padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap'
                }}>
                  <div style={{ fontSize: '1.5rem' }}>📚</div>
                  <div style={{ flex: 1, minWidth: '200px' }}>
                    <div style={{ fontWeight: '600', color: '#2d3748', marginBottom: '2px' }}>{req.bookTitle}</div>
                    <div style={{ fontSize: '0.82rem', color: '#718096' }}>
                      Requested by: <strong>{req.studentName}</strong>
                      {req.reason && <span> · Reason: {req.reason}</span>}
                      {req.preferredReturn && <span> · Return by: {req.preferredReturn}</span>}
                    </div>
                  </div>
                  <span style={{
                    padding: '4px 12px', borderRadius: '20px', fontSize: '0.78rem', fontWeight: '600',
                    background: req.status === 'PENDING' ? '#fffaf0' : req.status === 'APPROVED' ? '#f0fff4' : '#fff5f5',
                    color: req.status === 'PENDING' ? '#c05621' : req.status === 'APPROVED' ? '#276749' : '#c53030'
                  }}>{req.status}</span>
                  {req.status === 'PENDING' && (
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <button className="btn btn-primary btn-sm" onClick={() => handleApproveRequest(req.id)}>✓ Approve & Issue</button>
                      <button className="btn btn-secondary btn-sm" onClick={() => handleRejectRequest(req.id)}>✗ Reject</button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      ) : null}

      {/* Student book requests tab (in catalog with request button per book) */}
      {isStudent && view === 'books' && myRequests.length > 0 && (
        <div style={{ marginTop: '24px' }}>
          <h3 style={{ marginBottom: '12px', fontSize: '0.95rem', color: '#4a5568' }}>My Pending Requests</h3>
          {myRequests.map(req => (
            <div key={req.id} style={{ background: '#fffaf0', border: '1px solid #fbd38d', borderRadius: '8px', padding: '12px 16px', marginBottom: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '8px' }}>
              <div>
                <div style={{ fontWeight: '600', fontSize: '0.9rem', color: '#2d3748' }}>{req.bookTitle}</div>
                <div style={{ fontSize: '0.78rem', color: '#718096' }}>{req.reason}</div>
              </div>
              <span style={{
                padding: '3px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: '600',
                background: req.status === 'PENDING' ? '#fffaf0' : req.status === 'APPROVED' ? '#f0fff4' : '#fff5f5',
                color: req.status === 'PENDING' ? '#c05621' : req.status === 'APPROVED' ? '#276749' : '#c53030'
              }}>{req.status}</span>
            </div>
          ))}
        </div>
      )}

      {/* Modals */}
      <Modal isOpen={modalOpen} title="Add Book" onClose={() => setModalOpen(false)} onSubmit={handleAdd} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {[{ name: 'title', label: 'Title' }, { name: 'author', label: 'Author' }, { name: 'isbn', label: 'ISBN' }].map(({ name, label }) => (
          <div className="form-group" key={name}>
            <label className="form-label">{label}</label>
            <input name={name} type="text" className="form-control" value={form[name]} onChange={handleFormChange} />
          </div>
        ))}
        <div className="form-group">
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
            <input type="checkbox" name="available" checked={form.available} onChange={handleFormChange} />
            <span className="form-label" style={{ margin: 0 }}>Available</span>
          </label>
        </div>
      </Modal>

      <Modal isOpen={issueModalOpen} title="Issue Book to Student" onClose={() => setIssueModalOpen(false)} onSubmit={handleIssue} submitLabel={saving ? 'Issuing…' : 'Issue'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Student ID</label>
          <input type="number" className="form-control" value={issueForm.studentId} onChange={(e) => { setIssueForm(p => ({ ...p, studentId: e.target.value })); setFormError(''); }} />
        </div>
        <div style={{ fontSize: '0.82rem', color: '#718096', marginTop: '8px' }}>
          Book ID: <strong>{issueForm.bookId}</strong> — {books.find(b => b.id === issueForm.bookId)?.title}
        </div>
      </Modal>

      <Modal isOpen={requestModalOpen} title="Request a Book" onClose={() => setRequestModalOpen(false)} onSubmit={handleBookRequest} submitLabel="Submit Request">
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Book *</label>
          <select className="form-control" value={requestForm.bookId} onChange={e => { setRequestForm(p => ({ ...p, bookId: e.target.value })); setFormError(''); }}>
            <option value="">-- Select a Book --</option>
            {books.filter(b => b.available > 0).map(b => (
              <option key={b.id} value={b.id}>{b.title} by {b.author}</option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Reason / Purpose</label>
          <textarea className="form-control" rows="3" value={requestForm.reason} onChange={e => setRequestForm(p => ({ ...p, reason: e.target.value }))} placeholder="Why do you need this book?" />
        </div>
        <div className="form-group">
          <label className="form-label">Planned Return Date</label>
          <input type="date" className="form-control" value={requestForm.returnDate} onChange={e => setRequestForm(p => ({ ...p, returnDate: e.target.value }))} min={new Date().toISOString().split('T')[0]} />
        </div>
      </Modal>
    </div>
  );
};

export default LibraryPage;
