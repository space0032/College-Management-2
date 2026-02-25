import api from './api';

export const getAuditLogs = (params = {}) => {
    const query = new URLSearchParams();
    if (params.userId) query.set('userId', params.userId);
    if (params.from) query.set('from', params.from);
    if (params.to) query.set('to', params.to);
    if (params.limit) query.set('limit', params.limit);
    const qs = query.toString();
    return api.get(`/audit${qs ? '?' + qs : ''}`);
};
