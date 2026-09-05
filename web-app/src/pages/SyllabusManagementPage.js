import SessionManager from '../utils/SessionManager';
import React, { useState, useEffect, useCallback } from 'react';
import { getSyllabiBycourse, addSyllabus, deleteSyllabus, downloadSyllabus } from '../services/syllabusService';
import { getAllCourses } from '../services/courseService';
import { searchStudents, getStudentCourses } from '../services/studentService';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { toast } from '../components/Toast';
import { getErrorMessage, getSuccessRefId } from '../utils/error';
import { SkeletonCards } from '../components/Skeleton';

const getFileIcon = (path) => {
    if (!path) return '📄';
    const ext = path.split('.').pop().toLowerCase();
    if (ext === 'pdf') return '📕';
    if (ext.startsWith('doc')) return '📘';
    if (ext.startsWith('xls')) return '📊';
    if (ext.startsWith('ppt')) return '📽️';
    return '📄';
};

const SyllabusManagementPage = () => {
    const canUpload = SessionManager.hasPermission('CREATE_SYLLABUS');
    const canDelete = SessionManager.hasPermission('DELETE_SYLLABUS');
    const canManage = canUpload || canDelete;
    const userId = SessionManager.getUserId();

    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState('');
    const [syllabi, setSyllabi] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showAdd, setShowAdd] = useState(false);
    const [error, setError] = useState('');
    const [uploading, setUploading] = useState(false);
    const [uploadProgress, setUploadProgress] = useState(0);
    const [pendingDelete, setPendingDelete] = useState(null);
    const [deleting, setDeleting] = useState(false);

    // File upload state
    const [form, setForm] = useState({ title: '', version: '1.0', description: '', filePath: '' });
    const [selectedFile, setSelectedFile] = useState(null);

    useEffect(() => {
        const user = SessionManager.getUser() || {};
        const applyList = (list) => {
            setCourses(list);
            if (list.length > 0) setSelectedCourse(String(list[0].id));
        };
        if (!canManage && user.role === 'STUDENT' && user.username) {
            searchStudents(user.username).then(res => {
                const match = (res.data || []).find(s => s.username === user.username) || (res.data || [])[0];
                if (match) {
                    return getStudentCourses(match.id).then(cRes => {
                        const enrolled = cRes.data || [];
                        if (enrolled.length > 0) {
                            applyList(enrolled);
                            return;
                        }
                        throw new Error('no-enrolled');
                    });
                }
                throw new Error('no-student');
            }).catch(() => {
                getAllCourses(1, 500).then(res => {
                    const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
                    applyList(list);
                }).catch(err => setError(err.response?.data?.error || 'Courses could not be loaded.'));
            });
        } else {
            getAllCourses(1, 500).then(res => {
                const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
                applyList(list);
            }).catch(err => setError(err.response?.data?.error || 'Courses could not be loaded.'));
        }
    }, [canManage]);

    const fetchSyllabi = useCallback((signal) => {
        if (!selectedCourse) return Promise.resolve();
        setLoading(true);
        setError('');
        return getSyllabiBycourse(selectedCourse, signal)
            .then(res => setSyllabi(Array.isArray(res.data) ? res.data : []))
            .catch(err => {
                if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
                setError(err.response?.data?.error || 'Syllabus records could not be loaded.');
            })
            .finally(() => { if (!signal?.aborted) setLoading(false); });
    }, [selectedCourse]);

    useEffect(() => {
        const controller = new AbortController();
        fetchSyllabi(controller.signal);
        return () => controller.abort();
    }, [fetchSyllabi]);

    const handleFileChange = (e) => {
        if (e.target.files && e.target.files.length > 0) {
            setSelectedFile(e.target.files[0]);
            // Auto-fill title if empty
            if (!form.title) {
                setForm(prev => ({ ...prev, title: e.target.files[0].name.split('.')[0] }));
            }
        }
    };

    const handleAdd = async () => {
        if (selectedFile && selectedFile.size > 10 * 1024 * 1024) {
            toast.error('File is too large. Maximum size is 10 MB.');
            return;
        }
        setUploading(true);
        setUploadProgress(10);
        let fileData = null;
        let fileName = '';

        if (selectedFile) {
            fileName = selectedFile.name;
            try {
                fileData = await new Promise((resolve, reject) => {
                    const reader = new FileReader();
                    reader.readAsDataURL(selectedFile);
                    reader.onload = () => { setUploadProgress(60); resolve(reader.result); };
                    reader.onerror = error => reject(error);
                });
            } catch {
                setUploading(false);
                toast.error('Could not read the selected file.');
                return;
            }
        }

        try {
            setUploadProgress(80);
            const refId = getSuccessRefId();
            await addSyllabus({
                ...form,
                courseId: parseInt(selectedCourse),
                uploadedBy: userId,
                fileName,
                fileData
            });
            setUploadProgress(100);
            setShowAdd(false);
            setForm({ title: '', version: '1.0', description: '', filePath: '' });
            setSelectedFile(null);
            toast.success('Syllabus published.', { refId });
            fetchSyllabi();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Upload failed. Check if the file is too large.');
            toast.error(message, { refId, details: { status } });
        } finally { setUploading(false); setUploadProgress(0); }
    };

    const confirmDelete = async () => {
        if (!pendingDelete) return;
        setDeleting(true);
        try {
            await deleteSyllabus(pendingDelete.id);
            setPendingDelete(null);
            toast.success('Syllabus deleted.', { refId: getSuccessRefId() });
            fetchSyllabi();
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not delete this syllabus.');
            toast.error(message, { refId });
        } finally { setDeleting(false); }
    };

    const handleDownload = async (s) => {
        if (s.filePath && (s.filePath.startsWith('http') || s.filePath.startsWith('www'))) {
            window.open(s.filePath, '_blank');
            return;
        }
        try {
            const res = await downloadSyllabus(s.id);
            const url = window.URL.createObjectURL(new Blob([res.data]));
            const link = document.createElement('a');
            link.href = url;
            const fileName = s.filePath ? s.filePath.split('/').pop().split('\\').pop() : `syllabus_${s.id}`;
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);
            toast.success('Download started.', { refId: getSuccessRefId() });
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not download this file.');
            toast.error(message, { refId });
        }
    };

    const selCourseObj = courses.find(c => String(c.id) === selectedCourse);

    return (
        <div className="page-container" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                        <h1 className="page-title">📋 Curriculum & Syllabus</h1>
                        <p className="page-subtitle">Centralized repository for academic frameworks, course maps, and learning objectives</p>
                    </div>
                    {canUpload && (
                        <button className="btn btn-primary" onClick={() => setShowAdd(true)} disabled={!selectedCourse}>
                            + Upload Framework
                        </button>
                    )}
                </div>
            </div>

            {error && <div className="alert alert-error" role="alert">{error}</div>}
            {canManage && !selectedCourse && !error && (
                <div className="alert" role="status">Create or load a course before uploading a syllabus.</div>
            )}

            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 3fr) 1fr', gap: '30px' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '25px' }}>
                    {/* Course Header Stat Card */}
                    <div className="stat-card" style={{ background: 'white', border: 'none', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)', display: 'flex', gap: '20px', alignItems: 'center' }}>
                        <div style={{ width: '60px', height: '60px', background: '#3b82f6', borderRadius: '15px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.5rem' }}>📚</div>
                        <div style={{ flex: 1 }}>
                            <div style={{ fontSize: '0.75rem', color: '#64748b', textTransform: 'uppercase', letterSpacing: '1px' }}>Active Curriculum for</div>
                            <h2 style={{ margin: 0, fontSize: '1.4rem' }}>{selCourseObj?.name || 'Academic Course'}</h2>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                            <div style={{ fontSize: '0.75rem', color: '#64748b' }}>TOTAL MANUALS</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{syllabi.length}</div>
                        </div>
                    </div>

                    {loading ? (
                        <SkeletonCards count={4} />
                    ) : error ? (
                        <div className="retry-bar" role="alert">
                            <span>{error}</span>
                            <button className="btn btn-secondary btn-sm" onClick={() => fetchSyllabi()}>Retry</button>
                        </div>
                    ) : (
                        <div className="card-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
                            {syllabi.map(s => (
                                <div key={s.id} className="stat-card" style={{ display: 'flex', gap: '15px', alignItems: 'flex-start', borderLeft: '6px solid #3b82f6' }}>
                                    <div style={{ fontSize: '2rem' }}>{getFileIcon(s.filePath)}</div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                            <h4 style={{ margin: 0, fontSize: '1rem' }}>{s.title}</h4>
                                            <span className="badge" style={{ background: '#eff6ff', color: '#3b82f6', fontSize: '0.65rem' }}>v{s.version}</span>
                                        </div>
                                        <p style={{ fontSize: '0.8rem', color: '#64748b', marginBottom: '15px', lineHeight: '1.5' }}>{s.description || 'Institutional academic guide for ' + (selCourseObj?.name || 'course')}</p>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <span style={{ fontSize: '0.7rem', color: '#94a3b8' }}>Uploaded: {s.uploadedAt?.split('T')[0]}</span>
                                            <div style={{ display: 'flex', gap: '10px' }}>
                                                {canDelete && <button onClick={() => setPendingDelete(s)} style={{ background: 'none', border: 'none', color: '#ef4444', fontSize: '0.8rem', cursor: 'pointer' }}>Delete</button>}
                                                <button onClick={() => handleDownload(s)} style={{ background: 'none', border: 'none', fontSize: '0.9rem', color: '#3b82f6', fontWeight: 'bold', textDecoration: 'none', cursor: 'pointer' }}>Download ⬇</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                            {syllabi.length === 0 && (
                                <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '100px', color: '#94a3b8' }}>
                                    <div style={{ fontSize: '3rem', marginBottom: '15px' }}>📁</div>
                                    <p>No curriculum frameworks found for this unit.</p>
                                </div>
                            )}
                        </div>
                    )}
                </div>

                {/* Sidebar Filter */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div className="stat-card">
                        <h4 style={{ marginBottom: '15px' }}>Course Select</h4>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            {courses.map(c => (
                                <button
                                    key={c.id}
                                    onClick={() => setSelectedCourse(String(c.id))}
                                    style={{
                                        textAlign: 'left', padding: '12px 15px', borderRadius: '10px',
                                        background: selectedCourse === String(c.id) ? '#3b82f6' : 'transparent',
                                        color: selectedCourse === String(c.id) ? 'white' : '#475569',
                                        border: '1px solid ' + (selectedCourse === String(c.id) ? '#3b82f6' : '#e2e8f0'),
                                        cursor: 'pointer', fontSize: '0.85rem', fontWeight: selectedCourse === String(c.id) ? '600' : '400',
                                        transition: 'all 0.2s'
                                    }}
                                >
                                    {c.name}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="stat-card" style={{ background: '#f0f9ff', borderColor: '#bae6fd' }}>
                        <h4 style={{ color: '#0369a1', marginBottom: '10px' }}>Integrity Tip</h4>
                        <p style={{ fontSize: '0.75rem', color: '#0c4a6e', lineHeight: '1.5' }}>Ensure versions are incremented correctly (e.g. 1.0 &rarr; 1.1) to maintain historical curriculum accuracy.</p>
                    </div>
                </div>
            </div>

            <Modal
                isOpen={showAdd}
                title="Publish Syllabus"
                onClose={() => setShowAdd(false)}
                onSubmit={handleAdd}
                submitLabel={uploading ? `Uploading… ${uploadProgress}%` : 'Authorize Publication'}
                submitting={uploading}
                isDirty={Boolean(form.title || form.description || selectedFile)}
                size="large"
            >
                <p style={{ color: '#64748b', fontSize: '0.85rem', marginBottom: '14px' }}>Unit: <strong>{selCourseObj?.name}</strong></p>
                <form onSubmit={(e) => { e.preventDefault(); handleAdd(); }} className="form-grid">
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Framework Title *</label>
                        <input required className="form-control" type="text" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} placeholder="e.g. Data Structures 2025 Revised" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Iteration / Version</label>
                        <input className="form-control" type="text" value={form.version} onChange={e => setForm({ ...form, version: e.target.value })} placeholder="e.g. 1.1" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Framework File (PDF/DOC, max 10 MB) *</label>
                        <input required className="form-control" type="file" accept=".pdf,.doc,.docx" onChange={handleFileChange} />
                        {selectedFile && <span className="field-hint">{selectedFile.name} — {(selectedFile.size / 1024 / 1024).toFixed(2)} MB</span>}
                    </div>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Abstract / Scope</label>
                        <textarea className="form-control" rows="4" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} placeholder="Outline the modules and expected outcomes..."></textarea>
                    </div>
                    {uploading && (
                        <div style={{ gridColumn: '1 / -1' }} role="status" aria-label={`Upload progress ${uploadProgress} percent`}>
                            <div style={{ height: '8px', background: '#edf0f5', borderRadius: '4px', overflow: 'hidden' }}>
                                <div style={{ width: `${uploadProgress}%`, height: '100%', background: 'var(--primary)' }} />
                            </div>
                        </div>
                    )}
                </form>
            </Modal>
            <ConfirmDialog
                isOpen={Boolean(pendingDelete)}
                title={`Delete "${pendingDelete?.title}"?`}
                message="This syllabus version will be removed from institutional records. This cannot be undone."
                confirmLabel="Delete"
                loading={deleting}
                onConfirm={confirmDelete}
                onCancel={() => { if (!deleting) setPendingDelete(null); }}
            />
        </div>
    );
};

export default SyllabusManagementPage;
