import React, { useEffect, useState } from 'react';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';
import { getAllBooks, addBook } from '../services/libraryService';

const COLUMNS = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: 'Title' },
  { key: 'author', label: 'Author' },
  { key: 'isbn', label: 'ISBN' },
  { key: 'available', label: 'Available', render: (v) => (
    <span className={`badge badge-${v ? 'success' : 'danger'}`}>{v ? 'Yes' : 'No'}</span>
  )},
];

const EMPTY_FORM = { title: '', author: '', isbn: '', available: true };

const LibraryPage = () => {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchBooks = () => {
    setLoading(true);
    getAllBooks()
      .then((res) => setBooks(res.data || []))
      .catch(() => setError('Failed to load books.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchBooks(); }, []);

  const handleFormChange = (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm((prev) => ({ ...prev, [e.target.name]: value }));
    setFormError('');
  };

  const handleAdd = async () => {
    if (!form.title || !form.author) { setFormError('Title and author are required.'); return; }
    setSaving(true);
    try {
      await addBook(form);
      setModalOpen(false);
      setForm(EMPTY_FORM);
      fetchBooks();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to add book.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">📖 Library</h1>
        <button className="btn btn-primary" onClick={() => { setForm(EMPTY_FORM); setModalOpen(true); }}>
          + Add Book
        </button>
      </div>

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {loading ? (
        <div className="loading-container"><div className="spinner" /><span>Loading books…</span></div>
      ) : (
        <DataTable columns={COLUMNS} data={books} emptyMessage="No books in library." />
      )}

      <Modal
        isOpen={modalOpen}
        title="Add Book"
        onClose={() => setModalOpen(false)}
        onSubmit={handleAdd}
        submitLabel={saving ? 'Saving…' : 'Save'}
      >
        {formError && <div className="alert alert-error" style={{ marginBottom: 12 }}>{formError}</div>}
        {[{ name: 'title', label: 'Title' }, { name: 'author', label: 'Author' }, { name: 'isbn', label: 'ISBN' }].map(({ name, label }) => (
          <div className="form-group" key={name}>
            <label className="form-label" htmlFor={`book-${name}`}>{label}</label>
            <input id={`book-${name}`} name={name} type="text" className="form-control" value={form[name]} onChange={handleFormChange} placeholder={`Enter ${label.toLowerCase()}`} />
          </div>
        ))}
        <div className="form-group">
          <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
            <input type="checkbox" name="available" checked={form.available} onChange={handleFormChange} />
            <span className="form-label" style={{ margin: 0 }}>Available</span>
          </label>
        </div>
      </Modal>
    </div>
  );
};

export default LibraryPage;
