import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { useToast } from '../components/Toast';
import { exportToCSV } from '../utils/exportUtils';
import { getAllBooks, addBook, updateBook, deleteBook, getAllIssues, issueBook, returnBook, getIssuesByStudent, requestBook, getBookRequests, approveBookRequest, rejectBookRequest, sendReminders } from '../services/libraryService';
import { getAllStudents, searchStudents } from '../services/studentService';
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
  { key: 'studentName', label: 'Student Name', render: (v, r) => (
    <span>
      {v || 'N/A'}
      {(r.enrollmentId || r.enrollmentNumber || r.username) && <span style={{ fontWeight: 'bold', fontFamily: 'monospace', color: '#2d3748', marginLeft: '6px', fontSize: '0.85rem' }}>({r.enrollmentId || r.enrollmentNumber || r.username})</span>}
    </span>
  )},
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

const EMPTY_FORM = { title: '', author: '', isbn: '', quantity: 1, available: true };

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
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [issueForm, setIssueForm] = useState({ enrollmentId: '', bookId: null });
  const [requestForm, setRequestForm] = useState({ bookId: '', reason: '', returnDate: '' });
  const [rejectForm, setRejectForm] = useState({ requestId: null, remarks: '' });
  const [students, setStudents] = useState([]);
  const [requests, setRequests] = useState([]);
  const [filteredStudents, setFilteredStudents] = useState([]);
  const [studentSearch, setStudentSearch] = useState('');
  const [studentSearchLoading, setStudentSearchLoading] = useState(false);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [editingBookId, setEditingBookId] = useState(null);
  // Pagination state
  const [bookPage, setBookPage] = useState(0);
  const [bookTotalPages, setBookTotalPages] = useState(0);
  const [issuePage, setIssuePage] = useState(0);
  const [issueTotalPages, setIssueTotalPages] = useState(0);
  const [myIssuePage, setMyIssuePage] = useState(0);
  const [myIssueTotalPages, setMyIssueTotalPages] = useState(0);
  const PAGE_SIZE = 20;

  // Toast
  const { success: toastSuccess, error: toastError } = useToast();
  // ConfirmDialog state
  const [confirmDialog, setConfirmDialog] = useState({ open: false, title: '', message: '', onConfirm: null });

  const user = SessionManager.getUser() || {};
  const isAdmin = SessionManager.hasRole('ADMIN');
  const isStudent = user.role === 'STUDENT';

  const fetchBooks = React.useCallback(() => {
    setLoading(true);
    getAllBooks(bookPage, PAGE_SIZE)
      .then((res) => {
        setBooks(res.data?.content || res.data || []);
        setBookTotalPages(res.data?.totalPages || 0);
      })
      .catch(() => setError('Failed to load books.'))
      .finally(() => setLoading(false));
  }, [bookPage]);

  const fetchIssues = React.useCallback(() => {
    setLoading(true);
    getAllIssues(issuePage, PAGE_SIZE)
      .then((res) => {
        setIssues(res.data?.content || res.data || []);
        setIssueTotalPages(res.data?.totalPages || 0);
      })
      .catch(() => setError('Failed to load issued books.'))
      .finally(() => setLoading(false));
  }, [issuePage]);

  const fetchMyIssues = React.useCallback(() => {
    if (!user.username) return;
    setLoading(true);
    getIssuesByStudent(user.username, myIssuePage, PAGE_SIZE)
      .then(res => {
        setMyIssues(res.data?.content || res.data || []);
        setMyIssueTotalPages(res.data?.totalPages || 0);
      })
      .catch(() => setError('Failed to load your issues.'))
      .finally(() => setLoading(false));
  }, [user.username, myIssuePage]);

  const fetchRequests = React.useCallback(() => {
    getBookRequests()
      .then(res => setRequests(res.data || []))
      .catch(() => setError('Failed to load requests.'));
  }, []);

useEffect(() => {
    const query = studentSearch.trim();
    if (!query) {
      setFilteredStudents([]);
      setStudentSearchLoading(false);
      return undefined;
    }

    setStudentSearchLoading(true);
    let active = true;
    const timeoutId = setTimeout(() => {
      searchStudents(query)
        .then(res => { if (active) setFilteredStudents(res.data || []); })
        .catch(() => { if (active) setFilteredStudents([]); })
        .finally(() => { if (active) setStudentSearchLoading(false); });

    }, 300);

    return () => {
      active = false;
      clearTimeout(timeoutId);
    };
  }, [studentSearch, students]);

  useEffect(() => {
    if (view === 'books') { setBookPage(0); fetchBooks(); }
    else if (view === 'issues') { setIssuePage(0); fetchIssues(); }
    else if (view === 'my') { setMyIssuePage(0); fetchMyIssues(); }
    else if (view === 'requests') { fetchBooks(); fetchRequests(); }
  }, [view, fetchBooks, fetchIssues, fetchMyIssues, fetchRequests]);

useEffect(() => {
    getAllStudents().then(res => setStudents(res.data || [])).catch(() => {});
  }, []);

  // Reset pagination when search changes
  useEffect(() => {
    setBookPage(0);
}, [searchQuery]);

  const showConfirm = (title, message, onConfirm) => {
    setConfirmDialog({ open: true, title, message, onConfirm });
  };

const handleFormChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((prev) => ({ ...prev, [e.target.name]: value }));
    setFormError('');
  };

  const handleIssue = async () => {
    if (!issueForm.enrollmentId) { setFormError('Student enrollment is required.'); return; }
    setSaving(true);
    try {
      await issueBook({ enrollmentId: issueForm.enrollmentId, bookId: issueForm.bookId, issuedBy: user.id || 1 });
      setIssueModalOpen(false);
      setIssueForm({ enrollmentId: '', bookId: null });
      fetchBooks();
      if (view === 'issues') fetchIssues();
    } catch (err) {
      setFormError(err.response?.data?.error || 'Failed to issue book.');
    } finally {
      setSaving(false);
    }
  };

  const handleReturn = (issueId) => {
    showConfirm('Return Book', 'Mark this book as returned?', async () => {
      try {
        await returnBook(issueId, { returnedTo: user.id || 1 });
        toastSuccess('Book returned successfully');
        if (view === 'issues') fetchIssues();
        else fetchMyIssues();
      } catch (err) {
        toastError(err.response?.data?.error || 'Failed to return book.');
      }
    });
  };

  const handleSendReminders = () => {
    setSaving(true);
    sendReminders()
      .then(() => {
        toastSuccess('Reminders sent successfully to students with overdue books.');
      })
      .catch(err => {
        toastError(err.response?.data?.error || 'Failed to send reminders.');
      })
      .finally(() => setSaving(false));
  };

  const handleEditBook = (book) => {
    setEditingBookId(book.id);
    setForm({ title: book.title, author: book.author, isbn: book.isbn, quantity: book.quantity || 1, available: book.available || 0 });
    setModalOpen(true);
  };

  const handleDeleteBook = (bookId) => {
    showConfirm('Delete Book', 'Delete this book permanently? This cannot be undone.', async () => {
      try {
        await deleteBook(bookId);
        toastSuccess('Book deleted successfully');
        fetchBooks();
      } catch (err) {
        toastError(err.response?.data?.error || 'Failed to delete book.');
      }
    });
  };

  const handleFormSubmit = async () => {
    if (!form.title || !form.author) { setFormError('Title and author are required.'); return; }
    if (form.isbn) {
      const isbn = form.isbn.replace(/[-\s]/g, '');
      if (!/^(?:\d{9}[\dXx]|\d{13})$/.test(isbn)) {
        setFormError('ISBN must contain 10 or 13 valid characters.');
        return;
      }
    }
    const quantity = parseInt(form.quantity, 10) || 1;
    const available = parseInt(form.available, 10) || 0;
    if (available > quantity) { setFormError('Available copies cannot exceed total quantity.'); return; }
    setSaving(true);
    try {
      if (editingBookId) {
        await updateBook(editingBookId, { ...form, quantity, available });
      } else {
        await addBook({ ...form, quantity, available });
      }
      setModalOpen(false);
      setForm(EMPTY_FORM);
      setEditingBookId(null);
      fetchBooks();
    } catch (err) {
      setFormError(err.response?.data?.error || 'Failed to save book.');
    } finally {
      setSaving(false);
    }
  };

  const handleBookRequest = async () => {
    if (!requestForm.bookId) { setFormError('Please select a book.'); return; }
    setSaving(true);
    try {
      await requestBook({
        enrollmentId: user.username,
        bookId: requestForm.bookId,
        reason: requestForm.reason,
        loanPeriodDays: 14,
      });
      setRequestModalOpen(false);
      setRequestForm({ bookId: '', reason: '', returnDate: '' });
      fetchRequests();
      toastSuccess('Book request submitted! The librarian will review your request.');
    } catch (err) {
      setFormError(err.response?.data?.error || 'Failed to submit request.');
    } finally { setSaving(false); }
  };

  const handleApproveRequest = async (reqId) => {
    try {
      await approveBookRequest(reqId);
      fetchRequests();
      fetchBooks();
      toastSuccess('Request approved and book issued');
    } catch (err) {
      toastError(err.response?.data?.error || 'Failed to approve request.');
    }
  };

  const openRejectModal = (requestId) => {
    setRejectForm({ requestId, remarks: '' });
    setRejectModalOpen(true);
  };

  const handleRejectRequest = async () => {
    if (!rejectForm.requestId) return;
    setSaving(true);
    try {
      await rejectBookRequest(rejectForm.requestId, rejectForm.remarks);
      setRejectModalOpen(false);
      fetchRequests();
      toastSuccess('Request rejected');
    } catch (err) {
      toastError(err.response?.data?.error || 'Failed to reject request.');
    } finally { setSaving(false); }
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
        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
          {book.available > 0 && isAdmin && (
            <button className="btn btn-secondary btn-sm" onClick={() => { setIssueForm({ enrollmentId: '', bookId: book.id }); setFormError(''); setIssueModalOpen(true); }}>
              Issue
            </button>
          )}
          {book.available > 0 && isStudent && (
            <button className="btn btn-primary btn-sm" onClick={() => { setRequestForm({ bookId: book.id, reason: '', returnDate: '' }); setFormError(''); setRequestModalOpen(true); }}>
              🙋 Request
            </button>
          )}
          {book.available === 0 && isStudent && (
            <button className="btn btn-secondary btn-sm" onClick={() => { setRequestForm({ bookId: book.id, reason: 'Reserved - will wait for availability', returnDate: '' }); setFormError(''); setRequestModalOpen(true); }}>
              ⏳ Reserve
            </button>
          )}
          {book.available === 0 && !isStudent && (
            <span style={{ fontSize: '0.78rem', color: '#a0aec0' }}>Unavailable</span>
          )}
          {isAdmin && (
            <>
              <button className="btn btn-secondary btn-sm" onClick={() => handleEditBook(book)}>✏ Edit</button>
              <button className="btn btn-danger btn-sm" onClick={() => handleDeleteBook(book.id)}>🗑 Delete</button>
            </>
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
              exportToCSV(['ID', 'Title', 'Author', 'ISBN', 'Available'], filteredBooks.map(b => [b.id, b.title, b.author, b.isbn, b.available]), 'library_catalog_export');
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
          {bookTotalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', marginTop: '16px' }}>
              <button className="btn btn-secondary btn-sm" onClick={() => setBookPage(p => Math.max(0, p - 1))} disabled={bookPage === 0}>‹ Prev</button>
              <span>Page {bookPage + 1} of {bookTotalPages}</span>
              <button className="btn btn-secondary btn-sm" onClick={() => setBookPage(p => Math.min(bookTotalPages - 1, p + 1))} disabled={bookPage >= bookTotalPages - 1}>Next ›</button>
            </div>
          )}
        </>
      ) : view === 'issues' ? (
        <>
          <DataTable columns={extendedIssuesColumns} data={issues} emptyMessage="No issued books." />
          {issueTotalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', marginTop: '16px' }}>
              <button className="btn btn-secondary btn-sm" onClick={() => setIssuePage(p => Math.max(0, p - 1))} disabled={issuePage === 0}>‹ Prev</button>
              <span>Page {issuePage + 1} of {issueTotalPages}</span>
              <button className="btn btn-secondary btn-sm" onClick={() => setIssuePage(p => Math.min(issueTotalPages - 1, p + 1))} disabled={issuePage >= issueTotalPages - 1}>Next ›</button>
            </div>
          )}
        </>
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
          {myIssueTotalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', marginTop: '16px' }}>
              <button className="btn btn-secondary btn-sm" onClick={() => setMyIssuePage(p => Math.max(0, p - 1))} disabled={myIssuePage === 0}>‹ Prev</button>
              <span>Page {myIssuePage + 1} of {myIssueTotalPages}</span>
              <button className="btn btn-secondary btn-sm" onClick={() => setMyIssuePage(p => Math.min(myIssueTotalPages - 1, p + 1))} disabled={myIssuePage >= myIssueTotalPages - 1}>Next ›</button>
            </div>
          )}
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
                      <button className="btn btn-secondary btn-sm" onClick={() => openRejectModal(req.id)}>✗ Reject</button>
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
      <Modal isOpen={modalOpen} title={editingBookId ? 'Edit Book' : 'Add Book'} onClose={() => { setModalOpen(false); setEditingBookId(null); setForm(EMPTY_FORM); }} onSubmit={handleFormSubmit} submitLabel={saving ? 'Saving…' : 'Save'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {[{ name: 'title', label: 'Title' }, { name: 'author', label: 'Author' }, { name: 'isbn', label: 'ISBN' }].map(({ name, label }) => (
          <div className="form-group" key={name}>
            <label className="form-label">{label}</label>
            <input name={name} type="text" required={name === 'title' || name === 'author'} inputMode={name === 'isbn' ? 'numeric' : undefined} className="form-control" value={form[name]} onChange={handleFormChange} />
          </div>
        ))}
        <div className="form-group">
          <label className="form-label">Quantity (Total Copies)</label>
          <input name="quantity" type="number" min="1" className="form-control" value={form.quantity} onChange={handleFormChange} />
        </div>
        <div className="form-group">
          <label className="form-label">Available Copies</label>
          <input name="available" type="number" min="0" className="form-control" value={form.available} onChange={handleFormChange} />
        </div>
      </Modal>

      <Modal isOpen={issueModalOpen} title="Issue Book to Student" onClose={() => { setIssueModalOpen(false); setIssueForm({ enrollmentId: '', bookId: null }); setStudentSearch(''); setFilteredStudents([]); }} onSubmit={handleIssue} submitLabel={saving ? 'Issuing…' : 'Issue'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Search Student</label>
          <input type="text" className="form-control" placeholder="Search by name or enrollment…" value={studentSearch} onChange={(e) => { setStudentSearch(e.target.value); setFormError(''); }} />
          {studentSearchLoading && <div style={{ fontSize: '0.75rem', color: '#718096' }}>Searching…</div>}
          {filteredStudents.length > 0 && (
            <select className="form-control" style={{ marginTop: '8px' }} value={issueForm.enrollmentId} onChange={(e) => { setIssueForm(p => ({ ...p, enrollmentId: e.target.value })); setFormError(''); }}>
              <option value="">-- Select Student --</option>
              {filteredStudents.map(s => {
                const enrollmentId = s.enrollmentId || s.username;
                return enrollmentId ? <option key={s.id} value={enrollmentId}>{s.name} ({enrollmentId})</option> : null;
              })}
            </select>
          )}
        </div>
        <div style={{ fontSize: '0.82rem', color: '#718096', marginTop: '8px' }}>
          Book ID: <strong>{issueForm.bookId}</strong> — {books.find(b => b.id === issueForm.bookId)?.title}
        </div>
      </Modal>

      <Modal isOpen={requestModalOpen} title="Request a Book" onClose={() => { setRequestModalOpen(false); setRequestForm({ bookId: '', reason: '', returnDate: '' }); }} onSubmit={handleBookRequest} submitLabel="Submit Request">
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

      <Modal isOpen={rejectModalOpen} title="Reject Book Request" onClose={() => { setRejectModalOpen(false); setRejectForm({ requestId: null, remarks: '' }); }} onSubmit={handleRejectRequest} submitLabel={saving ? 'Rejecting…' : 'Reject'}>
        <div className="form-group">
          <label className="form-label">Reason for Rejection (optional)</label>
          <textarea className="form-control" rows="3" value={rejectForm.remarks} onChange={e => setRejectForm(p => ({ ...p, remarks: e.target.value }))} placeholder="Enter reason for rejecting this request…" />
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={confirmDialog.open}
        title={confirmDialog.title}
        message={confirmDialog.message}
        confirmLabel="Confirm"
        onConfirm={confirmDialog.onConfirm}
        onCancel={() => setConfirmDialog({ open: false, title: '', message: '', onConfirm: null })}
        destructive
      />
    </div>
  );
};

export default LibraryPage;
