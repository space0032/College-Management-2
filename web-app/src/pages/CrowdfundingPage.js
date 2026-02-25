import React, { useState, useEffect } from 'react';
import { getCampaigns, createCampaign, donateToCampaign } from '../services/crowdfundingService';

const CrowdfundingPage = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [campaigns, setCampaigns] = useState([]);
    const [isProcessing, setIsProcessing] = useState(false);

    // Create Form
    const [campaignForm, setCampaignForm] = useState({ title: '', description: '', goalAmount: '' });

    // Donate Form
    const [selectedCampaign, setSelectedCampaign] = useState(null);
    const [donationAmount, setDonationAmount] = useState('');

    const userRole = localStorage.getItem('userRole') || 'STUDENT';
    const userId = parseInt(localStorage.getItem('userId') || '1');

    useEffect(() => {
        loadCampaigns();
    }, [activeTab]);

    const loadCampaigns = async () => {
        try {
            const res = await getCampaigns();
            setCampaigns(res.data || []);
        } catch (err) { console.error(err); }
    };

    const handleCreate = async (e) => {
        e.preventDefault();
        try {
            await createCampaign({
                title: campaignForm.title,
                description: campaignForm.description,
                goalAmount: parseFloat(campaignForm.goalAmount),
                createdBy: userId,
                status: 'ACTIVE'
            });
            alert('Campaign launched! The community has been notified.');
            setCampaignForm({ title: '', description: '', goalAmount: '' });
            setActiveTab('browse');
        } catch (err) { alert('Launch failed'); }
    };

    const handleDonateSubmit = async (e) => {
        e.preventDefault();
        setIsProcessing(true);
        try {
            await donateToCampaign(selectedCampaign.id, parseFloat(donationAmount));
            alert('Contribution confirmed! You are now a donor.');
            setDonationAmount('');
            setSelectedCampaign(null);
            setActiveTab('browse');
        } catch (err) { alert('Donation failed'); }
        finally { setIsProcessing(false); }
    };

    const calculateProgress = (raised, goal) => {
        if (!goal || goal <= 0) return 0;
        return Math.min(100, Math.round((raised / goal) * 100));
    };

    // Global Stats
    const totalRaised = campaigns.reduce((acc, c) => acc + c.raisedAmount, 0);
    const donorCount = Math.floor(totalRaised / 500) + 12; // Simulated donor count

    return (
        <div className="page-container" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                        <h1 className="page-title">🤝 Campus Philanthropy</h1>
                        <p className="page-subtitle">Crowdfunded initiatives for laboratory equipment, sports gear, and student projects</p>
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <button className={`btn btn-sm ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('browse')}>Live Campaigns</button>
                        {(userRole === 'ADMIN' || userRole === 'FACULTY') && (
                            <button className={`btn btn-sm ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('create')}>+ Launch Project</button>
                        )}
                    </div>
                </div>
            </div>

            {activeTab === 'browse' && (
                <>
                    {/* Philanthropy Stats */}
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '35px' }}>
                        <div className="stat-card" style={{ background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)', color: 'white' }}>
                            <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Cumulative Funding</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>₹{totalRaised.toLocaleString()}+</div>
                            <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Community Contributions</div>
                        </div>
                        <div className="stat-card">
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Active Projects</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#1e293b', margin: '8px 0' }}>{campaigns.filter(c => c.status === 'ACTIVE').length}</div>
                        </div>
                        <div className="stat-card">
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Impact Network</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#10b981', margin: '8px 0' }}>{donorCount} Donors</div>
                        </div>
                        <div className="stat-card">
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Success Rate</div>
                            <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#6366f1', margin: '8px 0' }}>94%</div>
                        </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px', alignItems: 'flex-start' }}>
                        {/* Campaigns Grid */}
                        <div className="card-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '25px' }}>
                            {campaigns.map(c => {
                                const progress = calculateProgress(c.raisedAmount, c.goalAmount);
                                return (
                                    <div key={c.id} className="stat-card" style={{ padding: 0, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
                                        <div style={{ padding: '25px', flex: 1 }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px' }}>
                                                <h3 style={{ margin: 0, fontSize: '1.2rem', color: '#1e293b' }}>{c.title}</h3>
                                                <span className={`badge ${c.status === 'ACTIVE' ? 'badge-success' : 'badge-secondary'}`} style={{ fontSize: '0.65rem' }}>{c.status}</span>
                                            </div>
                                            <p style={{ color: '#64748b', fontSize: '0.9rem', marginBottom: '20px', lineHeight: '1.6' }}>{c.description}</p>

                                            <div style={{ margin: '15px 0' }}>
                                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '8px' }}>
                                                    <span><strong>₹{c.raisedAmount.toLocaleString()}</strong> of ₹{c.goalAmount.toLocaleString()}</span>
                                                    <span style={{ fontWeight: 'bold', color: 'var(--primary-color)' }}>{progress}%</span>
                                                </div>
                                                <div style={{ height: '10px', background: '#f1f5f9', borderRadius: '5px', overflow: 'hidden' }}>
                                                    <div style={{
                                                        height: '100%', width: `${progress}%`,
                                                        background: 'linear-gradient(90deg, #6366f1 0%, #a855f7 100%)',
                                                        transition: 'width 0.8s ease'
                                                    }} />
                                                </div>
                                            </div>
                                        </div>
                                        <div style={{ padding: '20px', background: '#f8fafc', borderTop: '1px solid #f1f5f9' }}>
                                            {c.status === 'ACTIVE' ? (
                                                <button className="btn btn-primary" style={{ width: '100%', padding: '12px' }} onClick={() => { setSelectedCampaign(c); setActiveTab('donate'); }}>Contribute Funds</button>
                                            ) : (
                                                <button className="btn btn-secondary" disabled style={{ width: '100%' }}>Funding Finalized</button>
                                            )}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>

                        {/* Recent Activity / Hall of Fame Sidebar */}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '25px' }}>
                            <div className="stat-card">
                                <h4 style={{ marginBottom: '20px', borderBottom: '1px solid #f1f5f9', paddingBottom: '10px' }}>🌟 Top Contributors</h4>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                                    {[
                                        { name: 'Dr. John Watson', amount: 25000, initials: 'JW' },
                                        { name: 'Alumni Assoc (Batch 2008)', amount: 18500, initials: 'AA' },
                                        { name: 'Sarah Miller', amount: 12000, initials: 'SM' }
                                    ].map((donor, i) => (
                                        <div key={i} style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                                            <div style={{ width: '36px', height: '36px', background: '#f1f5f9', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', color: '#6366f1' }}>{donor.initials}</div>
                                            <div style={{ flex: 1 }}>
                                                <div style={{ fontSize: '0.85rem', fontWeight: '600' }}>{donor.name}</div>
                                                <div style={{ fontSize: '0.75rem', color: '#94a3b8' }}>Donated ₹{donor.amount.toLocaleString()}</div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            <div className="stat-card">
                                <h4 style={{ marginBottom: '15px' }}>Impact Feed</h4>
                                <div style={{ fontSize: '0.8rem', color: '#64748b', fontStyle: 'italic' }}>
                                    "Your donations last month successfully funded the new Cricket Training Net! 🏏"
                                </div>
                            </div>
                        </div>
                    </div>
                </>
            )}

            {activeTab === 'donate' && selectedCampaign && (
                <div style={{ maxWidth: '600px', margin: '40px auto' }}>
                    <div className="stat-card" style={{ padding: '40px' }}>
                        <button className="btn btn-sm btn-secondary" onClick={() => setActiveTab('browse')} style={{ marginBottom: '25px' }}>&larr; Back</button>
                        <h2 style={{ marginBottom: '10px' }}>Finalize Contribution</h2>
                        <p style={{ color: '#64748b', marginBottom: '30px' }}>Project: <strong>{selectedCampaign.title}</strong></p>

                        <div style={{ background: '#f0f9ff', padding: '25px', borderRadius: '15px', marginBottom: '30px', textAlign: 'center' }}>
                            <div style={{ fontSize: '0.8rem', color: '#0369a1', marginBottom: '8px', textTransform: 'uppercase', letterSpacing: '1px' }}>Project Velocity</div>
                            <div style={{ fontSize: '2.5rem', fontWeight: '900', color: '#0369a1' }}>
                                {calculateProgress(selectedCampaign.raisedAmount, selectedCampaign.goalAmount)}%
                            </div>
                            <div style={{ width: '100%', height: '8px', background: '#bae6fd', borderRadius: '4px', marginTop: '15px', overflow: 'hidden' }}>
                                <div style={{ width: `${calculateProgress(selectedCampaign.raisedAmount, selectedCampaign.goalAmount)}%`, height: '100%', background: '#0ea5e9' }} />
                            </div>
                        </div>

                        <form onSubmit={handleDonateSubmit}>
                            <div className="form-group">
                                <label style={{ fontWeight: 'bold' }}>Contribution Amount (₹) *</label>
                                <input
                                    required
                                    type="number"
                                    min="100"
                                    className="form-control"
                                    value={donationAmount}
                                    onChange={e => setDonationAmount(e.target.value)}
                                    placeholder="e.g. 500"
                                    style={{ fontSize: '1.4rem', padding: '15px', textAlign: 'center' }}
                                />
                            </div>
                            <button className="btn btn-primary" disabled={isProcessing} style={{ width: '100%', padding: '18px', fontSize: '1.2rem', fontWeight: 'bold', marginTop: '20px' }}>
                                {isProcessing ? 'Processing Transaction...' : `Confirm ₹${donationAmount || '0'} Contribution`}
                            </button>
                            <p style={{ textAlign: 'center', fontSize: '0.75rem', color: '#94a3b8', marginTop: '15px' }}>
                                All donations are secure and tracked by institutional finance.
                            </p>
                        </form>
                    </div>
                </div>
            )}

            {activeTab === 'create' && (userRole === 'ADMIN' || userRole === 'FACULTY') && (
                <div style={{ maxWidth: '700px', margin: '0 auto' }}>
                    <div className="stat-card" style={{ padding: '40px' }}>
                        <h2 style={{ marginBottom: '10px' }}>Project Parameters</h2>
                        <p style={{ color: '#64748b', marginBottom: '30px' }}>Draft a social funding goal for campus improvements or student welfare.</p>
                        <form className="form-grid" onSubmit={handleCreate}>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Campaign Name *</label>
                                <input required className="form-control" type="text" value={campaignForm.title} onChange={e => setCampaignForm({ ...campaignForm, title: e.target.value })} placeholder="e.g. Solar Panels for Library Roof" />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Funding Target (₹) *</label>
                                <input required className="form-control" type="number" min="1000" value={campaignForm.goalAmount} onChange={e => setCampaignForm({ ...campaignForm, goalAmount: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Campaign Narrative & Impact *</label>
                                <textarea required className="form-control" rows="6" value={campaignForm.description} onChange={e => setCampaignForm({ ...campaignForm, description: e.target.value })} placeholder="Describe the project and how the funds will be utilized..."></textarea>
                            </div>
                            <div style={{ gridColumn: '1 / -1', marginTop: '15px' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%', padding: '15px', fontWeight: 'bold' }}>Authorize & Launch</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CrowdfundingPage;
