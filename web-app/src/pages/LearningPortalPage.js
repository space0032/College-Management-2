import React, { useState, useEffect } from 'react';
import { getSyllabiBycourse } from '../services/syllabusService';
import { getResources } from '../services/resourceService';
import { getCourses } from '../services/courseService';

const getFileIcon = (path) => {
    if (!path) return '📄';
    const ext = (path.split('.').pop() || '').toLowerCase();
    if (ext === 'pdf') return '📕';
    if (ext.startsWith('doc')) return '📘';
    if (ext.startsWith('xls')) return '📊';
    if (ext.startsWith('ppt')) return '📽️';
    if (['mp4', 'webm', 'mov'].includes(ext)) return '🎬';
    if (['jpg', 'png', 'jpeg'].includes(ext)) return '🖼️';
    return '📄';
};

const LearningPortalPage = () => {
    const [activeTab, setActiveTab] = useState('syllabi');
    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState('');
    const [syllabi, setSyllabi] = useState([]);
    const [resources, setResources] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        getCourses().then(res => {
            const list = Array.isArray(res.data) ? res.data : (res.data?.data || []);
            setCourses(list);
            if (list.length > 0) setSelectedCourse(String(list[0].id));
        }).catch(() => { });
    }, []);

    useEffect(() => {
        if (activeTab === 'resources') {
            setLoading(true);
            getResources().then(res => {
                const list = Array.isArray(res.data) ? res.data : (res.data?.data || res.data?.resources || []);
                setResources(list);
            }).catch(() => { }).finally(() => setLoading(false));
        }
    }, [activeTab]);

    useEffect(() => {
        if (activeTab === 'syllabi' && selectedCourse) {
            setLoading(true);
            getSyllabiBycourse(selectedCourse)
                .then(res => setSyllabi(Array.isArray(res.data) ? res.data : []))
                .catch(() => setSyllabi([]))
                .finally(() => setLoading(false));
        }
    }, [activeTab, selectedCourse]);

    const filteredResources = resources.filter(r =>
        !searchTerm ||
        (r.title || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (r.courseName || r.course || '').toLowerCase().includes(searchTerm.toLowerCase())
    );

    const tabStyle = (tab) => ({
        padding: '10px 24px',
        border: 'none',
        borderBottom: activeTab === tab ? '3px solid #3b82f6' : '3px solid transparent',
        background: 'none',
        cursor: 'pointer',
        fontWeight: activeTab === tab ? '600' : '400',
        color: activeTab === tab ? '#3b82f6' : '#718096',
        fontSize: '0.95rem',
        transition: 'all 0.2s'
    });

    const selectedCName = courses.find(c => String(c.id) === selectedCourse)?.name || '';

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>🎓 Learning Portal</h2>
                    <p className="text-muted">Access course syllabi and learning materials.</p>
                </div>
            </div>

            {/* Tabs */}
            <div style={{ borderBottom: '1px solid #e2e8f0', marginBottom: '24px', display: 'flex' }}>
                <button style={tabStyle('syllabi')} onClick={() => setActiveTab('syllabi')}>
                    📋 Course Syllabi
                </button>
                <button style={tabStyle('resources')} onClick={() => setActiveTab('resources')}>
                    📚 Learning Resources
                </button>
            </div>

            {/* Syllabi Tab */}
            {activeTab === 'syllabi' && (
                <div>
                    <div style={{ marginBottom: '18px', display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <label style={{ fontWeight: 500 }}>Course:</label>
                        <select
                            value={selectedCourse}
                            onChange={e => setSelectedCourse(e.target.value)}
                            style={{ padding: '8px 14px', borderRadius: '6px', border: '1px solid #ddd', minWidth: '260px' }}
                        >
                            <option value="">-- Select a course --</option>
                            {courses.map(c => (
                                <option key={c.id} value={String(c.id)}>{c.name || c.courseName}</option>
                            ))}
                        </select>
                    </div>

                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>Loading...</div>
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
                                        <th>Download</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {syllabi.length === 0 ? (
                                        <tr>
                                            <td colSpan="6" style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                                                {selectedCourse ? `No syllabi uploaded for ${selectedCName}.` : 'Select a course above.'}
                                            </td>
                                        </tr>
                                    ) : (
                                        syllabi.map(s => (
                                            <tr key={s.id}>
                                                <td style={{ fontWeight: 500 }}>{getFileIcon(s.filePath)} {s.title}</td>
                                                <td><span className="status-badge" style={{ background: '#e3f2fd', color: '#1565c0' }}>v{s.version}</span></td>
                                                <td style={{ color: '#555', maxWidth: '240px' }}>{s.description || '—'}</td>
                                                <td>{s.uploaderName || '—'}</td>
                                                <td style={{ fontSize: '0.85rem', color: '#718096' }}>{s.uploadedAt ? s.uploadedAt.split('T')[0] : '—'}</td>
                                                <td>
                                                    {s.filePath ? (
                                                        <a href={s.filePath} target="_blank" rel="noopener noreferrer" className="btn btn-secondary" style={{ padding: '4px 10px', fontSize: '0.8rem' }}>
                                                            ⬇ Download
                                                        </a>
                                                    ) : <span style={{ color: '#aaa' }}>No file</span>}
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}

            {/* Resources Tab */}
            {activeTab === 'resources' && (
                <div>
                    <input
                        type="text"
                        placeholder="🔍 Search by title or course..."
                        value={searchTerm}
                        onChange={e => setSearchTerm(e.target.value)}
                        style={{ padding: '10px 14px', borderRadius: '8px', border: '1px solid #ddd', width: '100%', maxWidth: '400px', marginBottom: '18px', fontSize: '0.95rem' }}
                    />

                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>Loading resources...</div>
                    ) : filteredResources.length === 0 ? (
                        <div style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                            {resources.length === 0 ? 'No learning resources found.' : 'No matches for your search.'}
                        </div>
                    ) : (
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '16px' }}>
                            {filteredResources.map(r => (
                                <div key={r.id} style={{
                                    background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px',
                                    padding: '16px', boxShadow: '0 1px 3px rgba(0,0,0,0.05)'
                                }}>
                                    <div style={{ fontSize: '1.8rem', marginBottom: '8px' }}>{getFileIcon(r.filePath || r.fileType)}</div>
                                    <div style={{ fontWeight: '600', marginBottom: '4px', fontSize: '0.95rem' }}>{r.title}</div>
                                    {(r.courseName || r.course) && (
                                        <div style={{ color: '#3b82f6', fontSize: '0.8rem', marginBottom: '6px' }}>📚 {r.courseName || r.course}</div>
                                    )}
                                    {r.description && (
                                        <div style={{ color: '#718096', fontSize: '0.82rem', marginBottom: '10px' }}>{r.description}</div>
                                    )}
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span className="status-badge" style={{ background: '#f0fff4', color: '#276749' }}>
                                            {r.fileType || r.type || 'Document'}
                                        </span>
                                        {r.filePath && (
                                            <a href={r.filePath} target="_blank" rel="noopener noreferrer"
                                                style={{ color: '#3b82f6', fontSize: '0.8rem', fontWeight: 500 }}>
                                                ⬇ Download
                                            </a>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default LearningPortalPage;
