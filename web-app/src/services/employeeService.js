import api from './api';

export const getEmployees = () => api.get('/employees');

export const addEmployee = (employeeData) => api.post('/employees', employeeData);

export const updateEmployee = (employeeData) => api.put('/employees', employeeData);
