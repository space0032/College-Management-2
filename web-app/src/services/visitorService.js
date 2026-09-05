import api from './api';

export const getVisitorByPhone = (phone, signal) => api.get(`/visitors/phone/${phone}`, signal ? { signal } : undefined);
export const getActiveVisitors = (signal) => api.get('/visitors/active', signal ? { signal } : undefined);
export const getAllVisitorLogs = (signal) => api.get('/visitors/logs', signal ? { signal } : undefined);
export const logVisitorEntry = (data) => api.post('/visitors/log/entry', data);
export const logVisitorExit = (logId) => api.put(`/visitors/log/${logId}/exit`);
