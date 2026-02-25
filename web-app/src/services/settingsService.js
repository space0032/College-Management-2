import api from './api';

export const getSettings = () => api.get('/settings');

export const updateSettings = (settingsData) => api.put('/settings', settingsData);
