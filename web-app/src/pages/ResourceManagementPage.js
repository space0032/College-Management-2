import React, { useState, useEffect, useMemo } from 'react';
import { getResources, getResourceCategories, addResource, deleteResource, incrementDownload } from '../services/resourceService';
import { getAllCourses } from '../services/courseService';
import { searchStudents, getStudentCourses } from '../services/studentService';
import SessionManager from '../utils/SessionManager';
import Modal from '../components/Modal';
import ConfirmDialog from '../components/ConfirmDialog';
import { toast } from '../components/Toast';
import { getErrorMessage, getSuccessRefId } from '../utils/error';
import { SkeletonTable } from '../components/Skeleton';

const ResourceManagementPage = () => {
    const [resources, setResources] = useState([]);
    const [categories, setCategories] = useState([]);
    const [courses, setCourses] = useState([]);

    const [showModal, setShowModal] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [saving, setSaving] = useState(false);
    const [pendingDelete, setPendingDelete] = useState(null);
    const [deleting, setDeleting] = useState(false);

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        courseId: '',
        categoryId: '',
        filePath: '',
        fileType: 'pdf',
        fileSize: 104858,
        isPublic: true
    });
    const [sizeError, setSizeError] = useState('');
    const [sizeMb, setSizeMb] = useState('0.1');
    const [enrolledIds, setEnrolledIds] = useState(null);

    const currentUser = SessionManager.getUser() || {};
    const isStudent = currentUser.role === 'STUDENT';

    useEffect(() => {
        if (isStudent && currentUser.username) {
            searchStudents(currentUser.username).then(res => {
                const match = (res.data || []).find(s => s.username === currentUser.username) || (res.data || [])[0];
                if (match) {
                    return getStudentCourses(match.id).then(cRes => {
                        const ids = new Set((cRes.data || []).map(c => String(c.id)));
                        if (ids.size > 0) setEnrolledIds(ids);
                    });
                }
            }).catch(() => {});
        }
    }, [isStudent, currentUser.username]);

    const visibleResources = useMemo(() => {
        if (!isStudent || !enrolledIds) return resources;
        return resources.filter(r => !r.courseId || enrolledIds.has(String(r.courseId)));
    }, [resources, enrolledIds, isStudent]);

    const [highlightId, setHighlightId] = useState(null);
    const isDirty = Boolean(formData.title || formData.description || formData.filePath);

    useEffect(() => {
        const controller = new AbortController();
        fetchData(controller.signal);
        return () => controller.abort();
    }, []);

    const fetchData = async (signal) => {
        try {
            setLoading(true);
            setError(null);
            const [resRes, catRes, crsRes] = await Promise.all([
                getResources(null, signal),
                getResourceCategories(signal),
                getAllCourses(1, 500)
            ]);
            if (signal?.aborted) return;
            setResources(resRes.data || []);
            setCategories(catRes.data || []);
            setCourses(crsRes.data || []);

            if (catRes.data && catRes.data.length > 0) {
                setFormData(prev => (prev.categoryId ? prev : { ...prev, categoryId: catRes.data[0].id }));
            }
        } catch (err) {
            if (signal?.aborted || err?.code === 'ERR_CANCELED') return;
            setError(err?.response?.data?.error || 'Failed to load resources data.');
        } finally {
            if (!signal?.aborted) setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async () => {
        // Validate the exact typed MB value so 0.05 is rejected instead
        // of displaying as rounded 0.1 while the error persists.
        const mb = parseFloat(sizeMb);
        const sizeBytes = Number.isFinite(mb) ? Math.round(mb * 1048576) : NaN;
        if (!Number.isFinite(sizeBytes) || sizeBytes < 104858) {
            setSizeError('File size must be at least 0.1 MB.');
            return;
        }
        setSizeError('');
        setSaving(true);
        try {
            const formattedData = {
                ...formData,
                courseId: formData.courseId ? parseInt(formData.courseId) : null,
                categoryId: parseInt(formData.categoryId),
                fileSize: sizeBytes,
                uploadedBy: currentUser.id
            };
            const res = await addResource(formattedData);
            const newId = res?.data?.id;
            setShowModal(false);
            setSizeMb('0.1');
            setFormData(prev => ({ title: '', description: '', courseId: '', categoryId: prev.categoryId, filePath: '', fileType: 'pdf', fileSize: 104858, isPublic: true }));
            toast.success('Resource uploaded.', { refId: getSuccessRefId() });
            await fetchData();
            if (newId) {
                setHighlightId(newId);
                setTimeout(() => setHighlightId(null), 3000);
            }
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not upload this resource.');
            toast.error(message, { refId, details: { status } });
        } finally {
            setSaving(false);
        }
    };

    const confirmDelete = async () => {
        if (!pendingDelete) return;
        setDeleting(true);
        try {
            await deleteResource(pendingDelete);
            setPendingDelete(null);
            toast.success('Resource deleted.', { refId: getSuccessRefId() });
            fetchData();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not delete this resource.');
            toast.error(message, { refId, details: { status } });
        } finally {
            setDeleting(false);
        }
    };

    const handleDownload = async (resource) => {
        try {
            await incrementDownload(resource.id);
            window.open(resource.filePath, '_blank');
            fetchData(); // Refresh counts
        } catch (err) {
            // Fallback open if increment fails
            window.open(resource.filePath, '_blank');
        }
    };

    const getFileIcon = (type) => {
        switch (type?.toLowerCase()) {
            case 'pdf': return '📄';
            case 'mp4': case 'mkv': case 'video': return '🎥';
            case 'doc': case 'docx': return '📝';
            case 'zip': case 'rar': return '🗜️';
            default: return '📁';
        }
    };

    const formatSize = (bytes) => {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / 1048576).toFixed(1) + ' MB';
    };

    const canUpload = SessionManager.hasPermission('CREATE_RESOURCE');
    const canDelete = SessionManager.hasPermission('DELETE_RESOURCE') || SessionManager.hasPermission('MANAGE_RESOURCE');

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>📚 Learning Resources</h2>
                    <p className="text-muted">Digital library, lecture notes, and course materials.</p>
                </div>
                {canUpload && (
                    <button className="btn btn-primary" onClick={() => { setSizeMb(((formData.fileSize || 104858) / 1048576).toFixed(1)); setSizeError(''); setShowModal(true); }}>
                        + Upload Resource
                    </button>
                )}
            </div>

            {error && (
                <div className="retry-bar" role="alert" style={{ marginBottom: '16px' }}>
                    <span>{error} (Showing loaded records only.)</span>
                    <button className="btn btn-secondary btn-sm" onClick={() => fetchData()}>Retry</button>
                </div>
            )}
            {loading ? (
                <SkeletonTable rows={6} cols={5} />
            ) : (
            <div className="data-table-container">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Resource</th>
                            <th>Category</th>
                            <th>Course</th>
                            <th>Size</th>
                            <th>Downloads</th>
                            <th>Uploaded By</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {visibleResources.length === 0 ? (
                            <tr><td colSpan="7" style={{ textAlign: 'center' }}>{isStudent && enrolledIds ? 'No resources for your enrolled subjects yet' : 'No resources found'}</td></tr>
                        ) : (
                            visibleResources.map(res => (
                                <tr key={res.id} style={highlightId === res.id ? { background: '#f0fdf4' } : undefined}>
                                    <td>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                            <span style={{ fontSize: '1.5rem' }}>{getFileIcon(res.fileType)}</span>
                                            <div>
                                                <div style={{ fontWeight: '500' }}>{res.title}</div>
                                                <small className="text-muted">{res.description}</small>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span className="status-badge status-active">{res.categoryName}</span></td>
                                    <td>{res.courseName || <span className="text-muted">General/Public</span>}</td>
                                    <td>{formatSize(res.fileSize)}</td>
                                    <td>{res.downloadCount}</td>
                                    <td>{res.uploaderName}</td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '10px' }}>
                                            <button className="btn btn-primary" onClick={() => handleDownload(res)} style={{ padding: '5px 10px' }}>
                                                ⬇️ Download
                                            </button>
                                            {canDelete && (currentUser.id === res.uploadedBy || currentUser.role === 'ADMIN') && (
                                                <button className="btn btn-danger" onClick={() => setPendingDelete(res.id)} style={{ padding: '5px 10px' }}>
                                                    Delete
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
            )}

            <Modal
                isOpen={showModal}
                title="Upload New Resource"
                onClose={() => setShowModal(false)}
                onSubmit={handleSubmit}
                submitLabel="Upload Resource"
                submitting={saving}
                isDirty={isDirty}
                size="large"
            >
                <form onSubmit={(e) => { e.preventDefault(); handleSubmit(); }} className="form-grid">
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Resource Title *</label>
                        <input required type="text" name="title" className="form-control" value={formData.title} onChange={handleInputChange} placeholder="e.g. Data Structures Lecture 5" />
                    </div>

                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Description</label>
                        <textarea name="description" className="form-control" value={formData.description} onChange={handleInputChange} rows="3" placeholder="What does this resource cover?" />
                    </div>

                    <div className="form-group">
                        <label className="form-label">Category *</label>
                        <select name="categoryId" className="form-control" value={formData.categoryId} onChange={handleInputChange} required>
                            {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                        </select>
                    </div>

                    <div className="form-group">
                        <label className="form-label">Course Binding (Optional)</label>
                        <select name="courseId" className="form-control" value={formData.courseId} onChange={handleInputChange}>
                            <option value="">-- General / Public Resource --</option>
                            {courses.map(c => <option key={c.id} value={c.id}>{c.department_name} - {c.name}</option>)}
                        </select>
                    </div>

                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">External File URL (S3, Dropbox, GDrive) *</label>
                        <input required type="url" name="filePath" className="form-control" value={formData.filePath} onChange={handleInputChange} placeholder="https://..." />
                    </div>

                    <div className="form-group">
                        <label className="form-label">File Type *</label>
                        <select name="fileType" className="form-control" value={formData.fileType} onChange={handleInputChange} required>
                            <option value="pdf">PDF Document</option>
                            <option value="doc">Word Document (.doc)</option>
                            <option value="docx">Word Document (.docx)</option>
                            <option value="ppt">PowerPoint (.ppt)</option>
                            <option value="pptx">PowerPoint (.pptx)</option>
                            <option value="xls">Excel Spreadsheet (.xls)</option>
                            <option value="xlsx">Excel Spreadsheet (.xlsx)</option>
                            <option value="mp4">Video (.mp4)</option>
                            <option value="mkv">Video (.mkv)</option>
                            <option value="zip">Archive (.zip)</option>
                            <option value="rar">Archive (.rar)</option>
                            <option value="txt">Text File (.txt)</option>
                            <option value="link">External Link / URL</option>
                            <option value="other">Other</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label className="form-label">File Size (MB, min 0.1) *</label>
                        <input
                            type="number"
                            step="0.01"
                            min="0.1"
                            required
                            className={`form-control${sizeError ? ' is-invalid' : ''}`}
                            placeholder="e.g. 2.5"
                            value={sizeMb}
                            onChange={e => {
                                setSizeMb(e.target.value);
                                const nextMb = parseFloat(e.target.value);
                                const nextBytes = Number.isFinite(nextMb) ? Math.round(nextMb * 1048576) : NaN;
                                setFormData(prev => ({ ...prev, fileSize: Number.isFinite(nextBytes) ? nextBytes : prev.fileSize }));
                                if (Number.isFinite(nextBytes) && nextBytes >= 104858) setSizeError('');
                            }}
                            aria-invalid={Boolean(sizeError)}
                            aria-describedby={sizeError ? 'resource-size-error' : undefined}
                        />
                        <span className="field-hint">Exact value preserved — 0.05 stays 0.05 and is rejected (min 0.1).</span>
                        {sizeError && <small id="resource-size-error" className="field-error" role="alert">{sizeError}</small>}
                    </div>

                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                            <input type="checkbox" name="isPublic" checked={formData.isPublic} onChange={handleInputChange} />
                            Make Publicly Available (No Course Registration Required)
                        </label>
                    </div>
                </form>
            </Modal>
            <ConfirmDialog
                isOpen={pendingDelete !== null}
                title="Delete this resource?"
                message="The resource will be removed for all students. This cannot be undone."
                confirmLabel="Delete"
                loading={deleting}
                onConfirm={confirmDelete}
                onCancel={() => { if (!deleting) setPendingDelete(null); }}
            />
        </div>
    );
};

export default ResourceManagementPage;
