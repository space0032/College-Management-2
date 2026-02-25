import React, { useState, useEffect } from 'react';
import {
    getScholarships, createScholarship, applyForScholarship,
    getApplications, updateApplicationStatus
} from '../services/scholarshipService';

const ScholarshipPage = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [scholarships, setScholarships] = useState([]);
    const [selectedScholarship, setSelectedScholarship] = useState(null);
    const [applications, setApplications] = useState([]);

    // Forms
    const [scholarshipForm, setScholarshipForm] = useState({ title: '', description: '', amount: '', donorName: '', status: 'OPEN' });
    const [statement, setStatement] = useState('');

    // Simulating logged-in student for applying
    const studentId = parseInt(localStorage.getItem('userId') || '1');
    const userRole = localStorage.getItem('userRole') || 'STUDENT'; // Admin vs Student view

    useEffect(() => {
        loadScholarships();
    }, [activeTab]);

    const loadScholarships = async () => {
        try {
            const res = await getScholarships();
            setScholarships(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        try {
            await createScholarship({
                ...scholarshipForm,
                amount: parseFloat(scholarshipForm.amount),
                createdBy: parseInt(localStorage.getItem('userId') || '1')
            });
            setScholarshipForm({ title: '', description: '', amount: '', donorName: '', status: 'OPEN' });
            setActiveTab('browse');
        } catch (err) {
            alert('Failed to create scholarship');
        }
    };

    const handleApplyClick = (s) => {
        setSelectedScholarship(s);
        setActiveTab('apply');
    };

    const handleSubmitApplication = async (e) => {
        e.preventDefault();
        try {
            await applyForScholarship(selectedScholarship.id, {
                studentId: studentId,
                statement: statement,
                status: 'APPLIED'
            });
            alert('Application submitted successfully!');
            setStatement('');
            setSelectedScholarship(null);
            setActiveTab('browse');
        } catch (err) {
            alert('Failed to submit application');
        }
    };

    // Admin specific
    const handleViewApplications = async (s) => {
        setSelectedScholarship(s);
        try {
            const res = await getApplications(s.id);
            setApplications(res.data || []);
            setActiveTab('manage');
        } catch (err) {
            alert('Failed to fetch applications');
        }
    };

    const handleUpdateStatus = async (appId, newStatus) => {
        try {
            await updateApplicationStatus(selectedScholarship.id, appId, newStatus);
            // Refresh list
            const res = await getApplications(selectedScholarship.id);
            setApplications(res.data || []);
        } catch (err) {
            alert('Failed to update status');
        }
    };

    return (
        <div className="page-container">
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between' }}>
                <h2>Scholarships & Grants</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('browse')}>
                        Browse Scholarships
                    </button>

                    {userRole !== 'STUDENT' && (
                        <button className={`btn ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('create')}>
                            Create Scholarship
                        </button>
                    )}

                    {activeTab === 'manage' && (
                        <button className="btn btn-primary">Reviewing: {selectedScholarship?.title}</button>
                    )}
                    {activeTab === 'apply' && (
                        <button className="btn btn-primary">Applying: {selectedScholarship?.title}</button>
                    )}
                </div>
            </div>

            {activeTab === 'browse' && (
                <div className="card-grid">
                    {scholarships.map(s => (
                        <div key={s.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                <h3 style={{ margin: 0, color: 'var(--primary-color)' }}>{s.title}</h3>
                                <span className={`badge badge-${s.status === 'OPEN' ? 'success' : 'secondary'}`}>{s.status}</span>
                            </div>
                            <h2 style={{ margin: '10px 0', fontSize: '1.5rem' }}>₹{s.amount.toLocaleString()}</h2>
                            <p style={{ color: 'var(--text-muted)', flex: 1 }}>{s.description}</p>
                            <div style={{ marginTop: '15px', color: '#666', fontSize: '0.9rem' }}>
                                <i className="fas fa-gift"></i> Sponsor: {s.donorName || 'College Trust'}
                            </div>
                            <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
                                {s.status === 'OPEN' && userRole === 'STUDENT' && (
                                    <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => handleApplyClick(s)}>Apply Now</button>
                                )}
                                {userRole !== 'STUDENT' && (
                                    <button className="btn btn-secondary" style={{ flex: 1 }} onClick={() => handleViewApplications(s)}>Review Candidates</button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {activeTab === 'apply' && selectedScholarship && (
                <div className="form-container" style={{ maxWidth: '800px', margin: '0 auto' }}>
                    <div className="stat-card">
                        <button className="btn btn-secondary btn-sm" onClick={() => setActiveTab('browse')} style={{ marginBottom: '15px' }}>&larr; Back to Browse</button>
                        <h2>{selectedScholarship.title}</h2>
                        <p className="badge badge-primary">Grant Amount: ₹{selectedScholarship.amount.toLocaleString()}</p>
                        <p style={{ marginTop: '15px' }}>{selectedScholarship.description}</p>

                        <hr style={{ margin: '20px 0', borderColor: 'var(--border-color)' }} />

                        <form onSubmit={handleSubmitApplication}>
                            <div className="form-group">
                                <label>Personal Statement (Max 500 words) *</label>
                                <textarea
                                    rows="8"
                                    required
                                    placeholder="Explain why you are a strong candidate for this scholarship..."
                                    value={statement}
                                    onChange={e => setStatement(e.target.value)}
                                ></textarea>
                                <small style={{ color: 'var(--text-muted)' }}>Include details about your academic achievements and financial need.</small>
                            </div>
                            <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '12px', fontSize: '1.1rem' }}>Submit Application</button>
                        </form>
                    </div>
                </div>
            )}

            {activeTab === 'manage' && selectedScholarship && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div className="stat-card">
                        <button className="btn btn-secondary btn-sm" onClick={() => setActiveTab('browse')} style={{ marginBottom: '15px' }}>&larr; Back to Scholarships</button>
                        <h3>Applicants for {selectedScholarship.title}</h3>
                    </div>

                    <div className="data-table-container">
                        <table className="data-table">
                            <thead><tr><th>Applicant</th><th>Statement</th><th>Status</th><th>Actions</th></tr></thead>
                            <tbody>
                                {applications.length === 0 ? <tr><td colSpan="4" style={{ textAlign: 'center' }}>No applications yet.</td></tr> :
                                    applications.map(app => (
                                        <tr key={app.id}>
                                            <td><strong>{app.studentName}</strong><br /><small>ID: {app.studentId}</small></td>
                                            <td style={{ maxWidth: '400px', whiteSpace: 'pre-wrap' }}>{app.statement}</td>
                                            <td>
                                                <span className={`badge badge-${app.status === 'APPROVED' ? 'success' : app.status === 'REJECTED' ? 'danger' : 'warning'}`}>
                                                    {app.status}
                                                </span>
                                            </td>
                                            <td>
                                                {app.status === 'APPLIED' && (
                                                    <div style={{ display: 'flex', gap: '5px' }}>
                                                        <button className="btn btn-success btn-sm" onClick={() => handleUpdateStatus(app.id, 'APPROVED')}>Approve</button>
                                                        <button className="btn btn-danger btn-sm" onClick={() => handleUpdateStatus(app.id, 'REJECTED')}>Reject</button>
                                                    </div>
                                                )}
                                            </td>
                                        </tr>
                                    ))
                                }
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {activeTab === 'create' && userRole !== 'STUDENT' && (
                <div className="form-container" style={{ maxWidth: '600px', margin: '0 auto' }}>
                    <div className="stat-card">
                        <h3>Publish New Scholarship</h3>
                        <form className="form-grid" onSubmit={handleCreate} style={{ marginTop: '20px' }}>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Scholarship Title *</label>
                                <input required type="text" value={scholarshipForm.title} onChange={e => setScholarshipForm({ ...scholarshipForm, title: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Grant Amount (₹) *</label>
                                <input required type="number" min="0" value={scholarshipForm.amount} onChange={e => setScholarshipForm({ ...scholarshipForm, amount: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Sponsor/Donor Name</label>
                                <input type="text" value={scholarshipForm.donorName} onChange={e => setScholarshipForm({ ...scholarshipForm, donorName: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Eligibility & Description *</label>
                                <textarea required rows="4" value={scholarshipForm.description} onChange={e => setScholarshipForm({ ...scholarshipForm, description: e.target.value })}></textarea>
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Publish Scholarship</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

        </div>
    );
};

export default ScholarshipPage;
