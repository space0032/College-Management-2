import api from './api';

export const getScholarships = (signal) => api.get('/scholarships', signal ? { signal } : undefined);
export const createScholarship = (data) => api.post('/scholarships', data);
export const applyForScholarship = (scholarshipId, data) => api.post(`/scholarships/${scholarshipId}/applications`, data);
export const getApplications = (scholarshipId) => api.get(`/scholarships/${scholarshipId}/applications`);
export const updateApplicationStatus = (scholarshipId, appId, status) => api.put(`/scholarships/${scholarshipId}/applications/${appId}/status`, { status });
