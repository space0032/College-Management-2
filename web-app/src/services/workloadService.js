import api from './api';

export const getWorkloadAnalytics = () => api.get('/workload/analytics');

export const getFacultyWorkload = (name) =>
    api.get(`/workload/faculty?name=${encodeURIComponent(name)}`);
