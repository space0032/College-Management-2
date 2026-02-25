import api from './api';

export const getStudentGatePasses = (studentId) => api.get(`/gatepass/student/${studentId}`);
export const getPendingGatePasses = () => api.get('/gatepass/pending');
export const getAllGatePasses = () => api.get('/gatepass');
export const requestGatePass = (data) => api.post('/gatepass', data);
export const approveGatePass = (passId, approvedBy, comment) => api.put(`/gatepass/${passId}/approve`, { approvedBy, comment });
export const rejectGatePass = (passId, rejectedBy, comment) => api.put(`/gatepass/${passId}/reject`, { rejectedBy, comment });
