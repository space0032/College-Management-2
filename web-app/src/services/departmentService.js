import API from './api';

export const getDepartments = () => API.get('/departments');
export const addDepartment = (dept) => API.post('/departments', dept);
export const updateDepartment = (id, dept) => API.put(`/departments/${id}`, dept);
export const deleteDepartment = (id) => API.delete(`/departments/${id}`);
