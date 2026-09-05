import SessionManager from '../utils/SessionManager';
import React, { useState, useEffect, useMemo, useRef, useCallback } from 'react';
import api from '../services/api';
import { searchStudents } from '../services/studentService';
import Modal from '../components/Modal';

const StudentProfilePage = () => {
    const user = SessionManager.getUser() || {};
    const userRole = SessionManager.getUserRole() || 'STUDENT';
    const isAdmin = userRole === 'ADMIN';
    const isFaculty = userRole === 'FACULTY';
    const canSearch = SessionManager.hasPermission('VIEW_STUDENT_PROFILE') && (isAdmin || isFaculty);

    const [student, setStudent] = useState(null);
    const [grades, setGrades] = useState([]);
    const [enrolledCourses, setEnrolledCourses] = useState([]);
    const [fees, setFees] = useState([]);
    const [attendanceRecords, setAttendanceRecords] = useState([]);
    const [showEditModal, setShowEditModal] = useState(false);
    const [editForm, setEditForm] = useState({});
    const [saving, setSaving] = useState(false);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('academic');
    const [error, setError] = useState(null);
    const [cgpa, setCgpa] = useState(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [showSearchDropdown, setShowSearchDropdown] = useState(false);
    const [viewedStudentId, setViewedStudentId] = useState(null);
    const searchRef = useRef(null);

    // Fetch student profile data
    const fetchStudentData = useCallback(async (studentId) => {
        setLoading(true);
        setError(null);
        try {
            const sRes = await api.get(`/students/${studentId}`);
            const found = sRes.data;
            if (found) {
                setStudent(found);

                // Fetch grades using correct endpoint
                try {
                    const gRes = await api.get(`/grades/student/${studentId}`);
                    const gData = Array.isArray(gRes.data) ? gRes.data : (gRes.data?.data || []);
                    setGrades(gData);
                } catch { setGrades([]); }

                // Fetch CGPA using backend endpoint
                try {
                    const cgpaRes = await api.get(`/grades/student/${studentId}/cgpa`);
                    const cgpaData = cgpaRes.data;
                    setCgpa(typeof cgpaData === 'object' ? cgpaData.cgpa : parseFloat(cgpaData));
                } catch { setCgpa(null); }

                // Fetch fees and filter by student
                try {
                    const fRes = await api.get('/fees');
                    const fList = Array.isArray(fRes.data) ? fRes.data : (fRes.data?.data || []);
                    setFees(fList.filter(f => f.studentId === studentId || f.student_id === studentId));
                } catch { setFees([]); }

                // Fetch attendance records using correct endpoint
                try {
                    const aRes = await api.get(`/attendance/student/${studentId}`);
                    const aData = Array.isArray(aRes.data) ? aRes.data : (aRes.data?.data || []);
                    setAttendanceRecords(aData);
                } catch { setAttendanceRecords([]); }

                // Fetch enrolled subjects (source of truth: course_registrations)
                try {
                    const cRes = await api.get(`/students/${studentId}/courses`);
                    const cData = Array.isArray(cRes.data) ? cRes.data : (cRes.data?.data || []);
                    setEnrolledCourses(cData);
                } catch { setEnrolledCourses([]); }
            } else {
                setError('Student record not found.');
            }
        } catch {
            setError('Could not load student profile.');
        } finally {
            setLoading(false);
        }
    }, []);

    // Load own profile on mount
    useEffect(() => {
        if (viewedStudentId === null) {
            const fetchOwn = async () => {
                setLoading(true);
                setError(null);
                try {
                    const sRes = await api.get('/students');
                    const sList = Array.isArray(sRes.data) ? sRes.data : (sRes.data?.data || []);
                    const found = sList.find(s =>
                        s.userId === user.id || s.user_id === user.id ||
                        s.email === user.email
                    );
                    if (found) {
                        await fetchStudentData(found.id);
                    } else {
                        setError('Student record not found.');
                        setLoading(false);
                    }
                } catch {
                    setError('Could not load student profile.');
                    setLoading(false);
                }
            };
            fetchOwn();
        }
    }, [user.id, user.email, fetchStudentData, viewedStudentId]);

    // Load viewed student when viewedStudentId changes
    useEffect(() => {
        if (viewedStudentId !== null) {
            fetchStudentData(viewedStudentId);
        }
    }, [viewedStudentId, fetchStudentData]);

    // Debounce search query
    useEffect(() => {
        const timer = setTimeout(() => setDebouncedSearchQuery(searchQuery), 300);
        return () => clearTimeout(timer);
    }, [searchQuery]);

    // Search students when debounced query changes
    useEffect(() => {
        const query = debouncedSearchQuery.trim();
        if (!query) {
            setSearchResults([]);
            return;
        }
        searchStudents(query)
            .then(res => {
                const data = Array.isArray(res.data) ? res.data : (res.data?.data || []);
                setSearchResults(data);
            })
            .catch(() => setSearchResults([]));
    }, [debouncedSearchQuery]);

    // Click outside to close dropdown
    useEffect(() => {
        const handleClickOutside = (e) => {
            if (searchRef.current && !searchRef.current.contains(e.target)) {
                setShowSearchDropdown(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // Compute attendance percentage from records
    const attPct = useMemo(() => {
        if (!attendanceRecords.length) return null;
        const present = attendanceRecords.filter(r => r.status === 'PRESENT' || r.status === 'LATE').length;
        return (present / attendanceRecords.length) * 100;
    }, [attendanceRecords]);

    // Fee calculations (already use filtered fees)
    const totalFees = useMemo(() => fees.reduce((s, f) => s + (parseFloat(f.amount) || 0), 0), [fees]);
    const paidFees = useMemo(() => fees.filter(f => f.status === 'PAID').reduce((s, f) => s + (parseFloat(f.amount) || 0), 0), [fees]);
    const pendingFees = useMemo(() => totalFees - paidFees, [totalFees, paidFees]);

    const initials = (student?.name || user.name || user.username || 'S')
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

    const handleSelectStudent = (selectedStudent) => {
        setViewedStudentId(selectedStudent.id);
        setSearchQuery(`${selectedStudent.name} (${selectedStudent.username || selectedStudent.enrollmentId || selectedStudent.enrollmentNumber})`);
        setShowSearchDropdown(false);
        setSearchResults([]);
    };

    const handleClearSearch = () => {
        setViewedStudentId(null);
        setSearchQuery('');
        setSearchResults([]);
        setShowSearchDropdown(false);
    };

return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>👤 Profile Center</h2>
                    <p className="text-muted">Manage your personal and institutional records</p>
                </div>
                <button className="btn btn-primary" onClick={() => { setEditForm({ ...student, ...user }); setShowEditModal(true); }}>
                    ✏️ Edit Profile
                </button>
            </div>

            {/* Student Search (Admin/Faculty only) */}
            {canSearch && (
                <div ref={searchRef} style={{ marginBottom: '20px' }}>
                    <div style={{ position: 'relative' }}>
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Search student by name, enrollment ID, or email..."
                            value={searchQuery}
                            onChange={(e) => {
                                setSearchQuery(e.target.value);
                                setShowSearchDropdown(true);
                            }}
                            onFocus={() => searchQuery && setShowSearchDropdown(true)}
                            style={{ paddingRight: viewedStudentId ? '40px' : '0' }}
                        />
                        {viewedStudentId && (
                            <button
                                type="button"
                                className="btn btn-secondary"
                                style={{ position: 'absolute', right: '8px', top: '50%', transform: 'translateY(-50%)', padding: '4px 10px', fontSize: '0.8rem' }}
                                onClick={handleClearSearch}
                            >
                                ✕ Clear
                            </button>
                        )}
                        {showSearchDropdown && searchResults.length > 0 && (
                            <div style={{
                                position: 'absolute',
                                top: '100%',
                                left: '0',
                                right: '0',
                                background: 'white',
                                border: '1px solid #e2e8f0',
                                borderRadius: '8px',
                                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                                zIndex: 100,
                                maxHeight: '300px',
                                overflowY: 'auto',
                                marginTop: '4px'
                            }}>
                                {searchResults.map((s, i) => (
                                    <button
                                        key={s.id || i}
                                        type="button"
                                        style={{
                                            width: '100%',
                                            padding: '12px 16px',
                                            textAlign: 'left',
                                            border: 'none',
                                            background: 'white',
                                            cursor: 'pointer',
                                            borderBottom: i < searchResults.length - 1 ? '1px solid #f1f5f9' : 'none'
                                        }}
                                        onMouseEnter={() => {}}
                                        onClick={() => handleSelectStudent(s)}
                                    >
                                        <div style={{ fontWeight: 600, color: '#1a202c' }}>{s.name}</div>
                                        <div style={{ fontSize: '0.8rem', color: '#718096', display: 'flex', gap: '12px', marginTop: '4px' }}>
                                            {s.username && <span>Enrollment: <code>{s.username}</code></span>}
                                            {s.enrollmentId && <span>Enrollment: <code>{s.enrollmentId}</code></span>}
                                            {s.email && <span>✉ {s.email}</span>}
                                            {s.course && <span>📚 {s.course}</span>}
                                            {s.department && <span>🏛 {s.department}</span>}
                                        </div>
                                    </button>
                                ))}
                            </div>
                        )}
                        {showSearchDropdown && searchResults.length === 0 && searchQuery && (
                            <div style={{
                                position: 'absolute',
                                top: '100%',
                                left: '0',
                                right: '0',
                                background: 'white',
                                border: '1px solid #e2e8f0',
                                borderRadius: '8px',
                                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                                zIndex: 100,
                                padding: '16px',
                                textAlign: 'center',
                                color: '#718096',
                                marginTop: '4px'
                            }}>
                                No students found
                            </div>
                        )}
                    </div>
                </div>
            )}

            {error && <div style={{ color: '#e53e3e', marginBottom: '16px', padding: '12px', background: '#fff5f5', borderRadius: '8px' }}>{error}</div>}

            {/* Viewing another student indicator */}
            {viewedStudentId !== null && (
                <div style={{ marginBottom: '16px', padding: '12px', background: '#ebf8ff', border: '1px solid #bee3f8', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span style={{ color: '#2b6cb0', fontWeight: 500 }}>Viewing another student's profile</span>
                    <button className="btn btn-sm btn-secondary" onClick={handleClearSearch}>Back to My Profile</button>
                </div>
            )}

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
                        {student?.enrollmentId || student?.username
                            ? <span style={{ fontWeight: 'bold', fontFamily: 'monospace', color: 'white' }}>ID: {student.enrollmentId || student.username}</span>
                            : `User ID: ${user.id}`}
                        {student?.department && <span style={{ marginLeft: '16px' }}>🏛 {student.department}</span>}
                        {student?.course && <span style={{ marginLeft: '16px' }}>📚 {student.course}</span>}
                        {student?.specialization && <span style={{ marginLeft: '16px' }}>🎯 Track: {student.specialization}</span>}
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
                <>
                {enrolledCourses.length > 0 && (
                    <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '16px 20px', marginBottom: '16px' }}>
                        <div style={{ fontWeight: '700', color: '#2d3748', marginBottom: '8px' }}>📚 Enrolled Subjects ({enrolledCourses.length})</div>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                            {enrolledCourses.map(c => (
                                <span key={c.id} style={{ background: '#ebf8ff', color: '#2b6cb0', padding: '4px 12px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: '600' }}>
                                    {c.code} — {c.name}{c.specialization ? ` [${c.specialization}]` : ''}
                                </span>
                            ))}
                        </div>
                    </div>
                )}
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
                                        <td style={{ fontWeight: 500 }}>{g.courseName || g.subject || `Subject ${g.courseId}`}</td>
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
                </>
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
                            { label: 'Username', value: student?.username || user.username },
                            { label: 'Email', value: student?.email || user.email },
                            { label: 'Phone', value: student?.phone },
                            { label: 'Enrollment No.', value: student?.enrollmentId || student?.username || student?.enrollmentNumber || student?.enrollment_number },
                            { label: 'Department', value: student?.department },
                            { label: 'Course', value: student?.course },
                            { label: 'Track / Specialization', value: student?.specialization },
                            { label: 'Semester', value: student?.semester },
                            { label: 'Batch Year', value: student?.batchYear || student?.batch_year },
                            { label: 'Gender', value: student?.gender },
                            { label: 'Date of Birth', value: student?.dob },
                            { label: 'Blood Group', value: student?.bloodGroup },
                            { label: 'Category', value: student?.category },
                            { label: 'Nationality', value: student?.nationality },
                            { label: 'Address', value: student?.address },
                            { label: 'Father\'s Name', value: student?.fatherName },
                            { label: 'Mother\'s Name', value: student?.motherName },
                            { label: 'Guardian Contact', value: student?.guardianContact },
                            { label: 'Previous School', value: student?.previousSchool },
                            { label: '10th Percentage', value: student?.tenthPercentage },
                            { label: '12th Percentage', value: student?.twelfthPercentage },
                            { label: 'Extracurricular Activities', value: student?.extracurricularActivities },
                            { label: 'Hostel Status', value: student?.isHostelite || student?.hostelite ? 'Hostelite' : 'Dayscholar' },
                            { label: 'Role', value: student?.role || user.role },
                        ].filter(f => f.value).map(f => (
                            <React.Fragment key={f.label}>
                                <dt style={{ color: '#718096', fontSize: '0.85rem', fontWeight: '500' }}>{f.label}</dt>
                                <dd style={{ color: '#2d3748', fontSize: '0.9rem', margin: 0 }}>{String(f.value)}</dd>
                            </React.Fragment>
                        ))}
                    </dl>
                </div>
            )}
            {/* Edit Modal */}
            {showEditModal && (
                <Modal
                    isOpen={showEditModal}
                    title="Modify Digital Profile"
                    onClose={() => setShowEditModal(false)}
                    onSubmit={async () => {
                        setSaving(true);
                        try {
                            if (userRole === 'STUDENT') await api.put(`/students/${student.id}`, editForm);
                            else if (userRole === 'FACULTY') await api.put(`/faculty/${student.id}`, editForm);
                            setShowEditModal(false);
                            window.location.reload();
                        } catch {
                            alert('Update failed. Check system logs.');
                        } finally { setSaving(false); }
                    }}
                    submitLabel={saving ? 'Updating...' : 'Save Changes'}
                >
                    <div className="form-grid">
                        <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                            <label>Full Display Name</label>
                            <input className="form-control" type="text" value={editForm.name || ''} onChange={e => setEditForm({ ...editForm, name: e.target.value })} />
                        </div>
                        <div className="form-group">
                            <label>Primary Contact</label>
                            <input className="form-control" type="text" value={editForm.phone || ''} onChange={e => setEditForm({ ...editForm, phone: e.target.value })} />
                        </div>
                        <div className="form-group">
                            <label>Gender</label>
                            <select className="form-control" value={editForm.gender || ''} onChange={e => setEditForm({ ...editForm, gender: e.target.value })}>
                                <option value="MALE">Male</option>
                                <option value="FEMALE">Female</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>
                        {userRole === 'STUDENT' ? (
                            <>
                                <div className="form-group">
                                    <label>Batch Year</label>
                                    <input className="form-control" type="text" value={editForm.batchYear || ''} onChange={e => setEditForm({ ...editForm, batchYear: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Hostel Status</label>
                                    <select className="form-control" value={editForm.hostelite ? 'YES' : 'NO'} onChange={e => setEditForm({ ...editForm, hostelite: e.target.value === 'YES' })}>
                                        <option value="NO">Dayscholar</option>
                                        <option value="YES">Hostelite</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Address</label>
                                    <input className="form-control" type="text" value={editForm.address || ''} onChange={e => setEditForm({ ...editForm, address: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Date of Birth</label>
                                    <input className="form-control" type="date" value={editForm.dob || ''} onChange={e => setEditForm({ ...editForm, dob: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Blood Group</label>
                                    <input className="form-control" type="text" value={editForm.bloodGroup || ''} onChange={e => setEditForm({ ...editForm, bloodGroup: e.target.value })} placeholder="e.g., A+, O-" />
                                </div>
                                <div className="form-group">
                                    <label>Category</label>
                                    <input className="form-control" type="text" value={editForm.category || ''} onChange={e => setEditForm({ ...editForm, category: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Nationality</label>
                                    <input className="form-control" type="text" value={editForm.nationality || ''} onChange={e => setEditForm({ ...editForm, nationality: e.target.value })} />
                                </div>
                                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                    <label>Father's Name</label>
                                    <input className="form-control" type="text" value={editForm.fatherName || ''} onChange={e => setEditForm({ ...editForm, fatherName: e.target.value })} />
                                </div>
                                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                    <label>Mother's Name</label>
                                    <input className="form-control" type="text" value={editForm.motherName || ''} onChange={e => setEditForm({ ...editForm, motherName: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Guardian Contact</label>
                                    <input className="form-control" type="text" value={editForm.guardianContact || ''} onChange={e => setEditForm({ ...editForm, guardianContact: e.target.value })} />
                                </div>
                                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                    <label>Previous School</label>
                                    <input className="form-control" type="text" value={editForm.previousSchool || ''} onChange={e => setEditForm({ ...editForm, previousSchool: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>10th Percentage</label>
                                    <input className="form-control" type="number" step="0.01" value={editForm.tenthPercentage || ''} onChange={e => setEditForm({ ...editForm, tenthPercentage: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>12th Percentage</label>
                                    <input className="form-control" type="number" step="0.01" value={editForm.twelfthPercentage || ''} onChange={e => setEditForm({ ...editForm, twelfthPercentage: e.target.value })} />
                                </div>
                                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                    <label>Extracurricular Activities</label>
                                    <textarea className="form-control" rows={3} value={editForm.extracurricularActivities || ''} onChange={e => setEditForm({ ...editForm, extracurricularActivities: e.target.value })} placeholder="List activities, sports, clubs, achievements..." />
                                </div>
                            </>
                        ) : (
                            <>
                                <div className="form-group">
                                    <label>Qualification</label>
                                    <input className="form-control" type="text" value={editForm.qualification || ''} onChange={e => setEditForm({ ...editForm, qualification: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Specialization</label>
                                    <input className="form-control" type="text" value={editForm.specialization || ''} onChange={e => setEditForm({ ...editForm, specialization: e.target.value })} />
                                </div>
                            </>
                        )}
                    </div>
                </Modal>
            )}
        </div>
    );
};

export default StudentProfilePage;
