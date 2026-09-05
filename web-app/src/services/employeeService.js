import api from './api';

export const getEmployees = (signal) => api.get('/employees', signal ? { signal } : undefined);

export const addEmployee = (employeeData) => api.post('/employees', employeeData);

export const updateEmployee = (employeeData) => api.put('/employees', employeeData);
