import React, { useState } from 'react';

const StudentAffairsPage = () => {
    const [activeTab, setActiveTab] = useState('scholarships');

    const tabs = [
        { id: 'scholarships', label: 'Scholarships', icon: '🎓' },
        { id: 'discipline', label: 'Conduct & Discipline', icon: '⚖️' },
        { id: 'wellness', label: 'Wellness & Health', icon: '🏥' },
        { id: 'career', label: 'Career Services', icon: '💼' },
        { id: 'housing', label: 'Housing & Residential', icon: '🏠' },
        { id: 'gov', label: 'Student Government', icon: '🗳️' },
        { id: 'aid', label: 'Financial Aid', icon: '💰' },
        { id: 'activities', label: 'Extracurriculars', icon: '🎭' },
        { id: 'international', label: 'International Student Office', icon: '🌍' }
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
                    <div className="stat-card" style={{ borderLeft: '4px solid #ef4444' }}>
                        <h3>Conduct & Grievance Registry</h3>
                        <p className="text-muted">Tracking behavioral incidents and resolution statuses.</p>
                        <div style={{ padding: '40px', textAlign: 'center', opacity: 0.5 }}>
                            <div style={{ fontSize: '3rem' }}>⚖️</div>
                            <p>No major conduct violations reported in the current term.</p>
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
