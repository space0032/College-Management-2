import api from './api';

export const getCampaigns = (signal) => api.get('/campaigns', signal ? { signal } : undefined);

export const createCampaign = (data) => api.post('/campaigns', data);

export const donateToCampaign = (campaignId, amount) => api.post(`/campaigns/${campaignId}/donate`, { amount });
