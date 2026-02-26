import React, { useState, useEffect } from 'react';
import {
    getClubs, joinClub, getMyMemberships, leaveClub,
    getPendingMemberships, approveMembership, rejectMembership
} from '../services/clubService';
import SessionManager from '../utils/SessionManager';

const CATEGORY_COLORS = {
    'Technical': { bg: '#ebf8ff', text: '#2b6cb0', icon: '💻' },
    'Cultural': { bg: '#faf5ff', text: '#805ad5', icon: '🎨' },
    'Sports': { bg: '#f0fff4', text: '#2f855a', icon: '⚽' },
    'Social': { bg: '#fff5f5', text: '#c53030', icon: '🤝' },
    'Academic': { bg: '#fffaf0', text: '#c05621', icon: '📚' },
};

const ClubsPage = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [clubs, setClubs] = useState([]);
    const [myMemberships, setMyMemberships] = useState([]);
    const [filterCategory, setFilterCategory] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    // Management state
    const [selectedClubId, setSelectedClubId] = useState('');
    const [pendingMemberships, setPendingMemberships] = useState([]);

    const user = SessionManager.getUser() || {};

    useEffect(() => {
        if (activeTab === 'browse' || activeTab === 'manage') loadClubs();
        if (activeTab === 'my_clubs') loadMyMemberships();
        // eslint-disable-next-line
    }, [activeTab]);

    useEffect(() => {
        if (activeTab === 'manage' && selectedClubId) {
            loadPendingMemberships(selectedClubId);
        }
    }, [activeTab, selectedClubId]);

    const loadClubs = async () => {
        try {
            const res = await getClubs();
            setClubs(res.data || []);
            if (!selectedClubId && res.data?.length > 0) {
                setSelectedClubId(res.data[0].id.toString());
            }
        } catch (err) {
            console.error(err);
        }
    };

    const loadMyMemberships = async () => {
        try {
            const res = await getMyMemberships(user.id);
            setMyMemberships(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const loadPendingMemberships = async (clubId) => {
        try {
            const res = await getPendingMemberships(clubId);
            setPendingMemberships(res.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    const handleJoin = async (clubId) => {
        try {
            await joinClub(clubId, user.id);
            alert('Join request sent successfully!');
            loadMyMemberships();
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to send join request.');
        }
    };

    const handleLeave = async (clubId) => {
        if (!window.confirm('Are you sure you want to leave this club?')) return;
        try {
            await leaveClub(clubId, user.id);
            loadMyMemberships();
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to leave club.');
        }
    };

    const handleApprove = async (membershipId) => {
        try {
            await approveMembership(membershipId);
            loadPendingMemberships(selectedClubId);
        } catch (err) {
            alert('Failed to approve membership.');
        }
    };

    const handleReject = async (membershipId) => {
        try {
            await rejectMembership(membershipId);
            loadPendingMemberships(selectedClubId);
        } catch (err) {
            alert('Failed to reject membership.');
        }
    };

    const filteredClubs = clubs.filter(c => {
        const matchesCategory = !filterCategory || c.category === filterCategory;
        const matchesSearch = !searchTerm || c.name.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesCategory && matchesSearch;
    });

    const categories = [...new Set(clubs.map(c => c.category).filter(Boolean))];
    const totalMembers = clubs.reduce((acc, c) => acc + (c.memberCount || 0), 0);

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">👥 Clubs & Societies</h1>
                    <p className="page-subtitle">Explore student-led organizations, join communities, and lead events</p>
                </div>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button
                        className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('browse')}
                    >
                        Browse Clubs
                    </button>
                    <button
                        className={`btn ${activeTab === 'my_clubs' ? 'btn-primary' : 'btn-secondary'}`}
                        onClick={() => setActiveTab('my_clubs')}
                    >
                        My Memberships
                    </button>
                    {(user.role === 'ADMIN' || user.role === 'FACULTY') && (
                        <button
                            className={`btn ${activeTab === 'manage' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setActiveTab('manage')}
                        >
                            Review Requests
                        </button>
                    )}
                </div>
            </div>

            {/* Premium Stats Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '25px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.9rem', opacity: 0.8 }}>Active Clubs</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold' }}>{clubs.length}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>Total Students Enrolled</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#3182ce' }}>{totalMembers}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>Categories</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#805ad5' }}>{categories.length}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.9rem', color: '#666' }}>My Clubs</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#48bb78' }}>{myMemberships.length}</div>
                </div>
            </div>

            {activeTab === 'browse' && (
                <>
                    <div className="card" style={{ padding: '15px', marginBottom: '20px', display: 'flex', gap: '15px', background: 'white', borderRadius: '12px' }}>
                        <input
                            type="text"
                            className="form-control"
                            placeholder="Search for a club by name..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            style={{ flex: 1 }}
                        />
                        <select
                            className="form-control"
                            style={{ width: '200px' }}
                            value={filterCategory}
                            onChange={(e) => setFilterCategory(e.target.value)}
                        >
                            <option value="">All Categories</option>
                            {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                        </select>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
                        {filteredClubs.length === 0 ? (
                            <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '50px', color: '#a0aec0' }}>
                                No clubs found matching your criteria.
                            </div>
                        ) : (
                            filteredClubs.map(club => {
                                const catStyle = CATEGORY_COLORS[club.category] || { bg: '#f7fafc', text: '#4a5568', icon: '💠' };
                                const isMember = myMemberships.some(m => m.clubId === club.id && m.status === 'APPROVED');
                                const isPending = myMemberships.some(m => m.clubId === club.id && m.status === 'PENDING');

                                return (
                                    <div key={club.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column', transition: 'transform 0.2s', border: '1px solid #edf2f7' }}>
                                        <div style={{ flex: 1 }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px' }}>
                                                <div style={{ fontSize: '2rem' }}>{catStyle.icon}</div>
                                                <span style={{
                                                    padding: '4px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: 'bold',
                                                    background: catStyle.bg, color: catStyle.text
                                                }}>
                                                    {club.category}
                                                </span>
                                            </div>
                                            <h3 style={{ margin: '0 0 10px 0', fontSize: '1.2rem' }}>{club.name}</h3>
                                            <p style={{ color: '#718096', fontSize: '0.9rem', minHeight: '45px', lineHeight: '1.4' }}>{club.description}</p>

                                            <div style={{
                                                marginTop: '15px', paddingTop: '15px', borderTop: '1px solid #edf2f7',
                                                display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem'
                                            }}>
                                                <div>
                                                    <span style={{ color: '#a0aec0' }}>Leads:</span><br />
                                                    <strong>{club.presidentName || 'TBA'}</strong>
                                                </div>
                                                <div style={{ textAlign: 'right' }}>
                                                    <span style={{ color: '#a0aec0' }}>Community:</span><br />
                                                    <strong>{club.memberCount} members</strong>
                                                </div>
                                            </div>
                                        </div>

                                        <div style={{ marginTop: '20px' }}>
                                            {isMember ? (
                                                <div style={{
                                                    textAlign: 'center', padding: '10px', borderRadius: '8px',
                                                    background: '#f0fff4', color: '#38a169', fontWeight: 'bold', fontSize: '0.9rem',
                                                    border: '1px solid #c6f6d5'
                                                }}>
                                                    ✓ Member
                                                </div>
                                            ) : isPending ? (
                                                <div style={{
                                                    textAlign: 'center', padding: '10px', borderRadius: '8px',
                                                    background: '#fffaf0', color: '#dd6b20', fontWeight: '600', fontSize: '0.9rem',
                                                    border: '1px solid #feebc8'
                                                }}>
                                                    ⏳ Request Pending
                                                </div>
                                            ) : (
                                                <button
                                                    className="btn btn-primary"
                                                    style={{ width: '100%', padding: '10px', fontWeight: 'bold' }}
                                                    onClick={() => handleJoin(club.id)}
                                                >
                                                    Join Community
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                );
                            })
                        )}
                    </div>
                </>
            )}

            {activeTab === 'my_clubs' && (
                <div>
                    {myMemberships.length === 0 ? (
                        <div className="stat-card" style={{ textAlign: 'center', padding: '60px', color: '#a0aec0' }}>
                            <div style={{ fontSize: '3rem', marginBottom: '15px' }}>🔭</div>
                            <p>You haven't joined any communities yet. Head over to <strong>Browse Clubs</strong> to explore!</p>
                        </div>
                    ) : (
                        <div className="data-table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Club Name</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                        <th>Joined On</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {myMemberships.map(m => (
                                        <tr key={m.id}>
                                            <td style={{ fontWeight: 'bold' }}>{m.clubName}</td>
                                            <td><span className="badge badge-secondary">{m.role}</span></td>
                                            <td>
                                                <span className={`badge ${m.status === 'APPROVED' ? 'badge-success' : m.status === 'PENDING' ? 'badge-warning' : 'badge-danger'}`}>
                                                    {m.status}
                                                </span>
                                            </td>
                                            <td>{new Date(m.joinedAt).toLocaleDateString()}</td>
                                            <td>
                                                <button className="btn btn-sm btn-danger" onClick={() => handleLeave(m.clubId)}>
                                                    {m.status === 'PENDING' ? 'Revoke Request' : 'Leave Club'}
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}

            {activeTab === 'manage' && (
                <div className="stat-card">
                    <div style={{ marginBottom: '25px', display: 'flex', alignItems: 'center', gap: '15px', padding: '15px', background: '#f8fafc', borderRadius: '10px' }}>
                        <div style={{ fontSize: '1.5rem' }}>🎫</div>
                        <div style={{ flex: 1 }}>
                            <label style={{ fontSize: '0.9rem', color: '#64748b' }}>Select Club to Manage:</label>
                            <select
                                value={selectedClubId}
                                onChange={e => setSelectedClubId(e.target.value)}
                                className="form-control"
                                style={{ marginTop: '5px' }}
                            >
                                <option value="">-- Choose Club --</option>
                                {clubs.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                            </select>
                        </div>
                    </div>

                    {selectedClubId && (
                        <div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                                <h3 style={{ margin: 0 }}>Membership Requests</h3>
                                <div className="badge badge-warning">{pendingMemberships.length} Pending</div>
                            </div>

                            <div className="data-table-container">
                                <table className="data-table">
                                    <thead>
                                        <tr>
                                            <th>Student Name</th>
                                            <th>Target Role</th>
                                            <th>Applied On</th>
                                            <th style={{ textAlign: 'right' }}>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {pendingMemberships.length === 0 ? (
                                            <tr><td colSpan="4" style={{ textAlign: 'center', padding: '40px', color: '#a0aec0' }}>Great! No pending requests for this club.</td></tr>
                                        ) : (
                                            pendingMemberships.map(pm => (
                                                <tr key={pm.id}>
                                                    <td><strong>{pm.studentName}</strong></td>
                                                    <td><span className="badge badge-secondary">{pm.role}</span></td>
                                                    <td>{new Date(pm.joinedAt).toLocaleString()}</td>
                                                    <td style={{ textAlign: 'right' }}>
                                                        <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                                                            <button className="btn btn-sm btn-success" onClick={() => handleApprove(pm.id)}>Approve</button>
                                                            <button className="btn btn-sm btn-danger" onClick={() => handleReject(pm.id)}>Reject</button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))
                                        )}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default ClubsPage;
