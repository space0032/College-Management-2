import React, { useState, useEffect, useCallback } from 'react';
import { getSyllabiBycourse, getAllSyllabi, addSyllabus, deleteSyllabus } from '../services/syllabusService';
import { getCourses } from '../services/courseService';

const getFileIcon = (path) => {
    if (!path) return '📄';
    const ext = path.split('.').pop().toLowerCase();
    if (ext === 'pdf') return '📕';
    if (ext.startsWith('doc')) return '📘';
    if (ext.startsWith('xls')) return '📊';
    if (ext.startsWith('ppt')) return '📽️';
    if (['jpg', 'png', 'jpeg'].includes(ext)) return '🖼️';
    if (['zip', 'rar'].includes(ext)) return '📦';
    return '📄';
};

const SyllabusManagementPage = () => {
    const user = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();
    const isAdmin = user.role === 'ADMIN';
    const isFaculty = user.role === 'FACULTY';
    const canManage = isAdmin || isFaculty;

    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState('');
    const [syllabi, setSyllabi] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showAdd, setShowAdd] = useState(false);
    const [form, setForm] = useState({ title: '', version: '1.0', description: '', filePath: '' });
    const [error, setError] = useState(null);

    useEffect(() => {
        getCourses().then(res => {
            const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
            setCourses(list);
            if (list.length > 0) setSelectedCourse(String(list[0].id));
        }).catch(() => { });
    }, []);

    const fetchSyllabi = useCallback(() => {
        if (!selectedCourse) return;
        setLoading(true);
        setError(null);
        getSyllabiBycourse(selectedCourse)
            .then(res => setSyllabi(Array.isArray(res.data) ? res.data : []))
            .catch(() => setError('Failed to load syllabi.'))
            .finally(() => setLoading(false));
    }, [selectedCourse]);

    useEffect(() => { fetchSyllabi(); }, [fetchSyllabi]);

    const handleAdd = async (e) => {
        e.preventDefault();
        try {
            await addSyllabus({
                courseId: parseInt(selectedCourse),
                title: form.title,
                version: form.version,
                description: form.description,
                filePath: form.filePath,
                uploadedBy: user.id
            });
            setShowAdd(false);
            setForm({ title: '', version: '1.0', description: '', filePath: '' });
            fetchSyllabi();
        } catch (err) {
            alert('Failed to add syllabus.');
        }
    };

    const handleDelete = async (id, title) => {
        if (!window.confirm(`Delete syllabus "${title}"?`)) return;
        try {
            await deleteSyllabus(id);
            fetchSyllabi();
        } catch {
            alert('Failed to delete syllabus.');
        }
    };

    const selectedCourseName = courses.find(c => String(c.id) === selectedCourse)?.name || '';

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>📋 Syllabus Management</h2>
                    <p className="text-muted">Browse and manage course syllabi.</p>
                </div>
                {canManage && (
                    <button className="btn btn-primary" onClick={() => setShowAdd(true)} disabled={!selectedCourse}>
                        + Upload Syllabus
                    </button>
                )}
            </div>

            {/* Course Filter */}
            <div style={{ marginBottom: '20px' }}>
                <label style={{ fontWeight: 500, marginRight: '10px' }}>Course:</label>
                <select
                    value={selectedCourse}
                    onChange={e => setSelectedCourse(e.target.value)}
                    style={{ padding: '8px 14px', borderRadius: '6px', border: '1px solid #ddd', minWidth: '250px' }}
                >
                    <option value="">-- Select a Course --</option>
                    {courses.map(c => (
                        <option key={c.id} value={String(c.id)}>{c.name || c.courseName}</option>
                    ))}
                </select>
            </div>

            {error && <div style={{ color: '#e53e3e', marginBottom: '15px' }}>{error}</div>}

            {loading ? (
                <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>Loading syllabi...</div>
            ) : (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Title</th>
                                <th>Version</th>
                                <th>Description</th>
                                <th>Uploaded By</th>
                                <th>Date</th>
                                {canManage && <th>Actions</th>}
                            </tr>
                        </thead>
                        <tbody>
                            {syllabi.length === 0 ? (
                                <tr>
                                    <td colSpan={canManage ? 6 : 5} style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                                        {selectedCourse
                                            ? `No syllabi uploaded for ${selectedCourseName} yet.`
                                            : 'Select a course to view syllabi.'}
                                    </td>
                                </tr>
                            ) : (
                                syllabi.map(s => (
                                    <tr key={s.id}>
                                        <td>
                                            <a
                                                href={s.filePath || '#'}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                style={{ color: '#3b82f6', textDecoration: 'none', fontWeight: 500 }}
                                            >
                                                {getFileIcon(s.filePath)} {s.title}
                                            </a>
                                        </td>
                                        <td><span className="status-badge" style={{ background: '#e3f2fd', color: '#1565c0' }}>v{s.version}</span></td>
                                        <td style={{ maxWidth: '280px', color: '#555' }}>{s.description || '—'}</td>
                                        <td>{s.uploaderName || '—'}</td>
                                        <td style={{ fontSize: '0.85rem', color: '#718096' }}>
                                            {s.uploadedAt ? s.uploadedAt.split('T')[0] : '—'}
                                        </td>
                                        {canManage && (
                                            <td>
                                                <button
                                                    className="btn"
                                                    style={{ padding: '4px 10px', fontSize: '0.8rem', background: '#fed7d7', color: '#c53030', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                                                    onClick={() => handleDelete(s.id, s.title)}
                                                >Delete</button>
                                            </td>
                                        )}
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Add Modal */}
            {showAdd && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '480px' }}>
                        <div className="modal-header">
                            <h2>📋 Add Syllabus — {selectedCourseName}</h2>
                            <button className="modal-close" onClick={() => setShowAdd(false)}>×</button>
                        </div>
                        <form onSubmit={handleAdd} style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '14px' }}>
                            <div className="form-group">
                                <label>Title *</label>
                                <input required value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} placeholder="e.g., Data Structures Syllabus 2025" />
                            </div>
                            <div className="form-group">
                                <label>Version</label>
                                <input value={form.version} onChange={e => setForm(f => ({ ...f, version: e.target.value }))} placeholder="e.g., 1.0, 2.1" />
                            </div>
                            <div className="form-group">
                                <label>Description</label>
                                <textarea
                                    value={form.description}
                                    onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                                    rows={3}
                                    placeholder="Brief description of this syllabus version..."
                                />
                            </div>
                            <div className="form-group">
                                <label>File URL *</label>
                                <input required value={form.filePath} onChange={e => setForm(f => ({ ...f, filePath: e.target.value }))} placeholder="https://drive.google.com/... or /path/to/file.pdf" />
                                <small style={{ color: '#718096' }}>Paste a link to a Google Drive PDF, URL, or local path</small>
                            </div>
                            <div style={{ display: 'flex', gap: '10px', marginTop: '8px' }}>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowAdd(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" style={{ flex: 2 }}>Upload Syllabus</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default SyllabusManagementPage;
