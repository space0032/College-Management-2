import React, { useState, useEffect } from 'react';
import { getRoles, addRole, deleteRole, getUsers, updateUserRole } from '../services/instituteService';
import DataTable from '../components/DataTable';
import Modal from '../components/Modal';

const RoleManagementPage = () => {
    const user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();
    const [roles, setRoles] = useState([]);
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('roles');
    const [roleModal, setRoleModal] = useState(false);
    const [roleName, setRoleName] = useState('');
    const [roleDesc, setRoleDesc] = useState('');
    const [saving, setSaving] = useState(false);
    const [search, setSearch] = useState('');

    const fetchAll = React.useCallback(async () => {
        setLoading(true);
        try {
            const [r, u] = await Promise.all([getRoles(), getUsers()]);
            setRoles(r.data || []);
            setUsers(u.data || []);
        } catch { console.error('Failed to load roles/users'); }
        finally { setLoading(false); }
    }, []);

    useEffect(() => {
        if (user.role === 'ADMIN') {
            fetchAll();
        }
    }, [user.role, fetchAll]);

    if (user.role !== 'ADMIN') {
        return (
            <div className="page-container" style={{ textAlign: 'center', paddingTop: '80px' }}>
                <div style={{ fontSize: '4rem', marginBottom: '16px' }}>🔐</div>
                <h2>Admin Access Required</h2>
                <p style={{ color: '#64748b' }}>Role management is restricted to administrators.</p>
            </div>
        );
    }

    const handleAddRole = async (e) => {
        e.preventDefault();
        if (!roleName.trim()) return;
        setSaving(true);
        try {
            await addRole({ code: roleName.trim(), name: roleName.trim(), description: roleDesc.trim(), portalType: 'ADMIN' });
            setRoleModal(false); setRoleName(''); setRoleDesc('');
            fetchAll();
        } catch (err) { alert(err.response?.data?.error || 'Failed to create role.'); }
        finally { setSaving(false); }
    };

    const handleDeleteRole = async (id, name) => {
        if (!window.confirm(`Delete role "${name}"? This cannot be undone.`)) return;
        try { await deleteRole(id); fetchAll(); }
        catch { alert('Failed to delete role.'); }
    };

    const handleRoleChange = async (userId, roleId) => {
        if (!roleId) return;
        try {
            await updateUserRole(userId, Number(roleId));
            fetchAll();
        } catch (err) { alert(err.response?.data?.error || 'Failed to update role.'); }
    };

    const filteredUsers = users.filter(u =>
        !search || u.name?.toLowerCase().includes(search.toLowerCase()) ||
        u.username?.toLowerCase().includes(search.toLowerCase()) ||
        u.role?.toLowerCase().includes(search.toLowerCase())
    );

    const roleCounts = users.reduce((acc, u) => {
        acc[u.role] = (acc[u.role] || 0) + 1;
        return acc;
    }, {});

    const tabStyle = (t) => ({
        padding: '8px 18px', border: 'none', cursor: 'pointer',
        borderBottom: activeTab === t ? '3px solid #3b82f6' : '3px solid transparent',
        background: 'none', fontWeight: activeTab === t ? '600' : '400',
        color: activeTab === t ? '#3b82f6' : '#64748b', fontSize: '0.9rem'
    });

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h1 className="page-title">🔑 Role Management</h1>
                    <p className="page-subtitle">Manage system roles and user access control</p>
                </div>
                {activeTab === 'roles' && (
                    <button className="btn btn-primary" onClick={() => setRoleModal(true)}>+ Add Role</button>
                )}
            </div>

            {/* Stats */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '24px' }}>
                {[
                    { label: 'Total Roles', value: roles.length, icon: '🏷️', color: '#3b82f6' },
                    { label: 'Total Users', value: users.length, icon: '👥', color: '#10b981' },
                    { label: 'Admins', value: roleCounts['ADMIN'] || 0, icon: '👑', color: '#f59e0b' },
                    { label: 'Faculty', value: roleCounts['FACULTY'] || 0, icon: '👩‍🏫', color: '#8b5cf6' },
                ].map(s => (
                    <div key={s.label} className="stat-card" style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                        <div style={{ fontSize: '2rem', background: s.color + '18', padding: '12px', borderRadius: '10px' }}>{s.icon}</div>
                        <div>
                            <div style={{ fontSize: '0.8rem', color: '#64748b' }}>{s.label}</div>
                            <div style={{ fontSize: '1.6rem', fontWeight: '700', color: s.color }}>{s.value}</div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Tabs */}
            <div style={{ borderBottom: '1px solid #e2e8f0', marginBottom: '20px', display: 'flex' }}>
                <button style={tabStyle('roles')} onClick={() => setActiveTab('roles')}>🏷️ Roles ({roles.length})</button>
                <button style={tabStyle('users')} onClick={() => setActiveTab('users')}>👥 Users ({users.length})</button>
            </div>

            {loading ? (
                <div className="loading-container"><div className="spinner" /><span>Loading...</span></div>
            ) : activeTab === 'roles' ? (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px' }}>
                    {roles.length === 0 ? (
                        <p style={{ color: '#94a3b8', gridColumn: '1/-1', textAlign: 'center', padding: '40px' }}>No custom roles defined. System roles are managed automatically.</p>
                    ) : roles.map(role => (
                        <div key={role.id} className="stat-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                                    <span style={{ background: '#3b82f620', color: '#3b82f6', padding: '2px 10px', borderRadius: '20px', fontSize: '0.75rem', fontWeight: '700' }}>
                                        {role.name}
                                    </span>
                                    <span className="badge badge-secondary">{roleCounts[role.name] || 0} users</span>
                                </div>
                                <p style={{ margin: 0, fontSize: '0.82rem', color: '#64748b' }}>{role.description || 'No description provided'}</p>
                            </div>
                            {!['ADMIN', 'FACULTY', 'STUDENT'].includes(role.name) && (
                                <button className="btn btn-sm btn-danger" onClick={() => handleDeleteRole(role.id, role.name)}>🗑</button>
                            )}
                        </div>
                    ))}
                    {/* System roles info card */}
                    <div className="stat-card" style={{ border: '1px dashed #cbd5e1', background: '#f8fafc' }}>
                        <div style={{ fontSize: '0.8rem', color: '#94a3b8', marginBottom: '10px', fontWeight: '600' }}>SYSTEM ROLES (Protected)</div>
                        {['ADMIN', 'FACULTY', 'STUDENT'].map(r => (
                            <div key={r} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0', fontSize: '0.85rem' }}>
                                <span style={{ color: '#475569' }}>{r}</span>
                                <span className="badge badge-primary">{roleCounts[r] || 0}</span>
                            </div>
                        ))}
                    </div>
                </div>
            ) : (
                <>
                    <div style={{ marginBottom: '16px' }}>
                        <input
                            className="form-control" placeholder="Search by name, username, or role..."
                            value={search} onChange={e => setSearch(e.target.value)}
                            style={{ maxWidth: '400px' }}
                        />
                    </div>
                    <DataTable
                        columns={[
                            { key: 'id', label: 'ID' },
                            { key: 'name', label: 'Full Name' },
                            { key: 'username', label: 'Username' },
                            { key: 'email', label: 'Email' },
                            {
                                key: 'role', label: 'Role', render: (v, row) => (
                                    <select
                                        className="form-control"
                                        value={row.roleId || ''}
                                        onChange={e => handleRoleChange(row.id, e.target.value)}
                                        style={{ minWidth: '140px' }}
                                        title="Assign role to this user"
                                    >
                                        <option value="">-- Select --</option>
                                        {roles.map(r => (
                                            <option key={r.id} value={r.id}>{r.name || r.code}</option>
                                        ))}
                                    </select>
                                )
                            },
                            { key: 'department', label: 'Department' },
                        ]}
                        data={filteredUsers}
                        emptyMessage="No users found."
                    />
                </>
            )}

            {roleModal && (
                <Modal isOpen={roleModal} title="Create New Role" onClose={() => setRoleModal(false)} onSubmit={handleAddRole} submitLabel={saving ? 'Creating...' : 'Create Role'}>
                    <div className="form-group">
                        <label>Role Name *</label>
                        <input className="form-control" placeholder="e.g. LIBRARIAN, HOD" value={roleName} onChange={e => setRoleName(e.target.value.toUpperCase().replace(/\s/g, '_'))} />
                        <small style={{ color: '#94a3b8' }}>Auto-formatted to uppercase with underscores</small>
                    </div>
                    <div className="form-group">
                        <label>Description</label>
                        <textarea className="form-control" rows="2" placeholder="What can this role do?" value={roleDesc} onChange={e => setRoleDesc(e.target.value)} />
                    </div>
                </Modal>
            )}
        </div>
    );
};

export default RoleManagementPage;
