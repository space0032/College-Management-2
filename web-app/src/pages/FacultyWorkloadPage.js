import React, { useState, useEffect } from 'react';
import { getWorkloadAnalytics, getFacultyWorkload } from '../services/workloadService';
import { PieChart, Pie, Cell, Tooltip, BarChart, Bar, XAxis, YAxis, ResponsiveContainer } from 'recharts';

const FacultyWorkloadPage = () => {
    const [analytics, setAnalytics] = useState([]);
    const [selectedFaculty, setSelectedFaculty] = useState(null);
    const [facultyDetails, setFacultyDetails] = useState(null);
    const [loading, setLoading] = useState(true);
    const [detailsLoading, setDetailsLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchAnalytics();
    }, []);

    const fetchAnalytics = async () => {
        setLoading(true);
        try {
            const res = await getWorkloadAnalytics();
            // Filter out faculty with 0 classes if we only want active workloads
            const activeLoad = (res.data || []).filter(f => f.totalClasses > 0);
            setAnalytics(activeLoad);
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
        try {
            const res = await getFacultyWorkload(facultyName);
            setFacultyDetails(res.data);
        } catch (err) {
            console.error(err);
            alert('Failed to load faculty details');
        } finally {
            setDetailsLoading(false);
        }
    };

    const CHART_COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];


    if (loading) return <div className="page-container">Loading Workload Analytics...</div>;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>👨‍🏫 Faculty Workload</h2>
                    <p className="text-muted">Analyze teaching hours, credit assignments, and schedule distributions.</p>
                </div>
            </div>

            {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

            <div className="data-table-container">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Faculty Name</th>
                            <th>Department</th>
                            <th>Total Classes/Week</th>
                            <th>Unique Subjects</th>
                            <th>Relative Load</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {analytics.length === 0 ? (
                            <tr><td colSpan="6" style={{ textAlign: 'center' }}>No active teaching loads found.</td></tr>
                        ) : (
                            analytics.map(f => (
                                <tr key={f.facultyId}>
                                    <td style={{ fontWeight: '500' }}>{f.facultyName}</td>
                                    <td>{f.department}</td>
                                    <td>{f.totalClasses}</td>
                                    <td>{f.uniqueSubjects}</td>
                                    <td>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                            <div style={{ flex: 1, backgroundColor: '#e0e0e0', height: '8px', borderRadius: '4px', overflow: 'hidden' }}>
                                                <div style={{
                                                    width: `${f.loadPercentage}%`,
                                                    backgroundColor: f.loadPercentage > 80 ? '#f44336' : f.loadPercentage > 50 ? '#ff9800' : '#4caf50',
                                                    height: '100%'
                                                }}></div>
                                            </div>
                                            <span style={{ fontSize: '0.8rem', width: '30px' }}>{f.loadPercentage}%</span>
                                        </div>
                                    </td>
                                    <td>
                                        <button className="btn btn-primary btn-sm" onClick={() => handleSelectFaculty(f.facultyName)}>
                                            View Details
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {selectedFaculty && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '800px' }}>
                        <div className="modal-header">
                            <h2>Workload Report: {selectedFaculty}</h2>
                            <button className="modal-close" onClick={() => setSelectedFaculty(null)}>×</button>
                        </div>

                        {detailsLoading ? (
                            <div style={{ padding: '20px', textAlign: 'center' }}>Loading distribution...</div>
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
                                                <div style={{ color: '#888', fontSize: '0.8rem' }}>Sem {slot.semester} • {slot.department}</div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        ) : null}
                    </div>
                </div>
            )}
        </div>
    );
};

export default FacultyWorkloadPage;
