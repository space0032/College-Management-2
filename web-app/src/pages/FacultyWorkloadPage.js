import React, { useState, useEffect } from 'react';
import { getWorkloadAnalytics, getFacultyWorkload, assignCourse, unassignCourse, checkConflict, suggestCourses } from '../services/workloadService';

import { PieChart, Pie, Cell, Tooltip, BarChart, Bar, XAxis, YAxis, ResponsiveContainer } from 'recharts';

const CHART_COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];
const CREDIT_LIMIT = 18;

const FacultyWorkloadPage = () => {
    const [analytics, setAnalytics] = useState([]);
    const [selectedFaculty, setSelectedFaculty] = useState(null);
    const [facultyDetails, setFacultyDetails] = useState(null);
    const [loading, setLoading] = useState(true);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [detailsError, setDetailsError] = useState(null);
    const [error, setError] = useState(null);
    const [departmentFilter, setDepartmentFilter] = useState('');

    // Assignment modal state
    const [assignModal, setAssignModal] = useState(null);
    const [assignLoading, setAssignLoading] = useState(false);
    const [currentCourses, setCurrentCourses] = useState([]);
    const [availableCourses, setAvailableCourses] = useState([]);
    const [selectedCourse, setSelectedCourse] = useState(null);
    const [conflictInfo, setConflictInfo] = useState(null);
    const [assignError, setAssignError] = useState(null);
    const [assignSuccess, setAssignSuccess] = useState(null);

    useEffect(() => {
        fetchAnalytics();
    }, []);

    const fetchAnalytics = async () => {
        setLoading(true);
        try {
            const res = await getWorkloadAnalytics();
            setAnalytics(res.data || []);
        } catch (err) {
            console.error(err);
            setError('Failed to load workload analytics.');
        } finally {
            setLoading(false);
        }
    };

    const handleSelectFaculty = async (facultyName) => {
        setSelectedFaculty(facultyName);
        setDetailsLoading(true);
        setDetailsError(null);
        try {
            const res = await getFacultyWorkload(facultyName);
            setFacultyDetails(res.data);
        } catch (err) {
            console.error(err);
            setDetailsError('Failed to load faculty details.');
        } finally {
            setDetailsLoading(false);
        }
    };

    const openAssignModal = async (facultyRow) => {
        setAssignModal(facultyRow);
        setAssignLoading(true);
        setAssignError(null);
        setAssignSuccess(null);
        setCurrentCourses([]);
        setAvailableCourses([]);
        setSelectedCourse(null);
        setConflictInfo(null);

        try {
            const allCoursesRes = await import('../services/workloadService').then(m =>
                import('../services/api').then(api => api.default.get('/courses?page=1&size=1000'))
            );
            const allCourses = allCoursesRes.data || [];
            const assigned = allCourses.filter(c => c.facultyId === facultyRow.facultyId);
            const unassigned = allCourses.filter(c => !c.facultyId || c.facultyId === 0);

            setCurrentCourses(assigned);
            setAvailableCourses(unassigned);
        } catch (err) {
            setAssignError('Failed to load courses.');
        } finally {
            setAssignLoading(false);
        }
    };

    const handleAssign = async () => {
        if (!selectedCourse || !assignModal) return;
        setAssignError(null);
        setAssignSuccess(null);
        setConflictInfo(null);

        // Re-validate the selected course against a fresh list so a stale
        // ID (e.g. after pagination reset) surfaces a clear error.
        try {
            const freshRes = await import('../services/api').then(m => m.default.get('/courses?page=1&size=1000'));
            const freshList = freshRes.data || [];
            if (!freshList.some(c => c.id === selectedCourse.id)) {
                setAssignError(`Course ${selectedCourse.code || selectedCourse.id} no longer exists. Refresh and retry.`);
                return;
            }
        } catch {
            // Non-critical: continue with conflict check + assign attempt.
        }

        // Check for time conflicts first
        try {
            const conflictRes = await checkConflict(assignModal.facultyId, selectedCourse.id);
            if (conflictRes.data.hasConflict) {
                setConflictInfo(conflictRes.data.conflicts);
                return;
            }
        } catch (err) {
            // Continue even if conflict check fails (non-critical)
        }

        // Check for overload
        const currentCredits = currentCourses.reduce((sum, c) => sum + (c.credits || 0), 0);
        const newTotal = currentCredits + (selectedCourse.credits || 0);
        if (newTotal > CREDIT_LIMIT) {
            if (!window.confirm(`This will exceed the ${CREDIT_LIMIT}-credit limit (Total: ${newTotal}). Continue anyway?`)) {
                return;
            }
        }

        setAssignLoading(true);
        try {
            await assignCourse(selectedCourse.id, assignModal.facultyId);
            setAssignSuccess(`${selectedCourse.code} assigned successfully!`);
            setCurrentCourses([...currentCourses, selectedCourse]);
            setAvailableCourses(availableCourses.filter(c => c.id !== selectedCourse.id));
            setSelectedCourse(null);
            setConflictInfo(null);
            fetchAnalytics();
        } catch (err) {
            const status = err.response?.status;
            const msg = err.response?.data?.message || err.response?.data?.error || 'Failed to assign course';
            console.error(`Assign failed course ${selectedCourse.id} faculty ${assignModal.facultyId}:`, status, msg);
            if (status === 409) {
                setConflictInfo([{ dayOfWeek: 'Conflict', timeSlot: '', existingSubject: msg }]);
            } else if (status === 403) {
                setAssignError(`Not permitted to assign courses (${msg}). Requires UPDATE_COURSE permission.`);
            } else if (status === 404) {
                setAssignError(`Assign failed: ${msg}. The faculty or course ID may be stale — reopen Manage and retry.`);
            } else {
                setAssignError(status ? `Assign failed (${status}) course ${selectedCourse.id} faculty ${assignModal.facultyId}: ${msg}` : `${msg} (course ${selectedCourse.id} faculty ${assignModal.facultyId})`);
            }
        } finally {
            setAssignLoading(false);
        }
    };

    const handleUnassign = async (course) => {
        if (!window.confirm(`Unassign ${course.code} - ${course.name}?`)) return;
        setAssignError(null);
        setAssignSuccess(null);

        try {
            await unassignCourse(course.id);
            setAssignSuccess(`${course.code} unassigned successfully!`);
            setCurrentCourses(currentCourses.filter(c => c.id !== course.id));
            setAvailableCourses([...availableCourses, course]);
            fetchAnalytics();
        } catch (err) {
            setAssignError(err.response?.data?.error || 'Failed to unassign course');
        }
    };

    const handleSuggestFit = async () => {
        if (!assignModal) return;
        setAssignError(null);
        try {
            const res = await suggestCourses(assignModal.facultyId);
            // Backend returns { suggested: [...] }; tolerate a bare array for old builds.
            const suggested = Array.isArray(res.data) ? res.data : (res.data?.suggested || []);
            if (res.data && !Array.isArray(res.data) && res.data.message && suggested.length === 0) {
                setAssignError(res.data.message);
                return;
            }
            if (suggested.length === 0) {
                setAssignError('No matching courses found for this faculty specialization.');
                return;
            }
            setAvailableCourses(suggested);
            setSelectedCourse(suggested[0]);
        } catch (err) {
            setAssignError(err.response?.data?.error || err.response?.data?.message || 'Failed to get suggestions.');
        }
    };

    const departments = [...new Set(analytics.map(f => f.department).filter(Boolean))].sort();
    const filteredAnalytics = departmentFilter ? analytics.filter(f => f.department === departmentFilter) : analytics;

    if (loading) return <div className="page-container">Loading Workload Analytics...</div>;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>Faculty Workload</h2>
                    <p className="text-muted">Analyze teaching loads, assign courses, and manage workloads.</p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <select className="form-control" style={{ width: '200px' }} value={departmentFilter} onChange={e => setDepartmentFilter(e.target.value)}>
                        <option value="">All Departments</option>
                        {departments.map(d => <option key={d} value={d}>{d}</option>)}
                    </select>
                </div>
            </div>

            {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

            <div className="data-table-container">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Faculty Name</th>
                            <th>Department</th>
                            <th>Courses</th>
                            <th>Total Credits</th>
                            <th>Total Students</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredAnalytics.length === 0 ? (
                            <tr><td colSpan="7" style={{ textAlign: 'center' }}>No workload data found.</td></tr>
                        ) : (
                            filteredAnalytics.map(f => (
                                <tr key={f.facultyId}>
                                    <td style={{ fontWeight: '500' }}>{f.facultyName}</td>
                                    <td>{f.department}</td>
                                    <td>{f.totalClasses}</td>
                                    <td>
                                        <span style={{
                                            fontWeight: 'bold',
                                            color: f.totalCredits > CREDIT_LIMIT ? '#ef4444' : f.totalCredits >= 8 ? '#10b981' : '#f59e0b'
                                        }}>
                                            {f.totalCredits || 0}
                                        </span>
                                    </td>
                                    <td>{f.totalStudents || 0}</td>
                                    <td>
                                        <span className={`badge ${f.status === 'OVERLOAD' ? 'badge-danger' : f.status === 'OPTIMAL' ? 'badge-success' : 'badge-warning'}`}>
                                            {f.status || 'N/A'}
                                        </span>
                                    </td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '5px' }}>
                                            <button className="btn btn-primary btn-sm" onClick={() => handleSelectFaculty(f.facultyName)}>
                                                Details
                                            </button>
                                            <button className="btn btn-secondary btn-sm" onClick={() => openAssignModal(f)}>
                                                Manage
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {/* Workload Detail Modal */}
            {selectedFaculty && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '800px' }}>
                        <div className="modal-header">
                            <h2>Workload Report: {selectedFaculty}</h2>
                            <button className="modal-close" onClick={() => setSelectedFaculty(null)}>×</button>
                        </div>

                        {detailsLoading ? (
                            <div style={{ padding: '20px', textAlign: 'center' }}>Loading distribution...</div>
                        ) : detailsError ? (
                            <div className="alert alert-danger" style={{ margin: '20px' }}>{detailsError}</div>
                        ) : facultyDetails ? (
                            <div style={{ display: 'flex', gap: '30px', marginTop: '20px' }}>
                                <div style={{ flex: 1 }}>
                                    <h3>Subject Distribution</h3>
                                    <ResponsiveContainer width="100%" height={200}>
                                        <PieChart>
                                            <Pie data={facultyDetails.distribution} dataKey="hours" nameKey="subject" cx="50%" cy="50%" outerRadius={80} innerRadius={40} label={({ subject, percent }) => `${subject} ${(percent * 100).toFixed(0)}%`} labelLine={false}>
                                                {facultyDetails.distribution.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                                            </Pie>
                                            <Tooltip formatter={(v) => `${v} hrs`} />
                                        </PieChart>
                                    </ResponsiveContainer>
                                    <ul style={{ listStyle: 'none', padding: 0 }}>
                                        {facultyDetails.distribution.map((d, i) => (
                                            <li key={d.subject} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', padding: '10px', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #f1f5f9' }}>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                                    <span style={{ display: 'inline-block', width: '10px', height: '10px', borderRadius: '50%', backgroundColor: CHART_COLORS[i % CHART_COLORS.length] }}></span>
                                                    <span style={{ fontSize: '0.85rem' }}>{d.subject}</span>
                                                </div>
                                                <span style={{ fontWeight: 'bold', fontSize: '0.85rem' }}>{d.hours} hrs</span>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                                <div style={{ flex: 1 }}>
                                    <h3>Hours by Subject</h3>
                                    <ResponsiveContainer width="100%" height={180}>
                                        <BarChart data={facultyDetails.distribution} barSize={28}>
                                            <XAxis dataKey="subject" tick={{ fontSize: 10 }} />
                                            <YAxis />
                                            <Tooltip formatter={v => `${v} hrs`} />
                                            <Bar dataKey="hours" name="Hours" radius={[4, 4, 0, 0]}>
                                                {facultyDetails.distribution.map((_, i) => <Cell key={i} fill={CHART_COLORS[i % CHART_COLORS.length]} />)}
                                            </Bar>
                                        </BarChart>
                                    </ResponsiveContainer>

                                    <h3>Weekly Schedule ({facultyDetails.totalHours} Total Hours)</h3>
                                    <div style={{ maxHeight: '400px', overflowY: 'auto', border: '1px solid #eee', borderRadius: '4px' }}>
                                        {facultyDetails.schedule.map((slot, index) => (
                                            <div key={index} style={{ padding: '10px', borderBottom: '1px solid #eee' }}>
                                                <div style={{ fontWeight: 'bold' }}>{slot.dayOfWeek} • {slot.timeSlot}</div>
                                                <div style={{ color: '#666', fontSize: '0.9rem' }}>{slot.subject} (Room: {slot.roomNumber})</div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        ) : null}
                    </div>
                </div>
            )}

            {/* Course Assignment Modal */}
            {assignModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '600px' }}>
                        <div className="modal-header">
                            <h2>Manage Assignments: {assignModal.facultyName}</h2>
                            <button className="modal-close" onClick={() => setAssignModal(null)}>×</button>
                        </div>
                        <div style={{ padding: '20px' }}>
                            {assignError && <div className="alert alert-danger" style={{ marginBottom: '15px' }}>{assignError}</div>}
                            {assignSuccess && <div className="alert alert-success" style={{ marginBottom: '15px' }}>{assignSuccess}</div>}

                            {/* Conflict Warning */}
                            {conflictInfo && conflictInfo.length > 0 && (
                                <div style={{ background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '8px', padding: '12px', marginBottom: '15px' }}>
                                    <div style={{ fontWeight: 'bold', color: '#dc2626', marginBottom: '8px' }}>Time Conflict Detected</div>
                                    {conflictInfo.map((c, i) => (
                                        <div key={i} style={{ fontSize: '0.9rem', color: '#991b1b' }}>
                                            Faculty is already busy on {c.dayOfWeek} at {c.timeSlot} {c.existingSubject && `(${c.existingSubject})`}
                                        </div>
                                    ))}
                                </div>
                            )}

                            {assignLoading ? (
                                <div style={{ textAlign: 'center', padding: '20px' }}>Loading...</div>
                            ) : (
                                <>
                                    {/* Current Assignments */}
                                    <div style={{ marginBottom: '20px' }}>
                                        <h4 style={{ marginTop: 0 }}>Current Assignments ({currentCourses.length})</h4>
                                        {currentCourses.length === 0 ? (
                                            <p style={{ color: '#666', fontStyle: 'italic' }}>No courses assigned.</p>
                                        ) : (
                                            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                                {currentCourses.map(c => (
                                                    <div key={c.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 12px', background: '#f0fdf4', borderRadius: '8px', border: '1px solid #bbf7d0' }}>
                                                        <div>
                                                            <strong>{c.code}</strong> - {c.name}
                                                            {c.specialization ? <span style={{ marginLeft: '6px', fontSize: '0.78rem', color: '#6b46c1' }}>[{c.specialization}]</span> : null}
                                                            <span style={{ marginLeft: '8px', fontSize: '0.85rem', color: '#666' }}>({c.credits} credits)</span>
                                                        </div>
                                                        <button className="btn btn-danger btn-sm" onClick={() => handleUnassign(c)}>Unassign</button>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>

                                    {/* Assign New Course */}
                                    <div style={{ borderTop: '1px solid #e2e8f0', paddingTop: '20px' }}>
                                        <h4 style={{ marginTop: 0 }}>Assign New Course</h4>
                                        <div style={{ display: 'flex', gap: '10px', alignItems: 'flex-end' }}>
                                            <div style={{ flex: 1 }}>
                                                <select
                                                    className="form-control"
                                                    value={selectedCourse?.id || ''}
                                                    onChange={e => {
                                                        const course = availableCourses.find(c => c.id === parseInt(e.target.value));
                                                        setSelectedCourse(course || null);
                                                        setConflictInfo(null);
                                                    }}
                                                >
                                                    <option value="">Select a course...</option>
                                                    {availableCourses.map(c => {
                                                        const isRecommended = assignModal.department === c.department || (c.specialization && c.specialization === (assignModal.specialization || ''));
                                                        return (
                                                            <option key={c.id} value={c.id}>
                                                                {c.code} - {c.name}{c.specialization ? ` [${c.specialization}]` : ''} ({c.credits} cr)
                                                                {isRecommended ? ' ★ Recommended' : ''}
                                                            </option>
                                                        );
                                                    })}
                                                </select>
                                            </div>
                                            <button className="btn btn-secondary btn-sm" onClick={handleSuggestFit} style={{ whiteSpace: 'nowrap' }}>
                                                Suggest Fit
                                            </button>
                                            <button
                                                className="btn btn-primary btn-sm"
                                                onClick={handleAssign}
                                                disabled={!selectedCourse || assignLoading}
                                                style={{ whiteSpace: 'nowrap' }}
                                            >
                                                Assign
                                            </button>
                                        </div>
                                        {availableCourses.length === 0 && (
                                            <p style={{ color: '#666', fontSize: '0.85rem', marginTop: '8px' }}>No unassigned courses available.</p>
                                        )}
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default FacultyWorkloadPage;
