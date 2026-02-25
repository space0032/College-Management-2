import React, { useState, useEffect } from 'react';
import { getStudentById, getStudentByUserId } from '../services/studentService';
import api from '../services/api';

const StudentProfilePage = () => {
    const user = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();

    const [student, setStudent] = useState(null);
    const [grades, setGrades] = useState([]);
    const [fees, setFees] = useState([]);
    const [attendance, setAttendance] = useState(null);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('academic');
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchAll = async () => {
            setLoading(true);
            try {
                // Fetch student details
                const sRes = await api.get(`/students/user/${user.id}`).catch(() => api.get('/students'));
                const sList = Array.isArray(sRes.data) ? sRes.data : (sRes.data?.data || []);
                const found = sList.find(s =>
                    s.userId === user.id || s.user_id === user.id ||
                    s.email === user.email
                );
                if (found) {
                    setStudent(found);
                    // Fetch grades
                    api.get(`/grades?studentId=${found.id}`).then(r => {
                        setGrades(Array.isArray(r.data) ? r.data : (r.data?.data || []));
                    }).catch(() => { });
                    // Fetch fees
                    api.get(`/fees?studentId=${found.id}`).then(r => {
                        setFees(Array.isArray(r.data) ? r.data : (r.data?.data || []));
                    }).catch(() => { });
                    // Fetch attendance
                    api.get(`/attendance?studentId=${found.id}`).then(r => {
                        const data = r.data;
                        if (data && typeof data === 'object' && !Array.isArray(data)) {
                            setAttendance(data);
                        }
                    }).catch(() => { });
                }
            } catch {
                setError('Could not load student profile.');
            } finally {
                setLoading(false);
            }
        };
        fetchAll();
    }, [user.id, user.email]); // eslint-disable-line

    const cgpa = grades.length > 0
        ? (grades.reduce((s, g) => s + (parseFloat(g.gpa) || parseFloat(g.grade) || 0), 0) / grades.length).toFixed(2)
        : null;

    const totalFees = fees.reduce((s, f) => s + (parseFloat(f.amount) || 0), 0);
    const paidFees = fees.filter(f => f.status === 'PAID').reduce((s, f) => s + (parseFloat(f.amount) || 0), 0);
    const pendingFees = totalFees - paidFees;

    const attPct = attendance?.attendancePercentage ?? attendance?.percentage ?? null;

    const initials = (user.name || user.username || 'S')
        .split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);

    const tabStyle = (tab) => ({
        padding: '10px 22px', border: 'none',
        borderBottom: activeTab === tab ? '3px solid #3b82f6' : '3px solid transparent',
        background: 'none', cursor: 'pointer',
        fontWeight: activeTab === tab ? '600' : '400',
        color: activeTab === tab ? '#3b82f6' : '#718096',
        fontSize: '0.9rem', transition: 'all 0.2s'
    });

    if (loading) return <div style={{ textAlign: 'center', padding: '50px', color: '#888' }}>Loading profile...</div>;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>👤 Student Profile</h2>
                    <p className="text-muted">Your academic record and personal information.</p>
                </div>
            </div>

            {error && <div style={{ color: '#e53e3e', marginBottom: '16px', padding: '12px', background: '#fff5f5', borderRadius: '8px' }}>{error}</div>}

            {/* Profile Card */}
            <div style={{
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                borderRadius: '14px', padding: '28px', marginBottom: '24px',
                color: 'white', display: 'flex', alignItems: 'center', gap: '24px'
            }}>
                <div style={{
                    width: '80px', height: '80px', borderRadius: '50%',
                    background: 'rgba(255,255,255,0.3)', display: 'flex',
                    alignItems: 'center', justifyContent: 'center',
                    fontSize: '1.8rem', fontWeight: 'bold', flexShrink: 0
                }}>
                    {initials}
                </div>
                <div style={{ flex: 1 }}>
                    <div style={{ fontSize: '1.4rem', fontWeight: 'bold', marginBottom: '4px' }}>
                        {student?.name || user.name || user.username || 'Student'}
                    </div>
                    <div style={{ opacity: 0.85, fontSize: '0.9rem', marginBottom: '6px' }}>
                        {student?.enrollmentNumber || student?.enrollment_number
                            ? `ID: ${student.enrollmentNumber || student.enrollment_number}`
                            : `User ID: ${user.id}`}
                        {student?.department && <span style={{ marginLeft: '16px' }}>🏛 {student.department}</span>}
                        {student?.course && <span style={{ marginLeft: '16px' }}>📚 {student.course}</span>}
                    </div>
                    <div style={{ opacity: 0.8, fontSize: '0.85rem' }}>
                        {user.email && `✉ ${user.email}`}
                        {student?.phone && <span style={{ marginLeft: '16px' }}>📱 {student.phone}</span>}
                    </div>
                </div>
            </div>

            {/* Quick Stats */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '14px', marginBottom: '24px' }}>
                <div style={{ padding: '16px', background: '#ebf8ff', borderRadius: '10px', border: '1px solid #bee3f8', textAlign: 'center' }}>
                    <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#2b6cb0' }}>{cgpa ?? '—'}</div>
                    <div style={{ fontSize: '0.8rem', color: '#4a5568' }}>CGPA</div>
                </div>
                <div style={{ padding: '16px', background: '#f0fff4', borderRadius: '10px', border: '1px solid #9ae6b4', textAlign: 'center' }}>
                    <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#276749' }}>
                        {attPct !== null ? `${parseFloat(attPct).toFixed(1)}%` : '—'}
                    </div>
                    <div style={{ fontSize: '0.8rem', color: '#4a5568' }}>Attendance</div>
                </div>
                <div style={{ padding: '16px', background: '#fffaf0', borderRadius: '10px', border: '1px solid #fbd38d', textAlign: 'center' }}>
                    <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#7b341e' }}>
                        {pendingFees > 0 ? `₹${pendingFees.toLocaleString()}` : '₹0'}
                    </div>
                    <div style={{ fontSize: '0.8rem', color: '#4a5568' }}>Pending Fees</div>
                </div>
                <div style={{ padding: '16px', background: '#faf5ff', borderRadius: '10px', border: '1px solid #d6bcfa', textAlign: 'center' }}>
                    <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#553c9a' }}>{grades.length}</div>
                    <div style={{ fontSize: '0.8rem', color: '#4a5568' }}>Subjects</div>
                </div>
            </div>

            {/* Tabs */}
            <div style={{ borderBottom: '1px solid #e2e8f0', marginBottom: '20px', display: 'flex' }}>
                <button style={tabStyle('academic')} onClick={() => setActiveTab('academic')}>📊 Academic History</button>
                <button style={tabStyle('fees')} onClick={() => setActiveTab('fees')}>💳 Fee Summary</button>
                <button style={tabStyle('personal')} onClick={() => setActiveTab('personal')}>📋 Personal Info</button>
            </div>

            {/* Academic tab */}
            {activeTab === 'academic' && (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Subject / Course</th>
                                <th>Semester</th>
                                <th>Internal</th>
                                <th>External</th>
                                <th>Total</th>
                                <th>Grade/GPA</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {grades.length === 0 ? (
                                <tr>
                                    <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: '#888' }}>No grade records found.</td>
                                </tr>
                            ) : (
                                grades.map((g, i) => (
                                    <tr key={g.id || i}>
                                        <td style={{ fontWeight: 500 }}>{g.courseName || g.subject || `Course ${g.courseId}`}</td>
                                        <td>{g.semester || g.term || '—'}</td>
                                        <td>{g.internalMarks ?? g.internal ?? '—'}</td>
                                        <td>{g.externalMarks ?? g.external ?? '—'}</td>
                                        <td>{g.totalMarks ?? g.total ?? '—'}</td>
                                        <td style={{ fontWeight: 'bold', color: '#3b82f6' }}>{g.grade || g.gpa || '—'}</td>
                                        <td><span className={`status-badge ${g.status === 'PASS' ? 'status-active' : g.status === 'FAIL' ? 'status-rejected' : ''}`}>{g.status || '—'}</span></td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Fees Tab */}
            {activeTab === 'fees' && (
                <div>
                    <div style={{ display: 'flex', gap: '14px', marginBottom: '20px' }}>
                        <div style={{ flex: 1, padding: '14px', background: '#f0fff4', borderRadius: '8px', textAlign: 'center' }}>
                            <div style={{ fontWeight: 'bold', color: '#276749' }}>₹{paidFees.toLocaleString()}</div>
                            <div style={{ fontSize: '0.8rem', color: '#4a5568' }}>Paid</div>
                        </div>
                        <div style={{ flex: 1, padding: '14px', background: '#fff5f5', borderRadius: '8px', textAlign: 'center' }}>
                            <div style={{ fontWeight: 'bold', color: '#c53030' }}>₹{pendingFees.toLocaleString()}</div>
                            <div style={{ fontSize: '0.8rem', color: '#4a5568' }}>Pending</div>
                        </div>
                        <div style={{ flex: 1, padding: '14px', background: '#ebf8ff', borderRadius: '8px', textAlign: 'center' }}>
                            <div style={{ fontWeight: 'bold', color: '#2b6cb0' }}>₹{totalFees.toLocaleString()}</div>
                            <div style={{ fontSize: '0.8rem', color: '#4a5568' }}>Total</div>
                        </div>
                    </div>
                    <div className="data-table-container">
                        <table className="data-table">
                            <thead><tr><th>Fee Type</th><th>Amount</th><th>Due Date</th><th>Paid Date</th><th>Status</th></tr></thead>
                            <tbody>
                                {fees.length === 0 ? (
                                    <tr><td colSpan="5" style={{ textAlign: 'center', padding: '30px', color: '#888' }}>No fee records found.</td></tr>
                                ) : (
                                    fees.map((f, i) => (
                                        <tr key={f.id || i}>
                                            <td>{f.feeType || f.fee_type || f.type || 'Tuition Fee'}</td>
                                            <td style={{ fontWeight: 500 }}>₹{parseFloat(f.amount || 0).toLocaleString()}</td>
                                            <td>{f.dueDate || f.due_date || '—'}</td>
                                            <td>{f.paidDate || f.paid_date || '—'}</td>
                                            <td><span className={`status-badge ${f.status === 'PAID' ? 'status-active' : 'status-pending'}`}>{f.status || 'PENDING'}</span></td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* Personal Info Tab */}
            {activeTab === 'personal' && (
                <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '24px' }}>
                    <dl style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '12px 20px' }}>
                        {[
                            { label: 'Full Name', value: student?.name || user.name },
                            { label: 'Username', value: user.username },
                            { label: 'Email', value: student?.email || user.email },
                            { label: 'Phone', value: student?.phone },
                            { label: 'Enrollment No.', value: student?.enrollmentNumber || student?.enrollment_number },
                            { label: 'Department', value: student?.department },
                            { label: 'Course', value: student?.course },
                            { label: 'Batch Year', value: student?.batchYear || student?.batch_year },
                            { label: 'Gender', value: student?.gender },
                            { label: 'Role', value: user.role },
                        ].filter(f => f.value).map(f => (
                            <React.Fragment key={f.label}>
                                <dt style={{ color: '#718096', fontSize: '0.85rem', fontWeight: '500' }}>{f.label}</dt>
                                <dd style={{ color: '#2d3748', fontSize: '0.9rem', margin: 0 }}>{String(f.value)}</dd>
                            </React.Fragment>
                        ))}
                    </dl>
                </div>
            )}
        </div>
    );
};

export default StudentProfilePage;
