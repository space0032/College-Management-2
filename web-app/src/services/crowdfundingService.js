import api from './api';

export const getCampaigns = () => api.get('/campaigns');

export const createCampaign = (data) => api.post('/campaigns', data);

export const donateToCampaign = (campaignId, amount) => api.post(`/campaigns/${campaignId}/donate`, { amount });
