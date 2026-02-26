import api from './api';

export const getDepartments = () => api.get('/departments');
export const addDepartment = (data) => api.post('/departments', data);
export const updateDepartment = (id, data) => api.put(`/departments/${id}`, data);
export const deleteDepartment = (id) => api.delete(`/departments/${id}`);

export const getRoles = () => api.get('/roles');
export const addRole = (data) => api.post('/roles', data);
export const deleteRole = (id) => api.delete(`/roles/${id}`);

export const getUsers = () => api.get('/users');
export const deleteUser = (id) => api.delete(`/users/${id}`);

export const getAllPermissions = () => api.get('/roles/permissions');
export const getRolePermissions = (roleId) => api.get(`/roles/${roleId}/permissions`);
export const setRolePermissions = (roleId, permissionIds) => api.put(`/roles/${roleId}/permissions`, permissionIds);
