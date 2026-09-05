import SessionManager from '../utils/SessionManager';
import React, { useState, useEffect } from 'react';
import {
    getScholarships, createScholarship, applyForScholarship,
    getApplications, updateApplicationStatus
} from '../services/scholarshipService';
import Modal from '../components/Modal';
import { toast } from '../components/Toast';
import { getErrorMessage, getSuccessRefId } from '../utils/error';
import { SkeletonCards } from '../components/Skeleton';

const ScholarshipPage = () => {
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState('');
    const [activeTab, setActiveTab] = useState('browse');
    const [scholarships, setScholarships] = useState([]);
    const [selectedScholarship, setSelectedScholarship] = useState(null);
    const [applications, setApplications] = useState([]);
    const [createOpen, setCreateOpen] = useState(false);
    const [applyOpen, setApplyOpen] = useState(false);
    const [saving, setSaving] = useState(false);


    // Forms
    const [scholarshipForm, setScholarshipForm] = useState({ title: '', description: '', amount: '', donorName: '', status: 'OPEN' });
    const [statement, setStatement] = useState('');

    const studentId = SessionManager.getUserId();
    const userRole = SessionManager.getUserRole() || 'STUDENT';
    const totalAwardValue = scholarships.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
    const averageAward = scholarships.length ? totalAwardValue / scholarships.length : 0;

    useEffect(() => {
        let cancelled = false;
        const controller = new AbortController();
        (async () => {
            setLoading(true);
            setLoadError('');
            try {
                const res = await getScholarships(controller.signal);
                if (!cancelled) setScholarships(res.data || []);
            } catch (err) {
                if (controller.signal.aborted || cancelled) return;
                setLoadError(err?.response?.data?.error || 'Could not load grants.');
            } finally {
                if (!cancelled) setLoading(false);
            }
        })();
        return () => { cancelled = true; controller.abort(); };
    }, []);

    const loadScholarships = async () => {
        setLoading(true);
        setLoadError('');
        try {
            const res = await getScholarships();
            setScholarships(res.data || []);
        } catch (err) { setLoadError(err?.response?.data?.error || 'Could not load grants.'); }
        finally { setLoading(false); }
    };

    const handleCreate = async () => {
        setSaving(true);
        try {
            await createScholarship({
                ...scholarshipForm,
                amount: parseFloat(scholarshipForm.amount),
                createdBy: studentId
            });
            setScholarshipForm({ title: '', description: '', amount: '', donorName: '', status: 'OPEN' });
            setCreateOpen(false);
            toast.success('Grant published.', { refId: getSuccessRefId() });
            loadScholarships();
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not publish this grant.');
            toast.error(message, { refId, details: { status } });
        } finally { setSaving(false); }
    };

    const handleApplyClick = (s) => {
        setSelectedScholarship(s);
        setStatement('');
        setApplyOpen(true);
    };

    const handleSubmitApplication = async () => {
        setSaving(true);
        try {
            const refId = getSuccessRefId();
            await applyForScholarship(selectedScholarship.id, {
                studentId: studentId,
                statement: statement,
                status: 'APPLIED'
            });
            toast.success('Application submitted for committee review.', { refId });
            setStatement('');
            setSelectedScholarship(null);
            setApplyOpen(false);
        } catch (err) {
            const { message, status, refId } = getErrorMessage(err, 'Could not submit your application.');
            toast.error(message, { refId, details: { status } });
        } finally { setSaving(false); }
    };

    const handleViewApplications = async (s) => {
        setSelectedScholarship(s);
        try {
            const res = await getApplications(s.id);
            setApplications(res.data || []);
            setActiveTab('manage');
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not load applications.');
            toast.error(message, { refId });
        }
    };

    const handleUpdateStatus = async (appId, newStatus) => {
        try {
            await updateApplicationStatus(selectedScholarship.id, appId, newStatus);
            const res = await getApplications(selectedScholarship.id);
            setApplications(res.data || []);
            toast.success(`Application ${newStatus.toLowerCase()}.`, { refId: getSuccessRefId() });
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not update application status.');
            toast.error(message, { refId });
        }
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
                            <button className="btn btn-sm btn-primary" onClick={() => setCreateOpen(true)}>+ New Grant</button>
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

            {loadError && activeTab === 'browse' && (
                <div className="retry-bar" role="alert" style={{ marginBottom: '16px' }}>
                    <span>{loadError} (Loaded records may be incomplete.)</span>
                    <button className="btn btn-secondary btn-sm" onClick={loadScholarships}>Retry</button>
                </div>
            )}
            {loading && activeTab === 'browse' ? (
                <SkeletonCards count={6} />
            ) : activeTab === 'browse' && (
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

            <Modal
                isOpen={applyOpen}
                title={selectedScholarship ? `Apply — ${selectedScholarship.title}` : 'Apply for grant'}
                onClose={() => setApplyOpen(false)}
                onSubmit={handleSubmitApplication}
                submitLabel="Submit Application"
                submitting={saving}
                submitDisabled={!statement.trim()}
                isDirty={Boolean(statement.trim())}
                size="large"
            >
                {selectedScholarship && (
                    <form onSubmit={(e) => { e.preventDefault(); handleSubmitApplication(); }}>
                        <div style={{ padding: '12px', background: '#f0f9ff', borderRadius: '10px', marginBottom: '14px' }}>
                            <div style={{ fontSize: '.72rem', color: '#0c4a6e' }}>AWARD AMOUNT</div>
                            <div style={{ fontSize: '1.3rem', fontWeight: 'bold', color: '#0369a1' }}>₹{Number(selectedScholarship.amount || 0).toLocaleString()}</div>
                            <div style={{ fontSize: '.8rem', color: '#475569', marginTop: '6px' }}>{selectedScholarship.description}</div>
                        </div>
                        <div className="form-group">
                            <label className="form-label">Personal Statement (Financial & Academic Context) *</label>
                            <textarea
                                rows="8"
                                required
                                className="form-control"
                                placeholder="Articulate your academic journey, future goals, and how this grant will facilitate your education..."
                                value={statement}
                                onChange={e => setStatement(e.target.value)}
                            ></textarea>
                            <div style={{ marginTop: '8px', fontSize: '0.75rem', color: '#94a3b8', textAlign: 'right' }}>
                                {statement.split(/\s+/).filter(Boolean).length} / 500 words
                            </div>
                        </div>
                    </form>
                )}
            </Modal>

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

            <Modal
                isOpen={createOpen}
                title="New Grant"
                onClose={() => setCreateOpen(false)}
                onSubmit={handleCreate}
                submitLabel="Publish Grant"
                submitting={saving}
                isDirty={Boolean(scholarshipForm.title || scholarshipForm.amount || scholarshipForm.description)}
                size="large"
            >
                <p style={{ color: '#64748b', marginBottom: '16px', fontSize: '0.85rem' }}>Configure eligibility and financial parameters for this merit grant.</p>
                <form className="form-grid" onSubmit={(e) => { e.preventDefault(); handleCreate(); }}>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Grant Title *</label>
                        <input required className="form-control" type="text" value={scholarshipForm.title} onChange={e => setScholarshipForm({ ...scholarshipForm, title: e.target.value })} placeholder="e.g. Dean's List Merit Grant" />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Award Amount (₹) *</label>
                        <input required className="form-control" type="number" min="0" value={scholarshipForm.amount} onChange={e => setScholarshipForm({ ...scholarshipForm, amount: e.target.value })} />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Underwriting Sponsor</label>
                        <input className="form-control" type="text" value={scholarshipForm.donorName} onChange={e => setScholarshipForm({ ...scholarshipForm, donorName: e.target.value })} placeholder="e.g. Alumni Association" />
                    </div>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label">Eligibility Framework *</label>
                        <textarea required className="form-control" rows="5" value={scholarshipForm.description} onChange={e => setScholarshipForm({ ...scholarshipForm, description: e.target.value })} placeholder="Define GPA requirements, department constraints, etc."></textarea>
                    </div>
                </form>
            </Modal>
        </div>
    );
};

export default ScholarshipPage;
