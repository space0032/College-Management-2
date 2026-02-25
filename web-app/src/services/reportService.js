import api from './api';

export const generateVisitorReportPdf = (startDate, endDate) => api.post('/reports/visitors/pdf', { startDate, endDate });
export const getPlacementStats = () => api.get('/reports/placements/stats');
