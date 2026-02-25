import React, { useState, useEffect } from 'react';
import {
    getDepartments, addDepartment, updateDepartment, deleteDepartment,
    getRoles, addRole, deleteRole,
    getUsers, deleteUser
} from '../services/instituteService';

const InstituteManagementPage = () => {
    const [activeTab, setActiveTab] = useState('departments');
    const [data, setData] = useState([]);

    // Forms
    const [deptForm, setDeptForm] = useState({ id: null, name: '', description: '' });
    const [roleForm, setRoleForm] = useState({ name: '', description: '' });

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
            alert('Failed to load data.');
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

    return (
        <div className="page-container">
            <div className="page-header">
                <h2>Institute Management (RBAC)</h2>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <button className={`btn ${activeTab === 'departments' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('departments')}>
                        Departments
                    </button>
                    <button className={`btn ${activeTab === 'roles' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('roles')}>
                        Roles & Permissions
                    </button>
                    <button className={`btn ${activeTab === 'users' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setActiveTab('users')}>
                        User Accounts
                    </button>
                </div>
            </div>

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
                                        <td>{d.id}</td>
                                        <td>{d.name}</td>
                                        <td>{d.description}</td>
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
                                            {['ADMIN', 'STUDENT', 'FACULTY'].includes(r.name) ? (
                                                <small style={{ color: '#888' }}>System Role</small>
                                            ) : (
                                                <button className="btn btn-danger btn-sm" onClick={() => handleDeleteRole(r.id)}>Delete</button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {activeTab === 'users' && (
                <div className="data-table-container">
                    <table className="data-table">
                        <thead><tr><th>ID</th><th>Username</th><th>Assigned Role</th><th>State</th><th>Action</th></tr></thead>
                        <tbody>
                            {data.map(u => (
                                <tr key={u.id}>
                                    <td>{u.id}</td>
                                    <td><strong>{u.username}</strong></td>
                                    <td><span className="badge badge-secondary">{u.role}</span></td>
                                    <td><span className="badge badge-success">Active</span></td>
                                    <td>
                                        {u.role === 'ADMIN' ? (
                                            <small style={{ color: '#888' }}>Protected</small>
                                        ) : (
                                            <button className="btn btn-danger btn-sm" onClick={() => handleDeleteUser(u.id)}>Revoke Account</button>
                                        )}
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
