import React, { useState, useEffect } from 'react';
import { getMyProfile, updateMyProfile, getMyCourses, getMyWorkload, getMyFeedback, getMySchedule } from '../services/facultyService';
import { PieChart, Pie, Cell, Tooltip, BarChart, Bar, XAxis, YAxis, ResponsiveContainer } from 'recharts';
import SessionManager from '../utils/SessionManager';

const CHART_COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];

const FacultyPortalPage = () => {
    const [activeTab, setActiveTab] = useState('overview');
    const [profile, setProfile] = useState(null);
    const [courses, setCourses] = useState([]);
    const [workload, setWorkload] = useState(null);
    const [feedback, setFeedback] = useState([]);
    const [schedule, setSchedule] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [editMode, setEditMode] = useState(false);
    const [editForm, setEditForm] = useState({});
    const [saveMsg, setSaveMsg] = useState(null);

    const user = SessionManager.getUser();

    useEffect(() => {
        loadAll();
    }, []);

    const loadAll = async () => {
        setLoading(true);
        setError(null);
        try {
            const [profileRes, coursesRes, workloadRes, feedbackRes, scheduleRes] = await Promise.allSettled([
                getMyProfile(),
                getMyCourses(),
                getMyWorkload(),
                getMyFeedback(),
                getMySchedule()
            ]);

            if (profileRes.status === 'fulfilled') setProfile(profileRes.value.data);
            if (coursesRes.status === 'fulfilled') setCourses(coursesRes.value.data || []);
            if (workloadRes.status === 'fulfilled') setWorkload(workloadRes.value.data);
            if (feedbackRes.status === 'fulfilled') setFeedback(feedbackRes.value.data || []);
            if (scheduleRes.status === 'fulfilled') setSchedule(scheduleRes.value.data || []);
        } catch (err) {
            setError('Failed to load profile data.');
        } finally {
            setLoading(false);
        }
    };

    const handleEditSave = async () => {
        try {
            await updateMyProfile(editForm);
            setProfile({ ...profile, ...editForm });
            setEditMode(false);
            setSaveMsg('Profile updated successfully');
            setTimeout(() => setSaveMsg(null), 3000);
        } catch (err) {
            setSaveMsg(err.response?.data?.error || 'Failed to update profile');
            setTimeout(() => setSaveMsg(null), 3000);
        }
    };

    if (loading) {
        return (
            <div className="page-container">
                <div style={{ textAlign: 'center', padding: '50px' }}>
                    <div className="spinner"></div>
                    <p style={{ marginTop: '15px', color: '#666' }}>Loading your dashboard...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="page-container">
                <div className="alert alert-danger">{error}</div>
            </div>
        );
    }

    const stats = workload?.stats || {};
    const distribution = workload?.courses?.reduce((acc, c) => {
        const existing = acc.find(d => d.subject === c.name);
        if (existing) existing.hours += c.credits;
        else acc.push({ subject: c.name, hours: c.credits });
        return acc;
    }, []) || [];

    const tabs = [
        { id: 'overview', label: 'Overview' },
        { id: 'courses', label: 'My Courses' },
        { id: 'schedule', label: 'Schedule' },
        { id: 'workload', label: 'Workload' },
        { id: 'feedback', label: 'Feedback' },
        { id: 'profile', label: 'Profile' },
    ];

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">Faculty Portal</h1>
                    <p className="page-subtitle">Welcome back, {profile?.name || user?.name || 'Faculty'}</p>
                </div>
            </div>

            {saveMsg && (
                <div className={`alert ${saveMsg.includes('success') ? 'alert-success' : 'alert-danger'}`} style={{ marginBottom: '15px' }}>
                    {saveMsg}
                </div>
            )}

            <div style={{ display: 'flex', gap: '5px', marginBottom: '25px', borderBottom: '2px solid #e2e8f0', paddingBottom: '10px' }}>
                {tabs.map(tab => (
                    <button
                        key={tab.id}
                        onClick={() => setActiveTab(tab.id)}
                        style={{
                            padding: '8px 16px', border: 'none', borderRadius: '8px 8px 0 0',
                            background: activeTab === tab.id ? '#667eea' : 'transparent',
                            color: activeTab === tab.id ? 'white' : '#666',
                            cursor: 'pointer', fontWeight: activeTab === tab.id ? 'bold' : 'normal',
                            fontSize: '0.9rem'
                        }}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Overview Tab */}
            {activeTab === 'overview' && (
                <>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '25px' }}>
                        <div className="stat-card" style={{ background: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)', color: 'white' }}>
                            <div style={{ fontSize: '0.9rem', opacity: 0.8 }}>Courses Assigned</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>{stats.count || 0}</div>
                        </div>
                        <div className="stat-card" style={{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white' }}>
                            <div style={{ fontSize: '0.9rem', opacity: 0.8 }}>Total Credits</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>{stats.credits || 0}</div>
                        </div>
                        <div className="stat-card" style={{ background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', color: 'white' }}>
                            <div style={{ fontSize: '0.9rem', opacity: 0.8 }}>Total Students</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>{stats.totalStudents || 0}</div>
                        </div>
                        <div className="stat-card" style={{
                            background: (stats.credits || 0) > 18 ? 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)' :
                                (stats.credits || 0) >= 8 ? 'linear-gradient(135deg, #10b981 0%, #059669 100%)' :
                                'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
                            color: 'white'
                        }}>
                            <div style={{ fontSize: '0.9rem', opacity: 0.8 }}>Workload Status</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>
                                {(stats.credits || 0) > 18 ? 'OVERLOAD' : (stats.credits || 0) >= 8 ? 'OPTIMAL' : 'UNDERLOAD'}
                            </div>
                        </div>
                    </div>

                    {distribution.length > 0 && (
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                            <div className="card" style={{ padding: '20px' }}>
                                <h3 style={{ marginTop: 0 }}>Subject Distribution</h3>
                                <ResponsiveContainer width="100%" height={250}>
                                    <PieChart>
                                        <Pie data={distribution} dataKey="hours" nameKey="subject" cx="50%" cy="50%" outerRadius={90} innerRadius={50} label={({ subject, percent }) => `${subject} ${(percent * 100).toFixed(0)}%`} labelLine={false}>
                                            {distribution.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                                        </Pie>
                                        <Tooltip formatter={(v) => `${v} credits`} />
                                    </PieChart>
                                </ResponsiveContainer>
                            </div>
                            <div className="card" style={{ padding: '20px' }}>
                                <h3 style={{ marginTop: 0 }}>Credits by Subject</h3>
                                <ResponsiveContainer width="100%" height={250}>
                                    <BarChart data={distribution} barSize={28}>
                                        <XAxis dataKey="subject" tick={{ fontSize: 10 }} />
                                        <YAxis />
                                        <Tooltip formatter={v => `${v} credits`} />
                                        <Bar dataKey="hours" name="Credits" radius={[4, 4, 0, 0]}>
                                            {distribution.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                                        </Bar>
                                    </BarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>
                    )}
                </>
            )}

            {/* Courses Tab */}
            {activeTab === 'courses' && (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Code</th>
                                <th>Course Name</th>
                                <th>Credits</th>
                                <th>Semester</th>
                                <th>Enrolled</th>
                                <th>Specialization</th>
                            </tr>
                        </thead>
                        <tbody>
                            {courses.length === 0 ? (
                                <tr><td colSpan="6" style={{ textAlign: 'center' }}>No courses assigned yet.</td></tr>
                            ) : (
                                courses.map(c => (
                                    <tr key={c.id}>
                                        <td><strong>{c.code}</strong></td>
                                        <td>{c.name}</td>
                                        <td>{c.credits}</td>
                                        <td>Sem {c.semester}</td>
                                        <td>{c.enrolledCount || 0}/{c.capacity || 60}</td>
                                        <td><span className="badge badge-primary">{c.specialization || 'N/A'}</span></td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Schedule Tab */}
            {activeTab === 'schedule' && (
                <div className="card" style={{ padding: '20px' }}>
                    <h3 style={{ marginTop: 0 }}>Weekly Schedule</h3>
                    {schedule.length === 0 ? (
                        <p style={{ color: '#666' }}>No timetable entries found.</p>
                    ) : (
                        <div style={{ maxHeight: '500px', overflowY: 'auto' }}>
                            {schedule.map((slot, idx) => (
                                <div key={idx} style={{
                                    padding: '12px 16px', marginBottom: '8px',
                                    background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0',
                                    display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                                }}>
                                    <div>
                                        <div style={{ fontWeight: '600', color: '#1a202c' }}>{slot.dayOfWeek}</div>
                                        <div style={{ fontSize: '0.9rem', color: '#666' }}>{slot.timeSlot}</div>
                                    </div>
                                    <div style={{ textAlign: 'right' }}>
                                        <div style={{ fontWeight: '500' }}>{slot.subject}</div>
                                        <div style={{ fontSize: '0.8rem', color: '#666' }}>Room: {slot.roomNumber}</div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}

            {/* Workload Tab */}
            {activeTab === 'workload' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                    <div className="card" style={{ padding: '20px' }}>
                        <h3 style={{ marginTop: 0 }}>Workload Summary</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
                                <span>Courses Assigned</span><strong>{stats.count || 0}</strong>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
                                <span>Total Credits</span><strong>{stats.credits || 0}/18</strong>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
                                <span>Total Students</span><strong>{stats.totalStudents || 0}</strong>
                            </div>
                            <div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                                    <span style={{ fontSize: '0.85rem' }}>Load</span>
                                    <span style={{ fontSize: '0.85rem' }}>{Math.min(100, Math.round(((stats.credits || 0) / 18) * 100))}%</span>
                                </div>
                                <div style={{ width: '100%', height: '10px', background: '#e2e8f0', borderRadius: '5px', overflow: 'hidden' }}>
                                    <div style={{
                                        width: `${Math.min(100, ((stats.credits || 0) / 18) * 100)}%`,
                                        height: '100%',
                                        background: (stats.credits || 0) > 18 ? '#ef4444' : (stats.credits || 0) >= 8 ? '#10b981' : '#f59e0b',
                                        borderRadius: '5px'
                                    }}></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="card" style={{ padding: '20px' }}>
                        <h3 style={{ marginTop: 0 }}>Subject Hours</h3>
                        {distribution.length > 0 ? (
                            <ResponsiveContainer width="100%" height={250}>
                                <BarChart data={distribution} barSize={28}>
                                    <XAxis dataKey="subject" tick={{ fontSize: 10 }} />
                                    <YAxis />
                                    <Tooltip formatter={v => `${v} credits`} />
                                    <Bar dataKey="hours" name="Credits" radius={[4, 4, 0, 0]}>
                                        {distribution.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                                    </Bar>
                                </BarChart>
                            </ResponsiveContainer>
                        ) : (
                            <p style={{ color: '#666' }}>No course data available.</p>
                        )}
                    </div>
                </div>
            )}

            {/* Feedback Tab */}
            {activeTab === 'feedback' && (
                <div>
                    <h3 style={{ marginTop: 0 }}>Student Feedback</h3>
                    {feedback.length === 0 ? (
                        <div className="card" style={{ padding: '30px', textAlign: 'center', color: '#666' }}>
                            No feedback received yet.
                        </div>
                    ) : (
                        feedback.map((fb, idx) => (
                            <div key={idx} className="card" style={{ padding: '16px', marginBottom: '12px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                                    <strong>{fb.studentName || 'Anonymous'}</strong>
                                    <div style={{ display: 'flex', gap: '8px' }}>
                                        {fb.category && <span className="badge badge-primary">{fb.category}</span>}
                                        {fb.isPrivate && <span className="badge badge-warning">Private</span>}
                                    </div>
                                </div>
                                <p style={{ margin: 0, color: '#4a5568' }}>{fb.feedbackText}</p>
                                {fb.createdAt && (
                                    <div style={{ fontSize: '0.8rem', color: '#a0aec0', marginTop: '8px' }}>
                                        {new Date(fb.createdAt).toLocaleDateString()}
                                    </div>
                                )}
                            </div>
                        ))
                    )}
                </div>
            )}

            {/* Profile Tab */}
            {activeTab === 'profile' && (
                <div className="card" style={{ padding: '25px', maxWidth: '600px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <h3 style={{ marginTop: 0 }}>Profile Information</h3>
                        {SessionManager.hasPermission('UPDATE_MY_FACULTY_PROFILE') && (
                        <button className="btn btn-secondary btn-sm" onClick={() => {
                            if (editMode) {
                                setEditMode(false);
                            } else {
                                setEditForm({
                                    phone: profile?.phone || '',
                                    qualification: profile?.qualification || '',
                                    specialization: profile?.specialization || ''
                                });
                                setEditMode(true);
                            }
                        }}>
                            {editMode ? 'Cancel' : 'Edit Profile'}
                        </button>
                        )}
                    </div>

                    {editMode ? (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            <div className="form-group">
                                <label>Phone</label>
                                <input type="tel" className="form-control" value={editForm.phone || ''} onChange={e => setEditForm({ ...editForm, phone: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Qualification</label>
                                <input type="text" className="form-control" value={editForm.qualification || ''} onChange={e => setEditForm({ ...editForm, qualification: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Specialization</label>
                                <input type="text" className="form-control" value={editForm.specialization || ''} onChange={e => setEditForm({ ...editForm, specialization: e.target.value })} />
                            </div>
                            <button className="btn btn-primary" onClick={handleEditSave}>Save Changes</button>
                        </div>
                    ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {[
                                ['Name', profile?.name],
                                ['Email', profile?.email],
                                ['Phone', profile?.phone],
                                ['Department', profile?.department],
                                ['Qualification', profile?.qualification],
                                ['Specialization', profile?.specialization],
                                ['Join Date', profile?.joinDate ? new Date(profile.joinDate).toLocaleDateString() : 'N/A'],
                                ['Username', profile?.username],
                            ].map(([label, value]) => (
                                <div key={label} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
                                    <span style={{ color: '#666' }}>{label}</span>
                                    <strong>{value || 'N/A'}</strong>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default FacultyPortalPage;
