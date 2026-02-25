import React, { useState, useEffect } from 'react';
import {
    getDepartments, addDepartment, updateDepartment, deleteDepartment,
    getRoles, addRole, deleteRole,
    getUsers, deleteUser
} from '../services/instituteService';

// ---- Permission Tree Data ----
const PERMISSION_MODULES = [
    { id: 'students', label: '🎓 Students', actions: ['View', 'Create', 'Edit', 'Delete'] },
    { id: 'faculty', label: '👩‍🏫 Faculty', actions: ['View', 'Create', 'Edit', 'Delete'] },
    { id: 'courses', label: '📚 Courses', actions: ['View', 'Create', 'Edit', 'Delete'] },
    { id: 'grades', label: '📊 Grades', actions: ['View', 'Enter', 'Edit', 'Bulk Entry'] },
    { id: 'fees', label: '💰 Fees', actions: ['View', 'Record Payment', 'Manage'] },
    { id: 'attendance', label: '📅 Attendance', actions: ['View', 'Mark', 'Bulk Mark'] },
    { id: 'library', label: '📖 Library', actions: ['View', 'Issue Book', 'Return Book', 'Manage'] },
    { id: 'events', label: '🎪 Events', actions: ['View', 'Register', 'Create', 'Manage'] },
    { id: 'clubs', label: '👥 Clubs', actions: ['View', 'Join', 'Create', 'Manage'] },
    { id: 'hostel', label: '🏠 Hostel', actions: ['View', 'Allocate', 'Manage'] },
    { id: 'placements', label: '💼 Placements', actions: ['View', 'Apply', 'Manage'] },
    { id: 'reports', label: '📋 Reports', actions: ['View', 'Export'] },
    { id: 'audit', label: '🗒️ Audit Log', actions: ['View', 'Export'] },
    { id: 'settings', label: '⚙️ Settings', actions: ['View', 'Edit'] },
];

const ROLE_DEFAULTS = {
    ADMIN: Object.fromEntries(PERMISSION_MODULES.map(m => [m.id, Object.fromEntries(m.actions.map(a => [a, true]))])),
    FACULTY: Object.fromEntries(PERMISSION_MODULES.map(m => [m.id, Object.fromEntries(m.actions.map(a => [a,
        ['students', 'courses', 'grades', 'attendance', 'events', 'clubs', 'placements', 'reports'].includes(m.id)
        && ['View', 'Mark', 'Bulk Mark', 'Enter', 'Register', 'Export'].includes(a)
    ]))])),
    STUDENT: Object.fromEntries(PERMISSION_MODULES.map(m => [m.id, Object.fromEntries(m.actions.map(a => [a,
        ['grades', 'fees', 'attendance', 'events', 'clubs', 'library', 'placements'].includes(m.id)
        && ['View', 'Register', 'Join', 'Apply'].includes(a)
    ]))])),
};

const InstituteManagementPage = () => {
    const [activeTab, setActiveTab] = useState('departments');
    const [data, setData] = useState([]);

    // Forms
    const [deptForm, setDeptForm] = useState({ id: null, name: '', description: '' });
    const [roleForm, setRoleForm] = useState({ name: '', description: '' });

    // Permission Tree state
    const [permRole, setPermRole] = useState('ADMIN');
    const [permissions, setPermissions] = useState(ROLE_DEFAULTS.ADMIN);
    const [permSaved, setPermSaved] = useState(false);

    useEffect(() => {
        loadData();
        // eslint-disable-next-line
    }, [activeTab]);

    const loadData = async () => {
        try {
            let res;
            if (activeTab === 'departments') res = await getDepartments();
            if (activeTab === 'roles') res = await getRoles();
            if (activeTab === 'users') res = await getUsers();
            setData(res?.data || []);
        } catch (err) {
            console.error(err);
        }
    };

    // --- Departments ---
    const handleSaveDept = async (e) => {
        e.preventDefault();
        try {
            if (deptForm.id) {
                await updateDepartment(deptForm.id, deptForm);
            } else {
                await addDepartment(deptForm);
            }
            setDeptForm({ id: null, name: '', description: '' });
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
        setPermissions(ROLE_DEFAULTS[role] || ROLE_DEFAULTS.ADMIN);
        setPermSaved(false);
    };

    const togglePermission = (moduleId, action) => {
        setPermissions(prev => ({
            ...prev,
            [moduleId]: { ...prev[moduleId], [action]: !prev[moduleId]?.[action] }
        }));
        setPermSaved(false);
    };

    const toggleModule = (moduleId, actions) => {
        const allChecked = actions.every(a => permissions[moduleId]?.[a]);
        setPermissions(prev => ({
            ...prev,
            [moduleId]: Object.fromEntries(actions.map(a => [a, !allChecked]))
        }));
        setPermSaved(false);
    };

    const handleSavePermissions = () => {
        const stored = JSON.parse(localStorage.getItem('rolePermissions') || '{}');
        stored[permRole] = permissions;
        localStorage.setItem('rolePermissions', JSON.stringify(stored));
        setPermSaved(true);
    };

    const totalGranted = PERMISSION_MODULES.reduce((sum, m) =>
        sum + m.actions.filter(a => permissions[m.id]?.[a]).length, 0);
    const totalPossible = PERMISSION_MODULES.reduce((sum, m) => sum + m.actions.length, 0);

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
                                <label>Description</label>
                                <input required type="text" value={deptForm.description} onChange={e => setDeptForm({ ...deptForm, description: e.target.value })} />
                            </div>
                            <div className="form-group" style={{ gridColumn: '1 / -1', display: 'flex', gap: '10px' }}>
                                <button type="submit" className="btn btn-primary">{deptForm.id ? 'Update' : 'Create'}</button>
                                {deptForm.id && <button type="button" className="btn btn-secondary" onClick={() => setDeptForm({ id: null, name: '', description: '' })}>Cancel</button>}
                            </div>
                        </form>
                    </div>
                    <div className="data-table-container" style={{ flex: 2 }}>
                        <table className="data-table">
                            <thead><tr><th>ID</th><th>Name</th><th>Description</th><th>Actions</th></tr></thead>
                            <tbody>
                                {data.map(d => (
                                    <tr key={d.id}>
                                        <td>{d.id}</td><td>{d.name}</td><td>{d.description}</td>
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
                                {['ADMIN', 'FACULTY', 'STUDENT'].map(r => (
                                    <button key={r} onClick={() => handleRoleChange(r)} style={{
                                        padding: '6px 16px', borderRadius: '20px', border: '2px solid',
                                        borderColor: permRole === r ? '#3b82f6' : '#e2e8f0',
                                        background: permRole === r ? '#3b82f6' : 'white',
                                        color: permRole === r ? 'white' : '#4a5568',
                                        fontWeight: '600', fontSize: '0.85rem', cursor: 'pointer', transition: 'all 0.15s'
                                    }}>{r}</button>
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
                            ✅ Permissions for <strong>{permRole}</strong> saved to local config.
                            <small style={{ display: 'block', opacity: 0.8 }}>Note: Backend enforcement requires API-level role guards.</small>
                        </div>
                    )}

                    {/* Permission tree */}
                    <div style={{ background: 'white', border: '1px solid #e2e8f0', borderRadius: '10px', overflow: 'hidden' }}>
                        {PERMISSION_MODULES.map((module, mi) => {
                            const allChecked = module.actions.every(a => permissions[module.id]?.[a]);
                            const someChecked = module.actions.some(a => permissions[module.id]?.[a]);
                            return (
                                <div key={module.id} style={{ borderBottom: mi < PERMISSION_MODULES.length - 1 ? '1px solid #f0f0f0' : 'none' }}>
                                    <div style={{
                                        display: 'flex', alignItems: 'center', gap: '12px',
                                        padding: '11px 20px',
                                        background: someChecked ? '#f7fbff' : 'white',
                                    }}>
                                        <input
                                            type="checkbox"
                                            checked={allChecked}
                                            ref={el => { if (el) el.indeterminate = someChecked && !allChecked; }}
                                            onChange={() => toggleModule(module.id, module.actions)}
                                            style={{ width: '16px', height: '16px', cursor: 'pointer', flexShrink: 0 }}
                                        />
                                        <span style={{ fontWeight: '700', fontSize: '0.9rem', color: '#2d3748', minWidth: '155px' }}>{module.label}</span>
                                        <div style={{ display: 'flex', gap: '7px', flexWrap: 'wrap' }}>
                                            {module.actions.map(action => {
                                                const granted = !!permissions[module.id]?.[action];
                                                return (
                                                    <label key={action} style={{
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
                                                            onChange={() => togglePermission(module.id, action)}
                                                            style={{ margin: 0, cursor: 'pointer' }}
                                                        />
                                                        {action}
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
