import React, { useState, useEffect } from 'react';
import {
    getClubs, joinClub, getMyMemberships, leaveClub,
    getPendingMemberships, approveMembership, rejectMembership,
    createClub, updateClub, deleteClub, getClubMembers,
    getClubAnnouncements, postClubAnnouncement
} from '../services/clubService';
import { getAllStudents } from '../services/studentService';
import { getAllFaculty } from '../services/facultyService';
import SessionManager from '../utils/SessionManager';
import Modal from '../components/Modal';

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
    const [students, setStudents] = useState([]);
    const [faculty, setFaculty] = useState([]);

    // Management states
    const [selectedClub, setSelectedClub] = useState(null);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);
    const [isRequestsModalOpen, setIsRequestsModalOpen] = useState(false);
    const [isAnnouncementsModalOpen, setIsAnnouncementsModalOpen] = useState(false);

    // Data for management modals
    const [clubFormData, setClubFormData] = useState({ name: '', description: '', category: 'Technical', status: 'ACTIVE', presidentStudentId: '', facultyCoordinatorId: '' });
    const [currentMembers, setCurrentMembers] = useState([]);
    const [pendingMemberships, setPendingMemberships] = useState([]);
    const [announcements, setAnnouncements] = useState([]);
    const [newAnn, setNewAnn] = useState({ title: '', content: '' });

    const user = SessionManager.getUser() || {};
    const isAdminOrFaculty = SessionManager.hasRole('ADMIN') || user.role === 'FACULTY';

    const loadClubs = React.useCallback(async () => {
        try {
            const res = await getClubs();
            setClubs(res.data || []);
        } catch (err) {
            console.error(err);
        }
    }, []);

    const loadUsers = React.useCallback(async () => {
        try {
            const stuRes = await getAllStudents();
            setStudents(stuRes.data || []);
            const facRes = await getAllFaculty();
            setFaculty(facRes.data || []);
        } catch (err) {
            console.error(err);
        }
    }, []);

    const loadMyMemberships = React.useCallback(async () => {
        try {
            const res = await getMyMemberships(user.username);
            setMyMemberships(res.data || []);
        } catch (err) {
            console.error(err);
        }
    }, [user.username]);

    useEffect(() => {
        if (activeTab === 'browse' || activeTab === 'manage') loadClubs();
        if (activeTab === 'my_clubs') loadMyMemberships();
        if (activeTab === 'manage') loadUsers();
    }, [activeTab, loadClubs, loadMyMemberships, loadUsers]);

    const handleJoin = async (clubId) => {
        try {
            await joinClub(clubId, user.username);
            alert('Join request sent successfully!');
            loadMyMemberships();
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to send join request.');
        }
    };

    const handleLeave = async (clubId) => {
        if (!window.confirm('Are you sure you want to leave this club?')) return;
        try {
            await leaveClub(clubId, user.username);
            loadMyMemberships();
            if (activeTab === 'manage' && selectedClub?.id === clubId) {
                // refresh members if open
                const res = await getClubMembers(clubId);
                setCurrentMembers(res.data || []);
            }
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to leave club.');
        }
    };

    const handleSaveClub = async () => {
        try {
            if (clubFormData.id) {
                await updateClub(clubFormData.id, clubFormData);
            } else {
                await createClub(clubFormData);
            }
            setIsEditModalOpen(false);
            loadClubs();
        } catch (err) {
            alert(err.response?.data?.error || 'Failed to save club.');
        }
    };

    const handleDeleteClub = async (id) => {
        if (!window.confirm('Are you sure you want to delete this club?')) return;
        try {
            await deleteClub(id);
            loadClubs();
        } catch (err) {
            alert('Failed to delete club.');
        }
    };

    const openEditModal = (club = null) => {
        if (club) {
            setClubFormData({
                id: club.id,
                name: club.name || '',
                description: club.description || '',
                category: club.category || 'Technical',
                status: club.status || 'ACTIVE',
                presidentStudentId: club.presidentStudentId || '',
                facultyCoordinatorId: club.facultyCoordinatorId || ''
            });
        } else {
            setClubFormData({ name: '', description: '', category: 'Technical', status: 'ACTIVE', presidentStudentId: '', facultyCoordinatorId: '' });
        }
        setIsEditModalOpen(true);
    };

    const openMembersModal = async (club) => {
        setSelectedClub(club);
        try {
            const res = await getClubMembers(club.id);
            setCurrentMembers(res.data || []);
            setIsMembersModalOpen(true);
        } catch (err) {
            alert('Failed to load members.');
        }
    };

    const openRequestsModal = async (club) => {
        setSelectedClub(club);
        try {
            const res = await getPendingMemberships(club.id);
            setPendingMemberships(res.data || []);
            setIsRequestsModalOpen(true);
        } catch (err) {
            alert('Failed to load requests.');
        }
    };

    const openAnnouncementsModal = async (club) => {
        setSelectedClub(club);
        try {
            const res = await getClubAnnouncements(club.id);
            setAnnouncements(res.data || []);
            setIsAnnouncementsModalOpen(true);
            setNewAnn({ title: '', content: '' });
        } catch (err) {
            alert('Failed to load announcements.');
        }
    };

    const handleApprove = async (membershipId) => {
        try {
            await approveMembership(membershipId);
            const res = await getPendingMemberships(selectedClub.id);
            setPendingMemberships(res.data || []);
            loadClubs();
        } catch (err) {
            alert('Failed to approve membership.');
        }
    };

    const handleReject = async (membershipId) => {
        try {
            await rejectMembership(membershipId);
            const res = await getPendingMemberships(selectedClub.id);
            setPendingMemberships(res.data || []);
        } catch (err) {
            alert('Failed to reject membership.');
        }
    };

    const handlePostAnnouncement = async () => {
        if (!newAnn.title || !newAnn.content) {
            alert('Title and content required.');
            return;
        }
        try {
            await postClubAnnouncement(selectedClub.id, { ...newAnn, postedBy: user.id });
            const res = await getClubAnnouncements(selectedClub.id);
            setAnnouncements(res.data || []);
            setNewAnn({ title: '', content: '' });
        } catch (err) {
            alert('Failed to post announcement.');
        }
    };

    const handleRemoveMember = async (memberId) => {
        if (!window.confirm('Remove this member?')) return;
        try {
            await leaveClub(selectedClub.id, memberId);
            const res = await getClubMembers(selectedClub.id);
            setCurrentMembers(res.data || []);
            loadClubs();
        } catch (err) {
            alert('Failed to remove member.');
        }
    };

    const filteredClubs = clubs.filter(c => {
        const matchesCategory = !filterCategory || c.category === filterCategory;
        const matchesSearch = !searchTerm || c.name?.toLowerCase().includes(searchTerm.toLowerCase());
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
                    <button className={`btn ${activeTab === 'browse' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('browse')}>Browse Clubs</button>
                    <button className={`btn ${activeTab === 'my_clubs' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('my_clubs')}>My Memberships</button>
                    {isAdminOrFaculty && (
                        <button className={`btn ${activeTab === 'manage' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('manage')}>Administration</button>
                    )}
                </div>
            </div>

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
                        <input type="text" className="form-control" placeholder="Search for a club by name..." value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} style={{ flex: 1 }} />
                        <select className="form-control" style={{ width: '200px' }} value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)}>
                            <option value="">All Categories</option>
                            {categories.map(cat => <option key={cat} value={cat}>{cat}</option>)}
                        </select>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
                        {filteredClubs.length === 0 ? (
                            <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '50px', color: '#a0aec0' }}>No clubs found matching your criteria.</div>
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
                                                <span style={{ padding: '4px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: 'bold', background: catStyle.bg, color: catStyle.text }}>{club.category}</span>
                                            </div>
                                            <h3 style={{ margin: '0 0 10px 0', fontSize: '1.2rem' }}>{club.name}</h3>
                                            <p style={{ color: '#718096', fontSize: '0.9rem', minHeight: '45px', lineHeight: '1.4' }}>{club.description}</p>
                                            <div style={{ marginTop: '15px', paddingTop: '15px', borderTop: '1px solid #edf2f7', display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
                                                <div><span style={{ color: '#a0aec0' }}>Leads:</span><br /><strong>{club.presidentName || 'TBA'}</strong></div>
                                                <div style={{ textAlign: 'right' }}><span style={{ color: '#a0aec0' }}>Community:</span><br /><strong>{club.memberCount} members</strong></div>
                                            </div>
                                        </div>
                                        <div style={{ marginTop: '20px' }}>
                                            {isMember ? (
                                                <div style={{ textAlign: 'center', padding: '10px', borderRadius: '8px', background: '#f0fff4', color: '#38a169', fontWeight: 'bold', fontSize: '0.9rem', border: '1px solid #c6f6d5' }}>✓ Member</div>
                                            ) : isPending ? (
                                                <div style={{ textAlign: 'center', padding: '10px', borderRadius: '8px', background: '#fffaf0', color: '#dd6b20', fontWeight: '600', fontSize: '0.9rem', border: '1px solid #feebc8' }}>⏳ Request Pending</div>
                                            ) : (
                                                <button className="btn btn-primary" style={{ width: '100%', padding: '10px', fontWeight: 'bold' }} onClick={() => handleJoin(club.id)}>Join Community</button>
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
                            <div style={{ fontSize: '3rem', marginBottom: '15px' }}>🔭</div><p>You haven't joined any communities yet.</p>
                        </div>
                    ) : (
                        <div className="data-table-container">
                            <table className="data-table">
                                <thead><tr><th>Club Name</th><th>Role</th><th>Status</th><th>Joined On</th><th>Actions</th></tr></thead>
                                <tbody>
                                    {myMemberships.map(m => (
                                        <tr key={m.id}>
                                            <td style={{ fontWeight: 'bold' }}>{m.clubName}</td>
                                            <td><span className="badge badge-secondary">{m.role}</span></td>
                                            <td><span className={`badge ${m.status === 'APPROVED' ? 'badge-success' : m.status === 'PENDING' ? 'badge-warning' : 'badge-danger'}`}>{m.status}</span></td>
                                            <td>{new Date(m.joinedAt).toLocaleDateString()}</td>
                                            <td><button className="btn btn-sm btn-danger" onClick={() => handleLeave(m.clubId)}>{m.status === 'PENDING' ? 'Revoke Request' : 'Leave Club'}</button></td>
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
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <h3 style={{ margin: 0 }}>Club Directory</h3>
                        <button className="btn btn-primary" onClick={() => openEditModal(null)}>+ Create Club</button>
                    </div>

                    <div className="data-table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Club Name</th>
                                    <th>Category</th>
                                    <th>Leads</th>
                                    <th>Members</th>
                                    <th>Status</th>
                                    <th style={{ textAlign: 'right', minWidth: '280px' }}>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {clubs.map(club => (
                                    <tr key={club.id}>
                                        <td><strong>{club.name}</strong></td>
                                        <td>{club.category}</td>
                                        <td>
                                            <div style={{ fontSize: '0.85rem' }}>
                                                <div>President: {club.presidentName || 'TBA'}</div>
                                                <div style={{ color: '#718096' }}>Coord: {club.coordinatorName || 'TBA'}</div>
                                            </div>
                                        </td>
                                        <td>{club.memberCount}</td>
                                        <td>
                                            <span className={`badge ${club.status === 'ACTIVE' ? 'badge-success' : 'badge-secondary'}`}>{club.status}</span>
                                        </td>
                                        <td style={{ textAlign: 'right' }}>
                                            <div style={{ display: 'flex', gap: '5px', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                                                <button className="btn btn-sm" style={{ backgroundColor: '#3b82f6', color: 'white' }} onClick={() => openEditModal(club)}>Edit</button>
                                                <button className="btn btn-sm" style={{ backgroundColor: '#8b5cf6', color: 'white' }} onClick={() => openMembersModal(club)}>Members</button>
                                                <button className="btn btn-sm" style={{ backgroundColor: '#f59e0b', color: 'white' }} onClick={() => openRequestsModal(club)}>Requests</button>
                                                <button className="btn btn-sm" style={{ backgroundColor: '#0d9488', color: 'white' }} onClick={() => openAnnouncementsModal(club)}>Announce</button>
                                                <button className="btn btn-sm btn-danger" onClick={() => handleDeleteClub(club.id)}>Delete</button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* Modals */}
            <Modal isOpen={isEditModalOpen} onClose={() => setIsEditModalOpen(false)} title={clubFormData.id ? 'Edit Club' : 'Create Club'} onSubmit={handleSaveClub}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div>
                        <label>Club Name</label>
                        <input className="form-control" value={clubFormData.name} onChange={e => setClubFormData({ ...clubFormData, name: e.target.value })} />
                    </div>
                    <div>
                        <label>Description</label>
                        <textarea className="form-control" rows="3" value={clubFormData.description} onChange={e => setClubFormData({ ...clubFormData, description: e.target.value })} />
                    </div>
                    <div>
                        <label>Category</label>
                        <select className="form-control" value={clubFormData.category} onChange={e => setClubFormData({ ...clubFormData, category: e.target.value })}>
                            <option value="Technical">Technical</option>
                            <option value="Cultural">Cultural</option>
                            <option value="Sports">Sports</option>
                            <option value="Social">Social</option>
                            <option value="Academic">Academic</option>
                        </select>
                    </div>
                    <div>
                        <label>President</label>
                        <select className="form-control" value={clubFormData.presidentStudentId || ''} onChange={e => setClubFormData({ ...clubFormData, presidentStudentId: e.target.value })}>
                            <option value="">-- None --</option>
                            {students.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
                        </select>
                    </div>
                    <div>
                        <label>Faculty Coordinator</label>
                        <select className="form-control" value={clubFormData.facultyCoordinatorId || ''} onChange={e => setClubFormData({ ...clubFormData, facultyCoordinatorId: e.target.value })}>
                            <option value="">-- None --</option>
                            {faculty.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}
                        </select>
                    </div>
                    <div>
                        <label>Status</label>
                        <select className="form-control" value={clubFormData.status} onChange={e => setClubFormData({ ...clubFormData, status: e.target.value })}>
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </div>
                </div>
            </Modal>

            <Modal isOpen={isMembersModalOpen} onClose={() => setIsMembersModalOpen(false)} title={`Members of ${selectedClub?.name}`}>
                <div className="data-table-container">
                    <table className="data-table">
                        <thead><tr><th>Name</th><th>Role</th><th style={{ textAlign: 'right' }}>Actions</th></tr></thead>
                        <tbody>
                            {currentMembers.length === 0 ? <tr><td colSpan="3" style={{ textAlign: 'center' }}>No members found.</td></tr> : currentMembers.map(m => (
                                <tr key={m.id}>
                                    <td>{m.studentName}{m.enrollmentId ? <span style={{ fontSize: '0.75rem', fontFamily: 'monospace', color: '#718096', marginLeft: '6px' }}>({m.enrollmentId})</span> : null}</td>
                                    <td><span className="badge badge-secondary">{m.role}</span></td>
                                    <td style={{ textAlign: 'right' }}><button className="btn btn-sm btn-danger" onClick={() => handleRemoveMember(m.studentId)}>Remove</button></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </Modal>

            <Modal isOpen={isRequestsModalOpen} onClose={() => setIsRequestsModalOpen(false)} title={`Pending Requests for ${selectedClub?.name}`}>
                <div className="data-table-container">
                    <table className="data-table">
                        <thead><tr><th>Student</th><th>Applied On</th><th style={{ textAlign: 'right' }}>Actions</th></tr></thead>
                        <tbody>
                            {pendingMemberships.length === 0 ? <tr><td colSpan="3" style={{ textAlign: 'center' }}>No pending requests.</td></tr> : pendingMemberships.map(pm => (
                                <tr key={pm.id}>
                                    <td>{pm.studentName}{pm.enrollmentId ? <span style={{ fontSize: '0.75rem', fontFamily: 'monospace', color: '#718096', marginLeft: '6px' }}>({pm.enrollmentId})</span> : null}</td>
                                    <td>{new Date(pm.joinedAt).toLocaleDateString()}</td>
                                    <td style={{ textAlign: 'right' }}>
                                        <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                                            <button className="btn btn-sm btn-success" onClick={() => handleApprove(pm.id)}>Approve</button>
                                            <button className="btn btn-sm btn-danger" onClick={() => handleReject(pm.id)}>Reject</button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </Modal>

            <Modal isOpen={isAnnouncementsModalOpen} onClose={() => setIsAnnouncementsModalOpen(false)} title={`Announcements for ${selectedClub?.name}`}>
                <div style={{ marginBottom: '20px', padding: '15px', background: '#f8fafc', borderRadius: '8px' }}>
                    <h4 style={{ margin: '0 0 10px 0' }}>Post New Announcement</h4>
                    <input className="form-control" placeholder="Title" value={newAnn.title} onChange={e => setNewAnn({ ...newAnn, title: e.target.value })} style={{ marginBottom: '10px' }} />
                    <textarea className="form-control" placeholder="Content" rows="3" value={newAnn.content} onChange={e => setNewAnn({ ...newAnn, content: e.target.value })} style={{ marginBottom: '10px' }} />
                    <button className="btn btn-success" onClick={handlePostAnnouncement}>Post Announcement</button>
                </div>
                <div>
                    <h4 style={{ margin: '0 0 10px 0' }}>Past Announcements</h4>
                    {announcements.length === 0 ? <p style={{ color: '#718096' }}>No announcements yet.</p> : announcements.map(a => (
                        <div key={a.id} className="stat-card" style={{ padding: '15px', marginBottom: '10px', boxShadow: 'none', border: '1px solid #e2e8f0' }}>
                            <div style={{ fontWeight: 'bold' }}>{a.title}</div>
                            <div style={{ fontSize: '0.8rem', color: '#718096', marginBottom: '5px' }}>Posted by {a.posterName} on {new Date(a.postedAt).toLocaleString()}</div>
                            <div style={{ fontSize: '0.9rem' }}>{a.content}</div>
                        </div>
                    ))}
                </div>
            </Modal>
        </div>
    );
};

export default ClubsPage;
