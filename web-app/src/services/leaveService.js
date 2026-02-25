import api from './api';

export const getStaffLeaves = (userId) => api.get(`/leaves/staff?userId=${userId}`);
export const getStudentLeaves = (studentId) => api.get(`/leaves/student?studentId=${studentId}`);
export const getAllPendingLeaves = () => api.get('/leaves/pending');

export const createStaffLeave = (leaveData) => api.post('/leaves/staff', leaveData);
export const createStudentLeave = (leaveData) => api.post('/leaves/student', leaveData);

export const updateStaffLeaveStatus = (id, data) => api.put(`/leaves/staff/${id}/status`, data);
export const updateStudentLeaveStatus = (id, data) => api.put(`/leaves/student/${id}/status`, data);
