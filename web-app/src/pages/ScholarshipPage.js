import './Opportunities.css';
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
    const [reviewLoading, setReviewLoading] = useState(false);
    const [reviewError, setReviewError] = useState('');
    const reviewRequest = React.useRef(0);
    const [submittedIds, setSubmittedIds] = useState([]);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL');


    // Forms
    const [scholarshipForm, setScholarshipForm] = useState({ title: '', description: '', amount: '', donorName: '', status: 'OPEN' });
    const [statement, setStatement] = useState('');

    const studentId = SessionManager.getUserId();
    const userRole = SessionManager.getUserRole();
    const canCreate = SessionManager.hasPermission('CREATE_SCHOLARSHIP');
    const canReview = userRole !== 'STUDENT' && SessionManager.hasPermission('VIEW_SCHOLARSHIP');
    const canUpdate = SessionManager.hasPermission('UPDATE_SCHOLARSHIP');
    const canApply = userRole === 'STUDENT' && SessionManager.hasPermission('MANAGE_SCHOLARSHIP');
    const wordCount = statement.trim().split(/\s+/).filter(Boolean).length;
    const visibleScholarships = scholarships.filter(s => (statusFilter === 'ALL' || s.status === statusFilter) && `${s.title} ${s.donorName || ''} ${s.description || ''}`.toLowerCase().includes(search.toLowerCase()));
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
        if (saving) return;
        if (!scholarshipForm.title.trim() || !scholarshipForm.description.trim() || !Number.isFinite(Number(scholarshipForm.amount)) || Number(scholarshipForm.amount) <= 0) { toast.error('Enter a title, eligibility description and an award amount greater than zero.'); return; }
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
        if (saving || !selectedScholarship) return;
        if (!statement.trim() || wordCount > 500) { toast.error('Enter a personal statement of 1 to 500 words.'); return; }
        setSaving(true);
        try {
            const refId = getSuccessRefId();
            await applyForScholarship(selectedScholarship.id, {
                enrollmentId: SessionManager.getUser()?.username,
                statement: statement.trim(),
                status: 'APPLIED'
            });
            setSubmittedIds(ids => [...ids, selectedScholarship.id]);
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
        const request = ++reviewRequest.current;
        setSelectedScholarship(s);
        setApplications([]);
        setReviewError('');
        setReviewLoading(true);
        setActiveTab('manage');
        try {
            const res = await getApplications(s.id);
            if (request === reviewRequest.current) setApplications(res.data || []);
        } catch (err) {
            const { message } = getErrorMessage(err, 'Could not load applications.');
            if (request === reviewRequest.current) setReviewError(message);
        } finally { if (request === reviewRequest.current) setReviewLoading(false); }
    };

    const handleUpdateStatus = async (appId, newStatus) => {
        if (saving) return;
        setSaving(true);
        try {
            await updateApplicationStatus(selectedScholarship.id, appId, newStatus);
            const res = await getApplications(selectedScholarship.id);
            setApplications(res.data || []);
            toast.success(`Application ${newStatus.toLowerCase()}.`, { refId: getSuccessRefId() });
        } catch (err) {
            const { message, refId } = getErrorMessage(err, 'Could not update application status.');
            toast.error(message, { refId });
        } finally { setSaving(false); }
    };

    return (
        <div className="opportunities-page scholarship-page">
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h1 className="page-title">Scholarships</h1>
                        <p className="page-subtitle">Academic empowerment through financial aid and merit-based sponsorships</p>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <button className={`btn btn-sm ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} disabled={saving} onClick={() => { reviewRequest.current++; setActiveTab('browse'); }}>Browse scholarships</button>
                        {canCreate && (
                            <button className="btn btn-sm btn-primary" onClick={() => setCreateOpen(true)}>+ New Grant</button>
                        )}
                    </div>
                </div>
            </div>

            {/* Premium Stats Row */}
            {activeTab === 'browse' && (
                <div className="scholarship-stats">
                    <div className="stat-card" style={{ background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', color: 'white' }}>
                        <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Total Award Value</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>₹{totalAwardValue.toLocaleString('en-IN')}</div>
                        <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Across loaded grants</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Active Grants</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#1e293b', margin: '8px 0' }}>{scholarships.filter(s => s.status === 'OPEN').length}</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Avg. Award</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3b82f6', margin: '8px 0' }}>₹{Math.round(averageAward).toLocaleString('en-IN')}</div>
                    </div>
                    <div className="stat-card">
                        <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Closed grants</div>
                        <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#8b5cf6', margin: '8px 0' }}>{scholarships.filter(s => s.status === 'CLOSED').length}</div>
                    </div>
                </div>
            )}

            {activeTab === 'browse' && <div className="opportunity-toolbar"><label htmlFor="scholarship-search">Find a scholarship</label><input id="scholarship-search" type="search" className="form-control" placeholder="Search title, sponsor or eligibility" value={search} onChange={e => setSearch(e.target.value)} /><select className="form-control" aria-label="Scholarship status" value={statusFilter} onChange={e => setStatusFilter(e.target.value)}><option value="ALL">All statuses</option><option value="OPEN">Open</option><option value="CLOSED">Closed</option></select></div>}
            {!loading && !loadError && activeTab === 'browse' && visibleScholarships.length === 0 && <div className="opportunity-empty"><h3>No scholarships found</h3><p>{scholarships.length ? 'Try another search or status filter.' : 'Scholarship opportunities will appear here when published.'}</p></div>}
            {loadError && activeTab === 'browse' && (
                <div className="retry-bar" role="alert" style={{ marginBottom: '16px' }}>
                    <span>{loadError} (Loaded records may be incomplete.)</span>
                    <button className="btn btn-secondary btn-sm" onClick={loadScholarships}>Retry</button>
                </div>
            )}
            {loading && activeTab === 'browse' ? (
                <SkeletonCards count={6} />
            ) : activeTab === 'browse' && (
                <div className="opportunity-grid">
                    {visibleScholarships.map(s => (
                        <div key={s.id} className="stat-card" style={{ padding: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column', transition: 'transform 0.2s' }}>
                            <div style={{ padding: '25px', display: 'flex', flexDirection: 'column', flex: 1 }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                                    <span className="badge" style={{ background: '#ecfdf5', color: '#047857', fontSize: '0.7rem' }}>{s.donorName || 'College Trust'}</span>
                                    <span className={`badge ${s.status === 'OPEN' ? 'badge-success' : 'badge-secondary'}`} style={{ fontSize: '0.65rem' }}>{s.status}</span>
                                </div>
                                <h3 style={{ margin: '0 0 10px 0', fontSize: '1.25rem', color: '#1e293b' }}>{s.title}</h3>
                                <div style={{ fontSize: '2rem', fontWeight: '800', color: '#1e293b', marginBottom: '15px' }}>₹{Number(s.amount || 0).toLocaleString('en-IN')}</div>
                                <p style={{ color: '#64748b', fontSize: '0.9rem', flex: 1, lineHeight: '1.6' }}>{s.description}</p>
                            </div>
                            <div style={{ padding: '20px', background: '#f8fafc', borderTop: '1px solid #f1f5f9' }}>
                                {s.status === 'OPEN' && canApply && (
                                    <button className="btn btn-primary" style={{ width: '100%', padding: '12px' }} disabled={submittedIds.includes(s.id)} onClick={() => handleApplyClick(s)}>{submittedIds.includes(s.id) ? 'Application submitted' : 'Start application'}</button>
                                )}
                                {canReview && (
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
                submitDisabled={!statement.trim() || wordCount > 500}
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
                            <label className="form-label" htmlFor="scholarship-statement">Personal Statement (Financial & Academic Context) *</label>
                            <textarea
                                id="scholarship-statement"
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
                            <button className="btn btn-sm btn-secondary" disabled={saving} onClick={() => { reviewRequest.current++; setActiveTab('browse'); }} style={{ marginBottom: '10px' }}>&larr; Back</button>
                            <h3 style={{ margin: 0 }}>Reviewing Candidates: {selectedScholarship.title}</h3>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>POOL SIZE</div>
                            <div style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{applications.length} Applicants</div>
                        </div>
                    </div>

                    <div className="opportunity-grid">
                        {reviewLoading ? <p role="status">Loading applications...</p> : reviewError ? <div role="alert">{reviewError} <button className="btn btn-secondary" onClick={() => handleViewApplications(selectedScholarship)}>Retry</button></div> : applications.length === 0 ? (
                            <div className="stat-card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '40px 20px', color: '#94a3b8' }}>
                                <div style={{ fontSize: '3rem', marginBottom: '15px' }}>📂</div>
                                <p>No applications have been submitted for this grant yet.</p>
                            </div>
                        ) : (
                            applications.map(app => (
                                <div key={app.id} className="stat-card" style={{ borderLeft: `6px solid ${app.status === 'APPROVED' ? '#10b981' : app.status === 'REJECTED' ? '#ef4444' : '#f59e0b'}` }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                                        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                                            <div style={{ width: '40px', height: '40px', background: '#3b82f6', color: 'white', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
                                                {(app.studentName || 'Student').charAt(0)}
                                            </div>
                                            <div>
                                                <h4 style={{ margin: 0 }}>{app.studentName || `Student ${app.studentId}`}</h4>
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
                                    {canUpdate && app.status === 'APPLIED' && (
                                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                                            <button className="btn btn-success" disabled={saving} onClick={() => handleUpdateStatus(app.id, 'APPROVED')}>Approve application</button>
                                            <button className="btn btn-danger" disabled={saving} onClick={() => handleUpdateStatus(app.id, 'REJECTED')}>Reject</button>
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
                        <label className="form-label" htmlFor="scholarship-title">Grant Title *</label>
                        <input id="scholarship-title" required className="form-control" type="text" value={scholarshipForm.title} onChange={e => setScholarshipForm({ ...scholarshipForm, title: e.target.value })} placeholder="e.g. Dean's List Merit Grant" />
                    </div>
                    <div className="form-group">
                        <label className="form-label" htmlFor="scholarship-amount">Award amount (INR) *</label>
                        <input id="scholarship-amount" required className="form-control" type="number" min="0.01" step="0.01" value={scholarshipForm.amount} onChange={e => setScholarshipForm({ ...scholarshipForm, amount: e.target.value })} />
                    </div>
                    <div className="form-group">
                        <label className="form-label" htmlFor="scholarship-sponsor">Underwriting Sponsor</label>
                        <input id="scholarship-sponsor" className="form-control" type="text" value={scholarshipForm.donorName} onChange={e => setScholarshipForm({ ...scholarshipForm, donorName: e.target.value })} placeholder="e.g. Alumni Association" />
                    </div>
                    <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                        <label className="form-label" htmlFor="scholarship-eligibility">Eligibility Framework *</label>
                        <textarea id="scholarship-eligibility" required className="form-control" rows="5" value={scholarshipForm.description} onChange={e => setScholarshipForm({ ...scholarshipForm, description: e.target.value })} placeholder="Define GPA requirements, department constraints, etc."></textarea>
                    </div>
                </form>
            </Modal>
        </div>
    );
};

export default ScholarshipPage;
