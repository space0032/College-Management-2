import React, { useState, useEffect } from 'react';
import { getWorkloadAnalytics, getFacultyWorkload } from '../services/workloadService';

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

            // Wait for modal to render before drawing pie chart
            setTimeout(() => drawPieChart(res.data.distribution), 100);
        } catch (err) {
            console.error(err);
            alert('Failed to load faculty details');
        } finally {
            setDetailsLoading(false);
        }
    };

    const drawPieChart = (distribution) => {
        const canvas = document.getElementById('workloadPieChart');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        const colors = ['#2196F3', '#4CAF50', '#FFC107', '#E91E63', '#9C27B0', '#00BCD4'];

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        const total = distribution.reduce((sum, item) => sum + item.hours, 0);
        let startAngle = 0;
        const centerX = canvas.width / 2;
        const centerY = canvas.height / 2;
        const radius = Math.min(centerX, centerY) - 10;

        if (total === 0) {
            ctx.fillStyle = '#ddd';
            ctx.beginPath();
            ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI);
            ctx.fill();
            return;
        }

        distribution.forEach((item, index) => {
            const sliceAngle = (item.hours / total) * 2 * Math.PI;

            ctx.fillStyle = colors[index % colors.length];
            ctx.beginPath();
            ctx.moveTo(centerX, centerY);
            ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
            ctx.closePath();
            ctx.fill();

            startAngle += sliceAngle;
        });
    };

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
                                    <div style={{ display: 'flex', justifyContent: 'center', margin: '20px 0' }}>
                                        <canvas id="workloadPieChart" width="200" height="200"></canvas>
                                    </div>

                                    <ul style={{ listStyle: 'none', padding: 0 }}>
                                        {facultyDetails.distribution.map((d, i) => {
                                            const colors = ['#2196F3', '#4CAF50', '#FFC107', '#E91E63', '#9C27B0', '#00BCD4'];
                                            return (
                                                <li key={d.subject} style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', padding: '5px', backgroundColor: '#f9f9f9', borderRadius: '4px' }}>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                                        <span style={{ display: 'inline-block', width: '12px', height: '12px', borderRadius: '50%', backgroundColor: colors[i % colors.length] }}></span>
                                                        <span>{d.subject}</span>
                                                    </div>
                                                    <span style={{ fontWeight: 'bold' }}>{d.hours} hrs</span>
                                                </li>
                                            )
                                        })}
                                    </ul>
                                </div>
                                <div style={{ flex: 1 }}>
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
