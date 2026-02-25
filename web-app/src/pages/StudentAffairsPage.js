import React, { useState } from 'react';

const StudentAffairsPage = () => {
    const [activeTab, setActiveTab] = useState('scholarships');

    const tabs = [
        { id: 'scholarships', label: 'Scholarships', icon: '🎓' },
        { id: 'discipline', label: 'Disciplinary Records', icon: '⚖️' },
        { id: 'grievance', label: 'Grievance System', icon: '📢' },
        { id: 'parents', label: 'Parent Communication', icon: '👪' },
        { id: 'wellness', label: 'Wellness & Health', icon: '🏥' },
        { id: 'career', label: 'Career Services', icon: '💼' },
        { id: 'housing', label: 'Housing & Residential', icon: '🏠' },
        { id: 'gov', label: 'Student Government', icon: '🗳️' },
        { id: 'activities', label: 'Extracurriculars', icon: '🎭' }
    ];

    const renderContent = () => {
        switch (activeTab) {
            case 'scholarships':
                return (
                    <div className="stat-card">
                        <h3>Active Scholarships</h3>
                        <p className="text-muted">Manage merit-based and need-based financial awards.</p>
                        <div className="data-table-container" style={{ marginTop: '20px' }}>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Award Name</th>
                                        <th>Recipient</th>
                                        <th>Amount</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>Presidential Merit</td>
                                        <td>Alice Student</td>
                                        <td>$5,000</td>
                                        <td><span className="badge badge-success">Disbursed</span></td>
                                    </tr>
                                    <tr>
                                        <td>STEM Excellence</td>
                                        <td>Boby Student</td>
                                        <td>$2,500</td>
                                        <td><span className="badge badge-warning">Pending Review</span></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                );
            case 'discipline':
                return (
                    <div className="stat-card" style={{ borderTop: '4px solid #ef4444' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <div>
                                <h3>⚖️ Disciplinary Records</h3>
                                <p className="text-muted">Tracking behavioral incidents, violations, and resolutions.</p>
                            </div>
                            <button className="btn btn-primary">+ Log Incident</button>
                        </div>
                        <div className="data-table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Student</th>
                                        <th>Date</th>
                                        <th>Violation Type</th>
                                        <th>Severity</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>#DISC-104</td>
                                        <td>S2023-401 (John Doe)</td>
                                        <td>Oct 12, 2024</td>
                                        <td>Plagiarism</td>
                                        <td><span className="badge badge-danger">High</span></td>
                                        <td><span className="badge badge-warning">Under Review</span></td>
                                        <td><button className="btn-icon" title="View Details">👁️</button></td>
                                    </tr>
                                    <tr>
                                        <td>#DISC-103</td>
                                        <td>S2023-112 (Jane Smith)</td>
                                        <td>Sep 28, 2024</td>
                                        <td>Hostel Curfew Breach</td>
                                        <td><span className="badge badge-warning" style={{ background: '#fef3c7', color: '#d97706' }}>Medium</span></td>
                                        <td><span className="badge badge-success">Resolved</span></td>
                                        <td><button className="btn-icon" title="View Details">👁️</button></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                );
            case 'grievance':
                return (
                    <div className="stat-card" style={{ borderTop: '4px solid #3b82f6' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <div>
                                <h3>📢 Grievance System</h3>
                                <p className="text-muted">Anonymous and identified complaints and feedback tickets.</p>
                            </div>
                        </div>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '20px' }}>
                            <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '15px', background: '#f8fafc' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                                    <span style={{ fontWeight: 'bold' }}>TKT-8842</span>
                                    <span className="badge badge-warning">Open</span>
                                </div>
                                <h4 style={{ margin: '0 0 10px 0' }}>Heating issue in Library Wing B</h4>
                                <p style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '15px' }}>Reported by: Anonymous Student<br />Date: Oct 20, 2024</p>
                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <button className="btn btn-primary btn-sm" style={{ flex: 1 }}>Assign to Maintenance</button>
                                </div>
                            </div>
                            <div style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '15px', background: '#f8fafc' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
                                    <span style={{ fontWeight: 'bold' }}>TKT-8841</span>
                                    <span className="badge badge-success">Resolved</span>
                                </div>
                                <h4 style={{ margin: '0 0 10px 0' }}>WiFi disconnection in CS Lab 3</h4>
                                <p style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '15px' }}>Reported by: S2022-819<br />Date: Oct 18, 2024</p>
                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <button className="btn btn-secondary btn-sm" style={{ flex: 1 }}>View Resolution</button>
                                </div>
                            </div>
                        </div>
                    </div>
                );
            case 'parents':
                return (
                    <div className="stat-card" style={{ borderTop: '4px solid #10b981' }}>
                        <h3>👪 Parent Communication Portal</h3>
                        <p className="text-muted">Send broadcasts, academic alerts, and conduct notices to parents/guardians.</p>

                        <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
                            <div style={{ flex: 1, padding: '20px', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                                <h4>Compose New Message</h4>
                                <div className="form-group" style={{ marginTop: '15px' }}>
                                    <label>Recipient Group</label>
                                    <select className="form-control">
                                        <option>All Parents (Broadcast)</option>
                                        <option>Year 1 Parents</option>
                                        <option>Specific Student's Guardian</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Subject</label>
                                    <input type="text" className="form-control" placeholder="e.g. End of Semester Updates" />
                                </div>
                                <div className="form-group">
                                    <label>Message</label>
                                    <textarea className="form-control" rows="4" placeholder="Type your message here..."></textarea>
                                </div>
                                <button className="btn btn-primary" style={{ width: '100%' }}>✉️ Send Communication</button>
                            </div>

                            <div style={{ flex: 1 }}>
                                <h4>Recent Communications</h4>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '15px' }}>
                                    <div style={{ padding: '12px', border: '1px solid #e2e8f0', borderRadius: '6px', borderLeft: '4px solid #3b82f6' }}>
                                        <div style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Campus Closure Notice - Diwali Holidays</div>
                                        <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>Sent to: All Parents • Oct 15, 2024</div>
                                    </div>
                                    <div style={{ padding: '12px', border: '1px solid #e2e8f0', borderRadius: '6px', borderLeft: '4px solid #10b981' }}>
                                        <div style={{ fontWeight: 'bold', fontSize: '0.9rem' }}>Mid-Term Grade Reports Available</div>
                                        <div style={{ fontSize: '0.8rem', color: '#64748b', marginTop: '4px' }}>Sent to: All Parents • Oct 05, 2024</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                );
            case 'wellness':
                return (
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        <div className="stat-card">
                            <h3>🏥 Campus Clinic Activity</h3>
                            <div style={{ fontSize: '2rem', fontWeight: 'bold', margin: '15px 0' }}>14</div>
                            <p className="text-muted">Appointments scheduled for today</p>
                        </div>
                        <div className="stat-card">
                            <h3>🧠 Counseling Sessions</h3>
                            <div style={{ fontSize: '2rem', fontWeight: 'bold', margin: '15px 0' }}>8</div>
                            <p className="text-muted">Wellness check-ins completed this week</p>
                        </div>
                    </div>
                );
            default:
                return (
                    <div className="stat-card" style={{ textAlign: 'center', padding: '100px' }}>
                        <div style={{ fontSize: '4rem', marginBottom: '20px' }}>🚧</div>
                        <h2>{tabs.find(t => t.id === activeTab)?.label} Module</h2>
                        <p className="text-muted">This management sub-system is currently being synchronized with the main ledger.</p>
                    </div>
                );
        }
    };

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">🏛️ Student Affairs Management</h1>
                    <p className="page-subtitle">Consolidated administrative portal for non-academic student life and wellness</p>
                </div>
            </div>

            <div style={{
                display: 'flex',
                gap: '10px',
                overflowX: 'auto',
                paddingBottom: '15px',
                marginBottom: '25px',
                borderBottom: '1px solid #e2e8f0'
            }}>
                {tabs.map(tab => (
                    <button
                        key={tab.id}
                        className={`btn btn-sm ${activeTab === tab.id ? 'btn-primary' : ''}`}
                        style={{
                            whiteSpace: 'nowrap',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            background: activeTab === tab.id ? '' : 'white',
                            color: activeTab === tab.id ? '' : '#475569',
                            border: activeTab === tab.id ? '' : '1px solid #e2e8f0',
                            padding: '10px 18px'
                        }}
                        onClick={() => setActiveTab(tab.id)}
                    >
                        <span>{tab.icon}</span> {tab.label}
                    </button>
                ))}
            </div>

            <div className="content-area">
                {renderContent()}
            </div>
        </div>
    );
};

export default StudentAffairsPage;
