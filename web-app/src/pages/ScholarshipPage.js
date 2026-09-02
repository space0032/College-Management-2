import SessionManager from '../utils/SessionManager';
import React, { useState, useEffect } from 'react';
import {
    getScholarships, createScholarship, applyForScholarship,
    getApplications, updateApplicationStatus
} from '../services/scholarshipService';

const ScholarshipPage = () => {
    const [, setIsLoading] = useState(false);
    const [activeTab, setActiveTab] = useState('browse');
    const [scholarships, setScholarships] = useState([]);
    const [selectedScholarship, setSelectedScholarship] = useState(null);
    const [applications, setApplications] = useState([]);


    // Forms
    const [scholarshipForm, setScholarshipForm] = useState({ title: '', description: '', amount: '', donorName: '', status: 'OPEN' });
    const [statement, setStatement] = useState('');

    const studentId = SessionManager.getUserId();
    const userRole = SessionManager.getUserRole() || 'STUDENT';
    const totalAwardValue = scholarships.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    const averageAward = scholarships.length ? totalAwardValue / scholarships.length : 0;

    useEffect(() => {
        loadScholarships();
    }, [activeTab]);

    const loadScholarships = async () => {
        setIsLoading(true);
        try {
            const res = await getScholarships();
            setScholarships(res.data || []);
        } catch (err) { console.error(err); }
        finally { setIsLoading(false); }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        try {
            await createScholarship({
                ...scholarshipForm,
                amount: parseFloat(scholarshipForm.amount),
                createdBy: studentId
            });
            setScholarshipForm({ title: '', description: '', amount: '', donorName: '', status: 'OPEN' });
            setActiveTab('browse');
        } catch (err) { alert('Creation failed'); }
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
            alert('Application archived for committee review.');
            setStatement('');
            setSelectedScholarship(null);
            setActiveTab('browse');
        } catch (err) { alert('Submission failed'); }
    };

    const handleViewApplications = async (s) => {
        setSelectedScholarship(s);
        try {
            const res = await getApplications(s.id);
            setApplications(res.data || []);
            setActiveTab('manage');
        } catch (err) { alert('Fetch failed'); }
    };

    const handleUpdateStatus = async (appId, newStatus) => {
        try {
            await updateApplicationStatus(selectedScholarship.id, appId, newStatus);
            const res = await getApplications(selectedScholarship.id);
            setApplications(res.data || []);
        } catch (err) { alert('Update failed'); }
    };

    return (
        <div className="page-container" style={{ background: '#f0f2f5', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h1 className="page-title">🎓 Grants & Foundations</h1>
                        <p className="page-subtitle">Academic empowerment through financial aid and merit-based sponsorships</p>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <button className={`btn btn-sm ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('browse')}>Archive</button>
                        {userRole !== 'STUDENT' && (
                            <button className={`btn btn-sm ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('create')}>+ New Grant</button>
                        )}
                    </div>
                </div>
            </div>

            {/* Premium Stats Row */}
            {activeTab === 'browse' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '30px' }}>
                    <div className="stat-card" style={{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white' }}>
                        <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Total Award Value</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>₹{totalAwardValue.toLocaleString('en-IN')}</div>
                        <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Across loaded grants</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Active Grants</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#1e293b', margin: '8px 0' }}>{scholarships.length}</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Avg. Award</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3b82f6', margin: '8px 0' }}>₹{Math.round(averageAward).toLocaleString('en-IN')}</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Beneficiaries</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#8b5cf6', margin: '8px 0' }}>N/A</div>
                    </div>
                </div>
            )}

            {activeTab === 'browse' && (
                <div className="card-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '25px' }}>
                    {scholarships.map(s => (
                        <div key={s.id} className="stat-card" style={{ padding: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column', transition: 'transform 0.2s' }}>
                            <div style={{ padding: '25px', display: 'flex', flexDirection: 'column', flex: 1 }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                                    <span className="badge" style={{ background: '#ecfdf5', color: '#047857', fontSize: '0.7rem' }}>{s.donorName || 'College Trust'}</span>
                                    <span className={`badge ${s.status === 'OPEN' ? 'badge-success' : 'badge-secondary'}`} style={{ fontSize: '0.65rem' }}>{s.status}</span>
                                </div>
                                <h3 style={{ margin: '0 0 10px 0', fontSize: '1.25rem', color: '#1e293b' }}>{s.title}</h3>
                                <div style={{ fontSize: '2rem', fontWeight: '800', color: '#1e293b', marginBottom: '15px' }}>₹{s.amount.toLocaleString()}</div>
                                <p style={{ color: '#64748b', fontSize: '0.9rem', flex: 1, lineHeight: '1.6' }}>{s.description}</p>
                            </div>
                            <div style={{ padding: '20px', background: '#f8fafc', borderTop: '1px solid #f1f5f9' }}>
                                {s.status === 'OPEN' && userRole === 'STUDENT' && (
                                    <button className="btn btn-primary" style={{ width: '100%', padding: '12px' }} onClick={() => handleApplyClick(s)}>Start Application</button>
                                )}
                                {userRole !== 'STUDENT' && (
                                    <button className="btn btn-secondary" style={{ width: '100%', padding: '12px' }} onClick={() => handleViewApplications(s)}>Review Candidates</button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {activeTab === 'apply' && selectedScholarship && (
                <div style={{ maxWidth: '900px', margin: '0 auto' }}>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '30px' }}>
                        <div className="stat-card" style={{ height: 'fit-content' }}>
                            <button className="btn btn-sm btn-secondary" onClick={() => setActiveTab('browse')} style={{ marginBottom: '20px' }}>&larr; Exit</button>
                            <h4 style={{ marginBottom: '10px' }}>Scholarship Summary</h4>
                            <h2 style={{ fontSize: '1.5rem', marginBottom: '15px' }}>{selectedScholarship.title}</h2>
                            <div style={{ padding: '15px', background: '#f0f9ff', borderRadius: '12px', marginBottom: '20px' }}>
                                <div style={{ fontSize: '0.75rem', color: '#0c4a6e' }}>AWARD AMOUNT</div>
                                <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#0369a1' }}>₹{selectedScholarship.amount.toLocaleString()}</div>
                            </div>
                            <p style={{ fontSize: '0.85rem', color: '#64748b' }}>{selectedScholarship.description}</p>
                        </div>

                        <div className="stat-card" style={{ padding: '40px' }}>
                            <h3 style={{ marginBottom: '25px', borderBottom: '1px solid #f1f5f9', paddingBottom: '15px' }}>Academic Proposal</h3>
                            <form onSubmit={handleSubmitApplication}>
                                <div className="form-group">
                                    <label>Personal Statement (Financial & Academic Context) *</label>
                                    <textarea
                                        rows="12"
                                        required
                                        className="form-control"
                                        placeholder="Articulate your academic journey, future goals, and how this grant will facilitate your education..."
                                        value={statement}
                                        onChange={e => setStatement(e.target.value)}
                                        style={{ background: '#fdfdfd' }}
                                    ></textarea>
                                    <div style={{ marginTop: '10px', fontSize: '0.75rem', color: '#94a3b8', textAlign: 'right' }}>
                                        {statement.split(/\s+/).filter(Boolean).length} / 500 words
                                    </div>
                                </div>
                                <div style={{ display: 'flex', gap: '15px', marginTop: '20px' }}>
                                    <button type="button" className="btn btn-secondary" onClick={() => setActiveTab('browse')} style={{ flex: 1 }}>Discard</button>
                                    <button type="submit" className="btn btn-primary" style={{ flex: 2, padding: '15px' }}>Lock & Submit</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            )}

            {activeTab === 'manage' && selectedScholarship && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '25px' }}>
                    <div className="stat-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <button className="btn btn-sm btn-secondary" onClick={() => setActiveTab('browse')} style={{ marginBottom: '10px' }}>&larr; Back</button>
                            <h3 style={{ margin: 0 }}>Reviewing Candidates: {selectedScholarship.title}</h3>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>POOL SIZE</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{applications.length} Applicants</div>
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))', gap: '25px' }}>
                        {applications.length === 0 ? (
                            <div className="stat-card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '100px', color: '#94a3b8' }}>
                                <div style={{ fontSize: '3rem', marginBottom: '15px' }}>📂</div>
                                <p>No applications have been submitted for this grant yet.</p>
                            </div>
                        ) : (
                            applications.map(app => (
                                <div key={app.id} className="stat-card" style={{ borderLeft: `6px solid ${app.status === 'APPROVED' ? '#10b981' : app.status === 'REJECTED' ? '#ef4444' : '#f59e0b'}` }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                                        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                                            <div style={{ width: '40px', height: '40px', background: '#3b82f6', color: 'white', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
                                                {app.studentName.charAt(0)}
                                            </div>
                                            <div>
                                                <h4 style={{ margin: 0 }}>{app.studentName}</h4>
                                                <div style={{ fontSize: '0.75rem', color: '#64748b' }}>ID: {app.studentId}</div>
                                            </div>
                                        </div>
                                        <span className={`badge ${app.status === 'APPROVED' ? 'badge-success' : app.status === 'REJECTED' ? 'badge-danger' : 'badge-warning'}`}>
                                            {app.status}
                                        </span>
                                    </div>
                                    <div style={{ background: '#f8fafc', padding: '15px', borderRadius: '12px', fontSize: '0.85rem', color: '#475569', minHeight: '120px', lineHeight: '1.6', marginBottom: '20px' }}>
                                        {app.statement}
                                    </div>
                                    {app.status === 'APPLIED' && (
                                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                            <button className="btn btn-success" onClick={() => handleUpdateStatus(app.id, 'APPROVED')}>Authorize Disbursement</button>
                                            <button className="btn btn-danger" onClick={() => handleUpdateStatus(app.id, 'REJECTED')}>Reject</button>
                                        </div>
                                    )}
                                </div>
                            ))
                        )}
                    </div>
                </div>
            )}

            {activeTab === 'create' && userRole !== 'STUDENT' && (
                <div style={{ maxWidth: '600px', margin: '0 auto' }}>
                    <div className="stat-card" style={{ padding: '40px' }}>
                        <h2 style={{ marginBottom: '10px' }}>Grant Architecture</h2>
                        <p style={{ color: '#64748b', marginBottom: '30px', fontSize: '0.9rem' }}>Configure the eligibility and financial parameters for this institutional merit grant.</p>
                        <form className="form-grid" onSubmit={handleCreate}>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Grant Title *</label>
                                <input required className="form-control" type="text" value={scholarshipForm.title} onChange={e => setScholarshipForm({ ...scholarshipForm, title: e.target.value })} placeholder="e.g. Dean's List Merit Grant" />
                            </div>
                            <div className="form-group">
                                <label>Award Amount (₹) *</label>
                                <input required className="form-control" type="number" min="0" value={scholarshipForm.amount} onChange={e => setScholarshipForm({ ...scholarshipForm, amount: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Underwriting Sponsor</label>
                                <input className="form-control" type="text" value={scholarshipForm.donorName} onChange={e => setScholarshipForm({ ...scholarshipForm, donorName: e.target.value })} placeholder="e.g. Alumni Association" />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Eligibility Framework *</label>
                                <textarea required className="form-control" rows="5" value={scholarshipForm.description} onChange={e => setScholarshipForm({ ...scholarshipForm, description: e.target.value })} placeholder="Define GPA requirements, department constraints, etc."></textarea>
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '15px' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '15px', fontWeight: 'bold' }}>Publish Grant</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ScholarshipPage;
