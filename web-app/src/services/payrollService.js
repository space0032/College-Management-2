import api from './api';

export const getPayroll = (month, year) =>
    api.get(`/payroll?month=${month}&year=${year}`);

export const generatePayroll = (month, year) =>
    api.post('/payroll', { month, year });

export const markAsPaid = (id) =>
    api.post('/payroll/mark-paid', { id });

export const markAllAsPaid = (month, year) =>
    api.post('/payroll/mark-all-paid', { month, year });

export const updatePayrollEntry = (id, bonuses, deductions) =>
    api.put(`/payroll/${id}`, { bonuses, deductions });

export const deletePayrollEntry = (id) =>
    api.delete(`/payroll/${id}`);
