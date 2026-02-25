import api from './api';

export const getVisitorByPhone = (phone) => api.get(`/visitors/phone/${phone}`);
export const getActiveVisitors = () => api.get('/visitors/active');
export const getAllVisitorLogs = () => api.get('/visitors/logs');
export const logVisitorEntry = (data) => api.post('/visitors/log/entry', data);
export const logVisitorExit = (logId) => api.put(`/visitors/log/${logId}/exit`);
