import React, { useState, useEffect } from 'react';
import {
    getDepartments, addDepartment, updateDepartment, deleteDepartment,
    getRoles, addRole, deleteRole,
    getUsers, deleteUser,
    getAllPermissions, getRolePermissions, setRolePermissions
} from '../services/instituteService';

// Delete static references now

const InstituteManagementPage = () => {
    const [activeTab, setActiveTab] = useState('departments');
    const [data, setData] = useState([]);

    // Forms
    const [deptForm, setDeptForm] = useState({ id: null, name: '', code: '', description: '' });
    const [roleForm, setRoleForm] = useState({ name: '', description: '' });

    // Permission Tree state
    const [permRole, setPermRole] = useState(null); // Will hold the whole role object
    const [allRoles, setAllRoles] = useState([]);
    const [systemPermissions, setSystemPermissions] = useState([]); // From backend
    const [rolePermissions, setRolePermissionsState] = useState(new Set()); // Set of permission IDs
    const [permSaved, setPermSaved] = useState(false);

    const loadData = React.useCallback(async () => {
        try {
            let res;
            if (activeTab === 'departments') { res = await getDepartments(); setData(res?.data || []); }
            if (activeTab === 'roles') { res = await getRoles(); setData(res?.data || []); }
            if (activeTab === 'users') { res = await getUsers(); setData(res?.data || []); }
            if (activeTab === 'permissions') {
                const rolesRes = await getRoles();
                setAllRoles(rolesRes?.data || []);
                if (rolesRes?.data?.length > 0) {
                    setPermRole(prev => prev || rolesRes.data[0]);
                }
                const permsRes = await getAllPermissions();
                setSystemPermissions(permsRes?.data || []);
            }
        } catch (err) {
            console.error(err);
        }
    }, [activeTab]);

    useEffect(() => {
        loadData();
    }, [loadData]);

    useEffect(() => {
        if (activeTab === 'permissions' && permRole) {
            // Load permissions for selected role
            getRolePermissions(permRole.id).then(res => {
                const ids = new Set((res.data || []).map(p => p.id));
                setRolePermissionsState(ids);
                setPermSaved(false);
            }).catch(err => console.error(err));
        }
    }, [permRole, activeTab]);

    // --- Departments ---
    const handleSaveDept = async (e) => {
        e.preventDefault();
        try {
            if (deptForm.id) {
                await updateDepartment(deptForm.id, deptForm);
            } else {
                await addDepartment(deptForm);
            }
            setDeptForm({ id: null, name: '', code: '', description: '' });
            loadData();
        } catch (err) {
            alert('Failed to save department');
        }
    };

    const handleDeleteDept = async (id) => {
        if (!window.confirm('Delete department?')) return;
        try {
            await deleteDepartment(id);
            loadData();
        } catch (err) {
            alert('Failed to delete department (might have active students).');
        }
    };

    // --- Roles ---
    const handleSaveRole = async (e) => {
        e.preventDefault();
        try {
            await addRole(roleForm);
            setRoleForm({ name: '', description: '' });
            loadData();
        } catch (err) {
            alert('Failed to add role');
        }
    };

    const handleDeleteRole = async (id) => {
        if (!window.confirm('Delete role?')) return;
        try {
            await deleteRole(id);
            loadData();
        } catch (err) {
            alert('Failed to delete role.');
        }
    };

    // --- Users ---
    const handleDeleteUser = async (id) => {
        if (!window.confirm('Delete User account? This is destructive and cascades.')) return;
        try {
            await deleteUser(id);
            loadData();
        } catch (err) {
            alert('Failed to delete user.');
        }
    };

    // --- Permission Tree ---
    const handleRoleChange = (role) => {
        setPermRole(role);
    };

    const togglePermission = (permId) => {
        setRolePermissionsState(prev => {
            const next = new Set(prev);
            if (next.has(permId)) next.delete(permId);
            else next.add(permId);
            return next;
        });
        setPermSaved(false);
    };

    const toggleCategory = (categoryPerms) => {
        const allChecked = categoryPerms.every(p => rolePermissions.has(p.id));
        setRolePermissionsState(prev => {
            const next = new Set(prev);
            categoryPerms.forEach(p => {
                if (allChecked) next.delete(p.id);
                else next.add(p.id);
            });
            return next;
        });
        setPermSaved(false);
    };

    const handleSavePermissions = async () => {
        if (!permRole) return;
        try {
            await setRolePermissions(permRole.id, Array.from(rolePermissions));
            setPermSaved(true);
        } catch (err) {
            alert('Failed to save permissions to backend');
        }
    };

    const totalGranted = rolePermissions.size;
    const totalPossible = systemPermissions.length;

    // Group permissions by category
    const groupedPermissions = systemPermissions.reduce((acc, p) => {
        const cat = p.category || 'Other';
        if (!acc[cat]) acc[cat] = [];
        acc[cat].push(p);
        return acc;
    }, {});

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Institute Management (RBAC)</h2>
                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                    <button className={`btn ${activeTab === 'departments' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('departments')}>Departments</button>
                    <button className={`btn ${activeTab === 'roles' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('roles')}>Roles</button>
                    <button className={`btn ${activeTab === 'permissions' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('permissions')}>🔐 Permission Tree</button>
                    <button className={`btn ${activeTab === 'users' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('users')}>User Accounts</button>
                </div>
            </div>

            {/* ===== DEPARTMENTS ===== */}
            {activeTab === 'departments' && (
                <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
                    <div className="stat-card" style={{ flex: 1 }}>
                        <h3>{deptForm.id ? 'Edit' : 'Add New'} Department</h3>
                        <form className="form-grid" onSubmit={handleSaveDept} style={{ marginTop: '15px' }}>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Department Name</label>
                                <input required type="text" value={deptForm.name} onChange={e => setDeptForm({ ...deptForm, name: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Department Code</label>
                                <input required maxLength="10" type="text" value={deptForm.code || ''} onChange={e => setDeptForm({ ...deptForm, code: e.target.value.toUpperCase() })} placeholder="e.g. CSE" />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Description</label>
                                <input required type="text" value={deptForm.description} onChange={e => setDeptForm({ ...deptForm, description: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1', display: 'flex', gap: '10px' }}>
                                <button type="submit" className="btn btn-primary">{deptForm.id ? 'Update' : 'Create'}</button>
                                {deptForm.id && <button type="button" className="btn btn-secondary" onClick={() => setDeptForm({ id: null, name: '', code: '', description: '' })}>Cancel</button>}
                            </div>
                        </form>
                    </div>
                    <div className="data-table-container" style={{ flex: 2 }}>
                        <table className="data-table">
                            <thead><tr><th>ID</th><th>Name</th><th>Code</th><th>Description</th><th>Actions</th></tr></thead>
                            <tbody>
                                {data.map(d => (
                                    <tr key={d.id}>
                                        <td>{d.id}</td><td>{d.name}</td><td>{d.code}</td><td>{d.description}</td>
                                        <td>
                                            <button className="btn btn-secondary btn-sm" onClick={() => setDeptForm(d)} style={{ marginRight: '5px' }}>Edit</button>
                                            <button className="btn btn-danger btn-sm" onClick={() => handleDeleteDept(d.id)}>Delete</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ===== ROLES ===== */}
            {activeTab === 'roles' && (
                <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
                    <div className="stat-card" style={{ flex: 1 }}>
                        <h3>Define New Role</h3>
                        <form className="form-grid" onSubmit={handleSaveRole} style={{ marginTop: '15px' }}>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Role Key (e.g., LIBRARIAN)</label>
                                <input required type="text" value={roleForm.name} style={{ textTransform: 'uppercase' }} onChange={e => setRoleForm({ ...roleForm, name: e.target.value.toUpperCase() })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Description</label>
                                <input type="text" value={roleForm.description} onChange={e => setRoleForm({ ...roleForm, description: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <button type="submit" className="btn btn-primary">Establish Role</button>
                            </div>
                        </form>
                    </div>
                    <div className="data-table-container" style={{ flex: 2 }}>
                        <table className="data-table">
                            <thead><tr><th>ID</th><th>Role Name</th><th>Description</th><th>Actions</th></tr></thead>
                            <tbody>
                                {data.map(r => (
                                    <tr key={r.id}>
                                        <td>{r.id}</td>
                                        <td><span className="badge badge-primary">{r.name}</span></td>
                                        <td>{r.description || '-'}</td>
                                        <td>
                                            {['ADMIN', 'STUDENT', 'FACULTY'].includes(r.name)
                                                ? <small style={{ color: '#888' }}>System Role</small>
                                                : <button className="btn btn-danger btn-sm" onClick={() => handleDeleteRole(r.id)}>Delete</button>}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {/* ===== PERMISSION TREE ===== */}
            {activeTab === 'permissions' && (
                <div>
                    {/* Header row: role pill selector + summary */}
                    <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', padding: '16px 20px', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '16px', flexWrap: 'wrap' }}>
                        <div style={{ flex: '1 1 250px' }}>
                            <div style={{ fontSize: '0.82rem', color: '#718096', marginBottom: '8px' }}>Configure permissions for role:</div>
                            <div style={{ display: 'flex', gap: '8px' }}>
                                {allRoles.map(r => (
                                    <button key={r.id} onClick={() => handleRoleChange(r)} style={{
                                        padding: '6px 16px', borderRadius: '20px', border: '2px solid',
                                        borderColor: permRole?.id === r.id ? '#3b82f6' : '#e2e8f0',
                                        background: permRole?.id === r.id ? '#3b82f6' : 'white',
                                        color: permRole?.id === r.id ? 'white' : '#4a5568',
                                        fontWeight: '600', fontSize: '0.85rem', cursor: 'pointer', transition: 'all 0.15s'
                                    }}>{r.name}</button>
                                ))}
                            </div>
                        </div>
                        <div style={{ display: 'flex', gap: '16px', alignItems: 'center', marginLeft: 'auto' }}>
                            <div style={{ fontSize: '0.82rem', color: '#718096' }}>
                                <span style={{ fontWeight: '700', fontSize: '1rem', color: '#2d3748' }}>{totalGranted}</span> / {totalPossible} permissions granted
                            </div>
                            <button className="btn btn-primary" onClick={handleSavePermissions} style={{ minWidth: '120px' }}>
                                {permSaved ? '✅ Saved' : '💾 Save'}
                            </button>
                        </div>
                    </div>

                    {permSaved && (
                        <div style={{ background: '#f0fff4', border: '1px solid #9ae6b4', borderRadius: '8px', padding: '10px 16px', marginBottom: '16px', color: '#276749', fontSize: '0.88rem' }}>
                            ✅ Permissions for <strong>{permRole?.name}</strong> saved to backend.
                        </div>
                    )}

                    {/* Permission tree */}
                    <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', overflow: 'hidden' }}>
                        {Object.entries(groupedPermissions).map(([category, perms], mi, arr) => {
                            const allChecked = perms.every(p => rolePermissions.has(p.id));
                            const someChecked = perms.some(p => rolePermissions.has(p.id));
                            return (
                                <div key={category} style={{ borderBottom: mi < arr.length - 1 ? '1px solid #f0f0f0' : 'none' }}>
                                    <div style={{
                                        display: 'flex', alignItems: 'center', gap: '12px',
                                        padding: '11px 20px',
                                        background: someChecked ? '#f7fbff' : 'white',
                                    }}>
                                        <input
                                            type="checkbox"
                                            checked={allChecked}
                                            ref={el => { if (el) el.indeterminate = someChecked && !allChecked; }}
                                            onChange={() => toggleCategory(perms)}
                                            style={{ width: '16px', height: '16px', cursor: 'pointer', flexShrink: 0 }}
                                        />
                                        <span style={{ fontWeight: '700', fontSize: '0.9rem', color: '#2d3748', minWidth: '155px' }}>{category}</span>
                                        <div style={{ display: 'flex', gap: '7px', flexWrap: 'wrap' }}>
                                            {perms.map(perm => {
                                                const granted = rolePermissions.has(perm.id);
                                                return (
                                                    <label key={perm.id} title={perm.description || perm.code} style={{
                                                        display: 'flex', alignItems: 'center', gap: '5px', cursor: 'pointer',
                                                        padding: '3px 10px', borderRadius: '20px', fontSize: '0.78rem', fontWeight: '500',
                                                        background: granted ? '#ebf8ff' : '#f7fafc',
                                                        color: granted ? '#2b6cb0' : '#a0aec0',
                                                        border: `1px solid ${granted ? '#bee3f8' : '#e2e8f0'}`,
                                                        transition: 'all 0.15s', userSelect: 'none'
                                                    }}>
                                                        <input
                                                            type="checkbox"
                                                            checked={granted}
                                                            onChange={() => togglePermission(perm.id)}
                                                            style={{ margin: 0, cursor: 'pointer' }}
                                                        />
                                                        {perm.name || perm.code}
                                                    </label>
                                                );
                                            })}
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </div>

                    <div style={{ marginTop: '16px', textAlign: 'right' }}>
                        <button className="btn btn-primary" onClick={handleSavePermissions} style={{ minWidth: '160px' }}>
                            {permSaved ? '✅ Saved' : '💾 Save Permissions'}
                        </button>
                    </div>
                </div>
            )}

            {/* ===== USERS ===== */}
            {activeTab === 'users' && (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead><tr><th>ID</th><th>Username</th><th>Role</th><th>State</th><th>Action</th></tr></thead>
                        <tbody>
                            {data.map(u => (
                                <tr key={u.id}>
                                    <td>{u.id}</td>
                                    <td><strong>{u.username}</strong></td>
                                    <td><span className="badge badge-secondary">{u.role}</span></td>
                                    <td><span className="badge badge-success">Active</span></td>
                                    <td>
                                        {u.role === 'ADMIN'
                                            ? <small style={{ color: '#888' }}>Protected</small>
                                            : <button className="btn btn-danger btn-sm" onClick={() => handleDeleteUser(u.id)}>Revoke Account</button>}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default InstituteManagementPage;
