import React, { useState, useEffect } from 'react';
import { getEmployees, addEmployee, updateEmployee } from '../services/employeeService';

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
        id: 0,
        employeeId: '',
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        designation: '',
        joinDate: '',
        salary: 0,
        status: 'ACTIVE'
    });

    const currentUser = (() => {
        try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; }
    })();

    useEffect(() => {
        fetchEmployees();
    }, []);

    useEffect(() => {
        filterData();
    }, [employees, searchQuery, statusFilter]);

    const fetchEmployees = async () => {
        try {
            setLoading(true);
            const res = await getEmployees();
            setEmployees(res.data || []);
            setError(null);
        } catch (err) {
            console.error(err);
            setError('Failed to load employee records.');
        } finally {
            setLoading(false);
        }
    };

    const filterData = () => {
        let result = employees;
        if (statusFilter !== 'ALL') {
            result = result.filter(e => e.status === statusFilter);
        }
        if (searchQuery) {
            const q = searchQuery.toLowerCase();
            result = result.filter(e =>
                (e.firstName && e.firstName.toLowerCase().includes(q)) ||
                (e.lastName && e.lastName.toLowerCase().includes(q)) ||
                (e.employeeId && e.employeeId.toLowerCase().includes(q)) ||
                (e.designation && e.designation.toLowerCase().includes(q))
            );
        }
        setFilteredEmployees(result);
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const openAddModal = () => {
        setFormData({
            id: 0,
            employeeId: '',
            firstName: '',
            lastName: '',
            email: '',
            phone: '',
            designation: '',
            joinDate: new Date().toISOString().split('T')[0],
            salary: 0,
            status: 'ACTIVE'
        });
        setIsEditing(false);
        setShowModal(true);
    };

    const openEditModal = (emp) => {
        setFormData({
            id: emp.id,
            employeeId: emp.employeeId || '',
            firstName: emp.firstName || '',
            lastName: emp.lastName || '',
            email: emp.email || '',
            phone: emp.phone || '',
            designation: emp.designation || '',
            joinDate: emp.joinDate ? emp.joinDate.split('T')[0] : '',
            salary: emp.salary || 0,
            status: emp.status || 'ACTIVE'
        });
        setIsEditing(true);
        setShowModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // Validate inputs implicitly handled by HTML5
            if (isEditing) {
                await updateEmployee(formData);
            } else {
                await addEmployee(formData);
            }
            setShowModal(false);
            fetchEmployees();
        } catch (err) {
            console.error(err);
            alert(`Failed to ${isEditing ? 'update' : 'add'} employee.`);
        }
    };

    const getStatusBadge = (status) => {
        switch (status) {
            case 'ACTIVE': return <span className="status-badge status-active">ACTIVE</span>;
            case 'ON_LEAVE': return <span className="status-badge status-pending">ON LEAVE</span>;
            case 'RESIGNED': return <span className="status-badge" style={{ backgroundColor: '#e0e0e0', color: '#616161' }}>RESIGNED</span>;
            case 'TERMINATED': return <span className="status-badge status-rejected">TERMINATED</span>;
            default: return <span className="status-badge">{status}</span>;
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount);
    };

    if (loading) return <div className="page-container">Loading employees...</div>;
    if (currentUser.role !== 'ADMIN') return <div className="page-container" style={{ color: 'red', textAlign: 'center' }}><h2>Access Denied</h2><p>Only Administrators can manage employee records.</p></div>;

    return (
        <div className="page-container">
            <div className="page-header">
                <div>
                    <h2>👨‍💼 Employee Management</h2>
                    <p className="text-muted">Manage staff profiles, track salaries, and monitor active statuses.</p>
                </div>
                <button className="btn btn-primary" onClick={openAddModal}>+ Add Employee</button>
            </div>

            <div className="filters-section" style={{ display: 'flex', gap: '15px', marginBottom: '20px' }}>
                <input
                    type="text"
                    placeholder="Search by Name, ID, or Designation..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    style={{ flex: 1, padding: '10px', borderRadius: '4px', border: '1px solid #ddd' }}
                />
                <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    style={{ padding: '10px', borderRadius: '4px', border: '1px solid #ddd' }}
                >
                    <option value="ALL">All Statuses</option>
                    <option value="ACTIVE">Active</option>
                    <option value="ON_LEAVE">On Leave</option>
                    <option value="RESIGNED">Resigned</option>
                    <option value="TERMINATED">Terminated</option>
                </select>
            </div>

            {error && <div style={{ color: 'red', marginBottom: '15px' }}>{error}</div>}

            <div className="data-table-container">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>Employee ID</th>
                            <th>Name</th>
                            <th>Designation</th>
                            <th>Email</th>
                            <th>Join Date</th>
                            <th>Salary</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredEmployees.length === 0 ? (
                            <tr><td colSpan="8" style={{ textAlign: 'center' }}>No employees found</td></tr>
                        ) : (
                            filteredEmployees.map(emp => (
                                <tr key={emp.employeeId || emp.id}>
                                    <td style={{ fontWeight: '500' }}>{emp.employeeId}</td>
                                    <td>
                                        {emp.firstName} {emp.lastName}
                                    </td>
                                    <td><span className="status-badge" style={{ backgroundColor: '#e3f2fd', color: '#1565c0' }}>{emp.designation}</span></td>
                                    <td>{emp.email}</td>
                                    <td>{emp.joinDate || 'N/A'}</td>
                                    <td>{formatCurrency(emp.salary)}</td>
                                    <td>{getStatusBadge(emp.status)}</td>
                                    <td>
                                        <button className="btn btn-secondary" onClick={() => openEditModal(emp)} style={{ padding: '5px 10px' }}>
                                            Edit
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {showModal && (
                <div className="modal-overlay">
                    <div className="modal-content" style={{ maxWidth: '600px' }}>
                        <div className="modal-header">
                            <h2>{isEditing ? 'Edit Employee Profile' : 'Add New Employee'}</h2>
                            <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
                        </div>
                        <form onSubmit={handleSubmit} className="form-grid">

                            <div className="form-group">
                                <label>Employee ID</label>
                                <input required type="text" name="employeeId" value={formData.employeeId} onChange={handleInputChange} disabled={isEditing && formData.id !== 0} />
                            </div>

                            <div className="form-group">
                                <label>Designation / Role Title</label>
                                <input required type="text" name="designation" value={formData.designation} onChange={handleInputChange} />
                            </div>

                            <div className="form-group">
                                <label>First Name</label>
                                <input required type="text" name="firstName" value={formData.firstName} onChange={handleInputChange} />
                            </div>

                            <div className="form-group">
                                <label>Last Name</label>
                                <input type="text" name="lastName" value={formData.lastName} onChange={handleInputChange} />
                            </div>

                            <div className="form-group">
                                <label>Email Address</label>
                                <input required type="email" name="email" value={formData.email} onChange={handleInputChange} />
                            </div>

                            <div className="form-group">
                                <label>Phone Number</label>
                                <input type="text" name="phone" value={formData.phone} onChange={handleInputChange} />
                            </div>

                            <div className="form-group">
                                <label>Join Date</label>
                                <input type="date" name="joinDate" value={formData.joinDate} onChange={handleInputChange} required />
                            </div>

                            <div className="form-group">
                                <label>Monthly Salary (INR)</label>
                                <input required type="number" step="0.01" name="salary" value={formData.salary} onChange={handleInputChange} />
                            </div>

                            <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                                <label>Employment Status</label>
                                <select name="status" value={formData.status} onChange={handleInputChange} required>
                                    <option value="ACTIVE">ACTIVE - Currently employed</option>
                                    <option value="ON_LEAVE">ON LEAVE - Temporary absence</option>
                                    <option value="RESIGNED">RESIGNED - Voluntarily left</option>
                                    <option value="TERMINATED">TERMINATED - Fired/Released</option>
                                </select>
                            </div>

                            <div className="form-group" style={{ gridColumn: '1 / -1', marginTop: '10px' }}>
                                <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                                    {isEditing ? 'Update Employee Details' : 'Register Employee'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default EmployeeManagementPage;
