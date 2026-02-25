import React, { useState, useEffect } from 'react';
import { getCampaigns, createCampaign, donateToCampaign } from '../services/crowdfundingService';

const CrowdfundingPage = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [campaigns, setCampaigns] = useState([]);

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
        } catch (err) {
            console.error('Failed to load campaigns', err);
        }
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
            alert('Campaign created successfully!');
            setCampaignForm({ title: '', description: '', goalAmount: '' });
            setActiveTab('browse');
        } catch (err) {
            alert('Failed to create campaign');
        }
    };

    const handleDonateSubmit = async (e) => {
        e.preventDefault();
        try {
            await donateToCampaign(selectedCampaign.id, parseFloat(donationAmount));
            alert('Thank you for your donation!');
            setDonationAmount('');
            setSelectedCampaign(null);
            setActiveTab('browse');
        } catch (err) {
            alert('Failed to process donation');
        }
    };

    const calculateProgress = (raised, goal) => {
        if (!goal || goal <= 0) return 0;
        const pct = (raised / goal) * 100;
        return pct > 100 ? 100 : Math.round(pct);
    };

    return (
        <div className="page-container">
            <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between' }}>
                <h2>College Crowdfunding</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('browse')}>
                        Browse Campaigns
                    </button>

                    {(userRole === 'ADMIN' || userRole === 'FACULTY') && (
                        <button className={`btn ${activeTab === 'create' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('create')}>
                            Start a Campaign
                        </button>
                    )}

                    {activeTab === 'donate' && selectedCampaign && (
                        <button className="btn btn-primary">Donating: {selectedCampaign.title}</button>
                    )}
                </div>
            </div>

            {activeTab === 'browse' && (
                <div className="card-grid">
                    {campaigns.map(c => {
                        const progress = calculateProgress(c.raisedAmount, c.goalAmount);
                        return (
                            <div key={c.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <h3 style={{ margin: 0, color: 'var(--primary-color)' }}>{c.title}</h3>
                                    <span className={`badge badge-${c.status === 'ACTIVE' ? 'success' : 'secondary'}`}>{c.status}</span>
                                </div>

                                <p style={{ margin: '15px 0', color: 'var(--text-muted)', flex: 1 }}>{c.description}</p>

                                <div style={{ margin: '15px 0' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', marginBottom: '5px' }}>
                                        <span><strong>₹{c.raisedAmount.toLocaleString()}</strong> raised</span>
                                        <span style={{ color: 'var(--text-muted)' }}>Goal: ₹{c.goalAmount.toLocaleString()}</span>
                                    </div>
                                    <div style={{ height: '8px', backgroundColor: '#e0e0e0', borderRadius: '4px', overflow: 'hidden' }}>
                                        <div style={{ height: '100%', width: `${progress}%`, backgroundColor: c.status === 'COMPLETED' ? '#4caf50' : 'var(--primary-color)', transition: 'width 0.5s ease' }}></div>
                                    </div>
                                </div>

                                {c.status === 'ACTIVE' && (
                                    <button
                                        className="btn btn-primary"
                                        style={{ width: '100%', marginTop: '10px' }}
                                        onClick={() => { setSelectedCampaign(c); setActiveTab('donate'); }}
                                    >
                                        Donate Now
                                    </button>
                                )}
                                {c.status === 'COMPLETED' && (
                                    <button className="btn btn-secondary" disabled style={{ width: '100%', marginTop: '10px' }}>Goal Reached!</button>
                                )}
                            </div>
                        );
                    })}
                </div>
            )}

            {activeTab === 'create' && (userRole === 'ADMIN' || userRole === 'FACULTY') && (
                <div className="form-container" style={{ maxWidth: '600px', margin: '0 auto' }}>
                    <div className="stat-card">
                        <h3>Launch New Campaign</h3>
                        <p style={{ color: 'var(--text-muted)', marginBottom: '20px' }}>Create a public funding goal for campus improvements, clubs, or events.</p>
                        <form className="form-grid" onSubmit={handleCreate}>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Campaign Title *</label>
                                <input required type="text" value={campaignForm.title} onChange={e => setCampaignForm({ ...campaignForm, title: e.target.value })} placeholder="e.g. New Robotics Lab" />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Target Goal Amount (₹) *</label>
                                <input required type="number" min="1000" step="100" value={campaignForm.goalAmount} onChange={e => setCampaignForm({ ...campaignForm, goalAmount: e.target.value })} placeholder="e.g. 50000" />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Campaign Story & Description *</label>
                                <textarea required rows="5" value={campaignForm.description} onChange={e => setCampaignForm({ ...campaignForm, description: e.target.value })} placeholder="Explain why these funds are needed..."></textarea>
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Launch Campaign</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {activeTab === 'donate' && selectedCampaign && (
                <div className="form-container" style={{ maxWidth: '500px', margin: '0 auto' }}>
                    <div className="stat-card">
                        <button className="btn btn-secondary btn-sm" onClick={() => setActiveTab('browse')} style={{ marginBottom: '15px' }}>&larr; Back</button>
                        <h3 style={{ borderBottom: '1px solid #eee', paddingBottom: '15px' }}>Support: {selectedCampaign.title}</h3>

                        <div style={{ margin: '20px 0', padding: '15px', backgroundColor: 'var(--secondary-bg)', borderRadius: 'var(--border-radius)', textAlign: 'center' }}>
                            <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', margin: 0 }}>Current Progress</p>
                            <h2 style={{ margin: '5px 0', color: 'var(--primary-color)' }}>
                                {calculateProgress(selectedCampaign.raisedAmount, selectedCampaign.goalAmount)}%
                            </h2>
                            <p style={{ fontSize: '0.9rem', margin: 0 }}>₹{selectedCampaign.raisedAmount.toLocaleString()} of ₹{selectedCampaign.goalAmount.toLocaleString()} goal</p>
                        </div>

                        <form onSubmit={handleDonateSubmit}>
                            <div className="form-group">
                                <label>Contribution Amount (₹) *</label>
                                <input
                                    required
                                    type="number"
                                    min="10"
                                    step="10"
                                    value={donationAmount}
                                    onChange={e => setDonationAmount(e.target.value)}
                                    placeholder="Enter amount to donate..."
                                />
                            </div>

                            <button className="btn btn-primary" style={{ width: '100%', padding: '12px', fontSize: '1.1rem', marginTop: '10px' }}>
                                Confirm Donation of ₹{donationAmount || '0'}
                            </button>
                        </form>
                    </div>
                </div>
            )}

        </div>
    );
};

export default CrowdfundingPage;
