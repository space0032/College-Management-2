import React, { useState, useEffect } from 'react';
import {
    getClubs, joinClub, getMyMemberships, leaveClub,
    getPendingMemberships, approveMembership, rejectMembership
} from '../services/clubService';

const ClubsPage = () => {
    const [activeTab, setActiveTab] = useState('browse');
    const [clubs, setClubs] = useState([]);
    const [myMemberships, setMyMemberships] = useState([]);

    // Management state
    const [selectedClubId, setSelectedClubId] = useState('');
    const [pendingMemberships, setPendingMemberships] = useState([]);

    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : { id: 2, role: 'STUDENT' };

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
            // Update UI optimistic or reload
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

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Clubs & Societies</h2>
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
                        My Clubs
                    </button>
                    {(user.role === 'ADMIN' || user.role === 'FACULTY') && (
                        <button
                            className={`btn ${activeTab === 'manage' ? 'btn-primary' : 'btn-secondary'}`}
                            onClick={() => setActiveTab('manage')}
                        >
                            Manage
                        </button>
                    )}
                </div>
            </div>

            {activeTab === 'browse' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
                    {clubs.length === 0 ? (
                        <p>No active clubs found.</p>
                    ) : (
                        clubs.map(club => (
                            <div key={club.id} className="stat-card" style={{ display: 'flex', flexDirection: 'column' }}>
                                <div style={{ flex: 1 }}>
                                    <h3>{club.name}</h3>
                                    <span className="badge badge-success">{club.category}</span>
                                    <p style={{ marginTop: '10px', color: '#666', minHeight: '60px' }}>{club.description}</p>
                                    <div style={{ fontSize: '0.9em', color: '#888', marginBottom: '15px' }}>
                                        <strong>President:</strong> {club.presidentName || 'N/A'}<br />
                                        <strong>Members:</strong> {club.memberCount}
                                    </div>
                                </div>
                                <button className="btn btn-primary" style={{ width: '100%' }} onClick={() => handleJoin(club.id)}>
                                    Join Club
                                </button>
                            </div>
                        ))
                    )}
                </div>
            )}

            {activeTab === 'my_clubs' && (
                <div>
                    {myMemberships.length === 0 ? (
                        <div className="stat-card">You have not joined any clubs yet.</div>
                    ) : (
                        <div className="data-table-container">
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Club Name</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                        <th>Joined At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {myMemberships.map(m => (
                                        <tr key={m.id}>
                                            <td>{m.clubName}</td>
                                            <td>{m.role}</td>
                                            <td>
                                                <span className={`badge ${m.status === 'APPROVED' ? 'badge-success' : m.status === 'PENDING' ? 'badge-warning' : 'badge-danger'}`}>
                                                    {m.status}
                                                </span>
                                            </td>
                                            <td>{new Date(m.joinedAt).toLocaleDateString()}</td>
                                            <td>
                                                <button className="btn btn-sm btn-danger" onClick={() => handleLeave(m.clubId)}>
                                                    {m.status === 'PENDING' ? 'Cancel Request' : 'Leave'}
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
                    <div style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '15px' }}>
                        <label><strong>Select Club:</strong></label>
                        <select
                            value={selectedClubId}
                            onChange={e => setSelectedClubId(e.target.value)}
                            style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}
                        >
                            <option value="">-- Select a Club --</option>
                            {clubs.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                        </select>
                    </div>

                    {selectedClubId && (
                        <div className="data-table-container">
                            <h4>Pending Membership Requests</h4>
                            <table className="data-table">
                                <thead>
                                    <tr>
                                        <th>Student Name</th>
                                        <th>Requested Role</th>
                                        <th>Requested At</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {pendingMemberships.length === 0 ? (
                                        <tr><td colSpan="4" style={{ textAlign: 'center' }}>No pending requests.</td></tr>
                                    ) : (
                                        pendingMemberships.map(pm => (
                                            <tr key={pm.id}>
                                                <td>{pm.studentName}</td>
                                                <td>{pm.role}</td>
                                                <td>{new Date(pm.joinedAt).toLocaleString()}</td>
                                                <td>
                                                    <div style={{ display: 'flex', gap: '8px' }}>
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
                    )}
                </div>
            )}
        </div>
    );
};

export default ClubsPage;
