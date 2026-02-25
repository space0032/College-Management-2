import React, { useState, useEffect } from 'react';
import { getResources, getResourceCategories, addResource, deleteResource, incrementDownload } from '../services/resourceService';
import { getAllCourses } from '../services/courseService';

const ResourceManagementPage = () => {
    const [resources, setResources] = useState([]);
    const [categories, setCategories] = useState([]);
    const [courses, setCourses] = useState([]);

    const [showModal, setShowModal] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [formData, setFormData] = useState({
        title: '',
        description: '',
        courseId: '',
        categoryId: '',
        filePath: '',
        fileType: 'pdf',
        fileSize: 1024,
        isPublic: true
    });

    const currentUser = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [resRes, catRes, crsRes] = await Promise.all([
                getResources(),
                getResourceCategories(),
                getAllCourses()
            ]);
            setResources(resRes.data || []);
            setCategories(catRes.data || []);
            setCourses(crsRes.data || []);

            if (catRes.data && catRes.data.length > 0) {
                setFormData(prev => ({ ...prev, categoryId: catRes.data[0].id }));
            }
        } catch (err) {
            console.error(err);
            setError('Failed to load resources data.');
        } finally {
            setLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const formattedData = {
                ...formData,
                courseId: formData.courseId ? parseInt(formData.courseId) : null,
                categoryId: parseInt(formData.categoryId),
                fileSize: parseInt(formData.fileSize),
                uploadedBy: currentUser.id
            };
            await addResource(formattedData);
            setShowModal(false);
            fetchData();
        } catch (err) {
            console.error(err);
            alert('Failed to upload resource.');
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Are you sure you want to delete this resource?')) {
            try {
                await deleteResource(id);
                fetchData();
            } catch (err) {
                console.error(err);
                alert('Failed to delete resource.');
            }
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

    if (loading) return <div className="page-container">Loading resources...</div>;
    if (error) return <div className="page-container" style={{ color: 'red' }}>{error}</div>;

    const canManage = currentUser.role === 'ADMIN' || currentUser.role === 'FACULTY';

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>📚 Learning Resources</h2>
                    <p className="text-muted">Digital library, lecture notes, and course materials.</p>
                </div>
                {canManage && (
                    <button className="btn btn-primary" onClick={() => setShowModal(true)}>
                        + Upload Resource
                    </button>
                )}
            </div>

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
                        {resources.length === 0 ? (
                            <tr><td colSpan="7" style={{ textAlign: 'center' }}>No resources found</td></tr>
                        ) : (
                            resources.map(res => (
                                <tr key={res.id}>
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
                                            {canManage && (currentUser.id === res.uploadedBy || currentUser.role === 'ADMIN') && (
                                                <button className="btn btn-danger" onClick={() => handleDelete(res.id)} style={{ padding: '5px 10px' }}>
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

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '600px' }}>
                        <div className="modal-header">
                            <h2>Upload New Resource</h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
                        </div>
                        <form onSubmit={handleSubmit} className="form-grid">

                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Resource Title</label>
                                <input required type="text" name="title" value={formData.title} onChange={handleInputChange} />
                            </div>

                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Description</label>
                                <textarea name="description" value={formData.description} onChange={handleInputChange} rows="3" />
                            </div>

                            <div className="form-group">
                                <label>Category</label>
                                <select name="categoryId" value={formData.categoryId} onChange={handleInputChange} required>
                                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                                </select>
                            </div>

                            <div className="form-group">
                                <label>Course Binding (Optional)</label>
                                <select name="courseId" value={formData.courseId} onChange={handleInputChange}>
                                    <option value="">-- General / Public Resource --</option>
                                    {courses.map(c => <option key={c.id} value={c.id}>{c.department_name} - {c.name}</option>)}
                                </select>
                            </div>

                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>External File URL (S3, GDrive, OneDrive)</label>
                                <input required type="url" name="filePath" value={formData.filePath} onChange={handleInputChange} placeholder="https://..." />
                            </div>

                            <div className="form-group">
                                <label>File Type Extension</label>
                                <input required type="text" name="fileType" value={formData.fileType} onChange={handleInputChange} placeholder="pdf, mp4, docx" />
                            </div>

                            <div className="form-group">
                                <label>Estimated Size (Bytes)</label>
                                <input required type="number" name="fileSize" value={formData.fileSize} onChange={handleInputChange} />
                            </div>

                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
                                    <input type="checkbox" name="isPublic" checked={formData.isPublic} onChange={handleInputChange} />
                                    Make Publicly Available (No Course Registration Required)
                                </label>
                            </div>

                            <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '10px' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Upload Resource</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ResourceManagementPage;
