import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllBooks, addBook, getAllIssues, issueBook, returnBook } from '../services/libraryService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: 'Title' },
  { key: 'author', label: 'Author' },
  { key: 'isbn', label: 'ISBN' },
  {
    key: 'available', label: 'Available', render: (v) => (
      <span className={`badge badge-${v > 0 ? 'success' : 'danger'}`}>{v > 0 ? 'Yes' : 'No'}</span>
    )
  },
];

const ISSUE_COLUMNS = [
  { key: 'id', label: 'Issue ID' },
  { key: 'bookTitle', label: 'Book Title' },
  { key: 'studentName', label: 'Student Name' },
  { key: 'dueDate', label: 'Due Date' },
  { key: 'fineAmount', label: 'Fine (Rs.)' },
];

const EMPTY_FORM = { title: '', author: '', isbn: '', available: true };

const LibraryPage = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [view, setView] = useState('books'); // 'books' or 'issues'
  const [issues, setIssues] = useState([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [issueModalOpen, setIssueModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [issueForm, setIssueForm] = useState({ studentId: '', bookId: null });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchBooks = () => {
    setLoading(true);
    getAllBooks()
      .then((res) => setBooks(res.data || []))
      .catch(() => setError('Failed to load books.'))
      .finally(() => setLoading(false));
  };

  const fetchIssues = () => {
    setLoading(true);
    getAllIssues()
      .then((res) => setIssues(res.data || []))
      .catch(() => setError('Failed to load issued books.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (view === 'books') fetchBooks();
    else fetchIssues();
  }, [view]);

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
      await issueBook({ studentId: Number(issueForm.studentId), bookId: issueForm.bookId, issuedBy: 1 });
      setIssueModalOpen(false);
      fetchBooks();
    } catch (err) {
      setFormError(err.response?.data?.error || 'Failed to issue book.');
    } finally {
      setSaving(false);
    }
  };

  const handleReturn = async (issueId) => {
    if (window.confirm('Are you sure you want to mark this book as returned?')) {
      try {
        await returnBook(issueId, { returnedTo: 1 });
        fetchIssues();
      } catch (err) {
        alert(err.response?.data?.error || 'Failed to return book.');
      }
    }
  };

  const extendedBooksColumns = [
    ...COLUMNS,
    {
      key: 'actions', label: 'Actions', render: (_, book) => (
        book.available > 0 && (
          <button className="btn btn-secondary btn-sm" onClick={() => { setIssueForm({ studentId: '', bookId: book.id }); setFormError(''); setIssueModalOpen(true); }}>
            Issue Book
          </button>
        )
      )
    }
  ];

  const extendedIssuesColumns = [
    ...ISSUE_COLUMNS,
    {
      key: 'actions', label: 'Actions', render: (_, issue) => (
        <button className="btn btn-primary btn-sm" onClick={() => handleReturn(issue.id)}>Return</button>
      )
    }
  ];

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📖 Library</h1>
        <div className="page-actions">
          <button className={view === 'books' ? "btn btn-primary" : "btn btn-secondary"} onClick={() => setView('books')}>Catalog</button>
          <button className={view === 'issues' ? "btn btn-primary" : "btn btn-secondary"} onClick={() => setView('issues')}>Issued Books</button>
          {view === 'books' && (
            <button className="btn btn-primary" onClick={() => { setForm(EMPTY_FORM); setModalOpen(true); }}>+ Add Book</button>
          )}
        </div>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading data…</span></div>
      ) : view === 'books' ? (
        <DataTable columns={extendedBooksColumns} data={books} emptyMessage="No books in library." />
      ) : (
        <DataTable columns={extendedIssuesColumns} data={issues} emptyMessage="No issued books." />
      )}

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

      <Modal isOpen={issueModalOpen} title="Issue Book" onClose={() => setIssueModalOpen(false)} onSubmit={handleIssue} submitLabel={saving ? 'Issuing…' : 'Issue'}>
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        <div className="form-group">
          <label className="form-label">Student ID</label>
          <input type="number" className="form-control" value={issueForm.studentId} onChange={(e) => { setIssueForm(p => ({ ...p, studentId: e.target.value })); setFormError(''); }} />
        </div>
      </Modal>
    </div>
  );
};

export default LibraryPage;
