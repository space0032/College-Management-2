import React, { useState, useEffect } from 'react';
import { getEmployees, addEmployee, updateEmployee } from '../services/employeeService';
import Modal from '../components/Modal';

const EmployeeManagementPage = () => {
    const [employees, setEmployees] = useState([]);
    const [filteredEmployees, setFilteredEmployees] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showModal, setShowModal] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState('ALL');

    const [formData, setFormData] = useState({
        id: 0, employeeId: '', firstName: '', lastName: '', email: '',
        phone: '', designation: '', joinDate: '', salary: 0, status: 'ACTIVE'
    });

    useEffect(() => { fetchEmployees(); }, []);
    useEffect(() => { filterData(); }, [employees, searchQuery, statusFilter]);

    const fetchEmployees = async () => {
        try {
            setLoading(true);
            const res = await getEmployees();
            setEmployees(res.data || []);
            setError(null);
        } catch (err) {
            setError('System error retrieving staff records.');
        } finally { setLoading(false); }
    };

    const filterData = () => {
        let result = employees;
        if (statusFilter !== 'ALL') result = result.filter(e => e.status === statusFilter);
        if (searchQuery) {
            const q = searchQuery.toLowerCase();
            result = result.filter(e =>
                (e.firstName + ' ' + e.lastName).toLowerCase().includes(q) ||
                e.employeeId.toLowerCase().includes(q) ||
                e.designation.toLowerCase().includes(q)
            );
        }
        setFilteredEmployees(result);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            if (isEditing) await updateEmployee(formData.id, formData);
            else await addEmployee(formData);
            setShowModal(false);
            fetchEmployees();
        } catch (err) { alert('Operation failed'); }
    };

    const handleEdit = (emp) => {
        setFormData(emp);
        setIsEditing(true);
        setShowModal(true);
    };

    const getAvatarColor = (name) => {
        const colors = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444'];
        const charCode = name.charCodeAt(0) || 0;
        return colors[charCode % colors.length];
    };

    // Stats
    const totalStaff = employees.length;
    const activeStaff = employees.filter(e => e.status === 'ACTIVE').length;
    const depts = [...new Set(employees.map(e => e.designation))].length;

    return (
        <div className="page-container" style={{ background: '#f8fafc', minHeight: '100vh', padding: '30px' }}>
            <div className="page-header" style={{ marginBottom: '30px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                        <h1 className="page-title">👥 Human Capital Management</h1>
                        <p className="page-subtitle">Unified staff directory, lifecycle management, and organizational hierarchy</p>
                    </div>
                    <button className="btn btn-primary" onClick={() => { setFormData({ id: 0, employeeId: '', firstName: '', lastName: '', email: '', phone: '', designation: '', joinDate: '', salary: 0, status: 'ACTIVE' }); setIsEditing(false); setShowModal(true); }}>
                        + Onboard Staff
                    </button>
                </div>
            </div>

            {/* Premium Stats Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px', marginBottom: '30px' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, #4f46e5 0%, #3b82f6 100%)', color: 'white' }}>
                    <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Total Workforce</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', margin: '8px 0' }}>{totalStaff}</div>
                    <div style={{ fontSize: '0.75rem', opacity: 0.8 }}>Non-Teaching & Support</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Active Status</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#10b981', margin: '8px 0' }}>{activeStaff}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Units / Designations</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#f59e0b', margin: '8px 0' }}>{depts}</div>
                </div>
                <div className="stat-card">
                    <div style={{ fontSize: '0.8rem', color: '#64748b' }}>Retention Rate</div>
                    <div style={{ fontSize: '1.8rem', fontWeight: 'bold', color: '#6366f1', margin: '8px 0' }}>98.2%</div>
                </div>
            </div>

            {/* Filter Bar */}
            <div className="stat-card" style={{ marginBottom: '30px', padding: '20px', display: 'flex', gap: '20px', alignItems: 'center' }}>
                <div style={{ flex: 1, position: 'relative' }}>
                    <input
                        type="text"
                        placeholder="Search by name, ID or role..."
                        className="form-control"
                        value={searchQuery}
                        onChange={e => setSearchQuery(e.target.value)}
                        style={{ paddingLeft: '40px' }}
                    />
                    <span style={{ position: 'absolute', left: '15px', top: '12px', color: '#94a3b8' }}>🔍</span>
                </div>
                <select className="form-control" style={{ width: '200px' }} value={statusFilter} onChange={e => setStatusFilter(e.target.value)}>
                    <option value="ALL">All Status</option>
                    <option value="ACTIVE">Active Only</option>
                    <option value="INACTIVE">Inactive / Former</option>
                </select>
            </div>

            {error ? (
                <div className="stat-card" style={{ color: '#ef4444', textAlign: 'center' }}>{error}</div>
            ) : loading ? (
                <div style={{ textAlign: 'center', padding: '50px', color: '#64748b' }}>📊 Synchronizing records...</div>
            ) : (
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '25px' }}>
                    {filteredEmployees.map(emp => (
                        <div key={emp.id} className="stat-card" style={{ display: 'flex', gap: '20px', alignItems: 'center', transition: 'box-shadow 0.2s' }}>
                            <div style={{
                                width: '60px', height: '60px', borderRadius: '15px',
                                background: getAvatarColor(emp.firstName), color: 'white',
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                fontSize: '1.5rem', fontWeight: '900', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)'
                            }}>
                                {emp.firstName.charAt(0)}{emp.lastName.charAt(0)}
                            </div>
                            <div style={{ flex: 1 }}>
                                <h4 style={{ margin: 0, fontSize: '1.1rem' }}>{emp.firstName} {emp.lastName}</h4>
                                <div style={{ fontSize: '0.8rem', color: 'var(--primary-color)', fontWeight: '600' }}>{emp.designation}</div>
                                <div style={{ fontSize: '0.75rem', color: '#94a3b8', marginTop: '4px' }}>ID: {emp.employeeId}</div>
                            </div>
                            <div style={{ textAlign: 'right' }}>
                                <span className={`badge ${emp.status === 'ACTIVE' ? 'badge-success' : 'badge-secondary'}`} style={{ fontSize: '0.6rem', padding: '4px 8px' }}>{emp.status}</span>
                                <div style={{ marginTop: '10px' }}>
                                    <button className="btn btn-sm btn-secondary" onClick={() => handleEdit(emp)} style={{ padding: '4px 8px' }}>Edit</button>
                                </div>
                            </div>
                        </div>
                    ))}
                    {filteredEmployees.length === 0 && (
                        <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '100px', color: '#94a3b8' }}>
                            <div style={{ fontSize: '3rem' }}>🚫</div>
                            <p>No matching staff profiles found.</p>
                        </div>
                    )}
                </div>
            )}

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '800px', padding: '40px' }}>
                        <h2 style={{ marginBottom: '25px' }}>{isEditing ? 'Modify Personnel Profile' : 'Staff Onboarding'}</h2>
                        <form onSubmit={handleSubmit} className="form-grid">
                            <div className="form-group">
                                <label>First Name *</label>
                                <input required className="form-control" type="text" value={formData.firstName} onChange={e => setFormData({ ...formData, firstName: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Last Name *</label>
                                <input required className="form-control" type="text" value={formData.lastName} onChange={e => setFormData({ ...formData, lastName: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Official ID *</label>
                                <input required className="form-control" type="text" value={formData.employeeId} onChange={e => setFormData({ ...formData, employeeId: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Designation / Unit *</label>
                                <input required className="form-control" type="text" value={formData.designation} onChange={e => setFormData({ ...formData, designation: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Work Email *</label>
                                <input required className="form-control" type="email" value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Phone Contact</label>
                                <input className="form-control" type="text" value={formData.phone} onChange={e => setFormData({ ...formData, phone: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Join Date</label>
                                <input className="form-control" type="date" value={formData.joinDate} onChange={e => setFormData({ ...formData, joinDate: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Annual Salary (₹)</label>
                                <input className="form-control" type="number" value={formData.salary} onChange={e => setFormData({ ...formData, salary: parseFloat(e.target.value) })} />
                            </div>
                            <div className="form-group">
                                <label>Employment Status</label>
                                <select className="form-control" value={formData.status} onChange={e => setFormData({ ...formData, status: e.target.value })}>
                                    <option value="ACTIVE">Active</option>
                                    <option value="INACTIVE">Inactive / Former</option>
                                </select>
                            </div>
                            <div style={{ gridColumn: '1 / -1', display: 'flex', gap: '15px', marginTop: '20px' }}>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1, padding: '12px' }} onClick={() => setShowModal(false)}>Cancel</button>
                                <button type="submit" className="btn btn-primary" style={{ flex: 2, padding: '12px', fontWeight: 'bold' }}>{isEditing ? 'Commit Changes' : 'Initialize Profile'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default EmployeeManagementPage;
