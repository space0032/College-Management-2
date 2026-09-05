import React, { useState, useEffect } from 'react';
import { getSyllabiBycourse } from '../services/syllabusService';
import { getResources } from '../services/resourceService';
import { getAllCourses } from '../services/courseService';
import { searchStudents, getStudentCourses } from '../services/studentService';
import SessionManager from '../utils/SessionManager';

const getFileIcon = (path) => {
    const ext = (path?.split('.').pop() || '').toLowerCase();
    if (ext === 'pdf') return { icon: '📕', color: '#ef4444' };
    if (['mp4', 'webm', 'mov'].includes(ext)) return { icon: '🎬', color: '#8b5cf6' };
    if (['jpg', 'png', 'jpeg'].includes(ext)) return { icon: '🖼️', color: '#10b981' };
    return { icon: '📄', color: '#94a3b8' };
};

const LearningPortalPage = () => {
    const [activeTab, setActiveTab] = useState('resources'); // resources, syllabi
    const [courses, setCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState('');
    const [syllabi, setSyllabi] = useState([]);
    const [resources, setResources] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [enrolledOnly, setEnrolledOnly] = useState(false);

    useEffect(() => {
        const user = SessionManager.getUser() || {};
        const applyList = (list) => {
            setCourses(list);
            if (list.length > 0) setSelectedCourse(String(list[0].id));
        };
        if (user.role === 'STUDENT' && user.username) {
            searchStudents(user.username).then(res => {
                const match = (res.data || []).find(s => s.username === user.username) || (res.data || [])[0];
                if (match) {
                    return getStudentCourses(match.id).then(cRes => {
                        const enrolled = cRes.data || [];
                        if (enrolled.length > 0) {
                            setEnrolledOnly(true);
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
    }, []);

    useEffect(() => {
        if (activeTab === 'resources') {
            setLoading(true);
            setError('');
            getResources().then(res => {
                setResources(Array.isArray(res.data) ? res.data : (res.data?.data || []));
            }).catch(err => setError(err.response?.data?.error || 'Learning resources could not be loaded.'))
                .finally(() => setLoading(false));
        }
    }, [activeTab]);

    useEffect(() => {
        if (activeTab === 'syllabi' && selectedCourse) {
            setLoading(true);
            setError('');
            getSyllabiBycourse(selectedCourse)
                .then(res => setSyllabi(Array.isArray(res.data) ? res.data : []))
                .catch(err => setError(err.response?.data?.error || 'Syllabi could not be loaded.'))
                .finally(() => setLoading(false));
        }
    }, [activeTab, selectedCourse]);

    const enrolledIds = new Set((courses || []).map(c => String(c.id)));
    const filteredResources = resources.filter(r => {
        if (enrolledOnly && r.courseId && !enrolledIds.has(String(r.courseId))) return false;
        return !searchTerm ||
            (r.title || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
            (r.courseName || '').toLowerCase().includes(searchTerm.toLowerCase());
    });

    return (
        <div className="page-container" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h1 className="page-title">🚀 Intellectual Capital Portal</h1>
                        <p className="page-subtitle">Unified gateway to institutional syllabi, video lectures, and research materials</p>
                    </div>
                    <div style={{ display: 'flex', gap: '5px', background: '#e2e8f0', padding: '4px', borderRadius: '12px' }}>
                        <button className={`btn btn-sm ${activeTab === 'resources' ? 'btn-primary' : ''}`} style={{ background: activeTab === 'resources' ? '#3b82f6' : 'transparent', color: activeTab === 'resources' ? 'white' : '#475569', border: 'none' }} onClick={() => setActiveTab('resources')}>Materials</button>
                        <button className={`btn btn-sm ${activeTab === 'syllabi' ? 'btn-primary' : ''}`} style={{ background: activeTab === 'syllabi' ? '#3b82f6' : 'transparent', color: activeTab === 'syllabi' ? 'white' : '#475569', border: 'none' }} onClick={() => setActiveTab('syllabi')}>Frameworks</button>
                    </div>
                </div>
            </div>

            {error && <div className="alert alert-error" role="alert">{error}</div>}

            {/* Premium Stat Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '35px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Digital Assets</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>{resources.length}</div>
                    <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Sync'd Materials</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Video Lectures</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#8b5cf6', margin: '8px 0' }}>{Math.floor(resources.length * 0.4)} Units</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Total Downloads</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3b82f6', margin: '8px 0' }}>N/A</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Learning Streak</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#f59e0b', margin: '8px 0' }}>N/A</div>
                </div>
            </div>

            {activeTab === 'resources' && (
                <>
                    <div style={{ marginBottom: '25px', display: 'flex', gap: '15px' }}>
                        <div style={{ flex: 1, position: 'relative' }}>
                            <input
                                type="text"
                                className="form-control"
                                placeholder="Search by topic, unit name or file type..."
                                value={searchTerm}
                                onChange={e => setSearchTerm(e.target.value)}
                                style={{ paddingLeft: '40px', borderRadius: '15px' }}
                            />
                            <span style={{ position: 'absolute', left: '15px', top: '12px', color: '#94a3b8' }}>🔍</span>
                        </div>
                    </div>

                    {loading ? (
                        <div style={{ textAlign: 'center', padding: '100px', color: '#94a3b8' }}>📡 Accessing digital archive...</div>
                    ) : (
                        <div className="card-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '25px' }}>
                            {filteredResources.map(r => {
                                const meta = getFileIcon(r.filePath);
                                return (
                                    <div key={r.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column', transition: 'transform 0.2s', padding: '25px' }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                                            <div style={{ fontSize: '2.5rem', background: '#f8fafc', width: '60px', height: '60px', borderRadius: '15px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                                {meta.icon}
                                            </div>
                                            <span className="badge" style={{ background: '#f0fdf4', color: '#16a34a', fontSize: '0.65rem', height: 'fit-content' }}>NEW</span>
                                        </div>
                                        <h3 style={{ margin: '0 0 5px 0', fontSize: '1.1rem' }}>{r.title}</h3>
                                        <div style={{ fontSize: '0.75rem', color: '#3b82f6', fontWeight: 'bold', marginBottom: '15px' }}>📚 {r.courseName || 'General Resource'}</div>
                                        <p style={{ fontSize: '0.85rem', color: '#64748b', flex: 1, marginBottom: '20px' }}>{r.description || 'Institutional material for academic advancement.'}</p>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '15px', borderTop: '1px solid #f1f5f9' }}>
                                            <span style={{ fontSize: '0.7rem', color: '#94a3b8' }}>{r.fileType || 'Doc'} • {r.fileSize || 'Size unavailable'}</span>
                                            <a href={r.filePath} target="_blank" rel="noopener noreferrer" style={{ textDecoration: 'none', color: meta.color, fontWeight: 'bold', fontSize: '0.9rem' }}>Access Content &rarr;</a>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </>
            )}

            {activeTab === 'syllabi' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 3fr', gap: '30px' }}>
                    <div className="stat-card" style={{ height: 'fit-content' }}>
                        <h4 style={{ marginBottom: '15px' }}>Department Unit</h4>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                            {courses.map(c => (
                                <button key={c.id} onClick={() => setSelectedCourse(String(c.id))} style={{ textAlign: 'left', padding: '12px', borderRadius: '10px', background: selectedCourse === String(c.id) ? '#3b82f6' : 'white', color: selectedCourse === String(c.id) ? 'white' : '#475569', border: '1px solid #e2e8f0', cursor: 'pointer', fontSize: '0.85rem' }}>{c.name}</button>
                            ))}
                        </div>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                        {loading ? <div style={{ textAlign: 'center', padding: '50px' }}>Loading...</div> : (
                            syllabi.map(s => (
                                <div key={s.id} className="stat-card" style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                                    <div style={{ fontSize: '2rem' }}>📜</div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                            <h4 style={{ margin: 0 }}>{s.title}</h4>
                                            <span className="badge" style={{ background: '#eff6ff', color: '#3b82f6' }}>v{s.version}</span>
                                        </div>
                                        <p style={{ fontSize: '0.85rem', color: '#64748b', margin: '5px 0' }}>{s.description}</p>
                                    </div>
                                    <a href={s.filePath} target="_blank" rel="noopener noreferrer" className="btn btn-secondary">Download PDF</a>
                                </div>
                            ))
                        )}
                        {syllabi.length === 0 && !loading && <div style={{ textAlign: 'center', padding: '80px', color: '#94a3b8' }}>No curriculum frameworks for this unit.</div>}
                    </div>
                </div>
            )}
        </div>
    );
};

export default LearningPortalPage;
