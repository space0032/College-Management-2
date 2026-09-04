import API from './api';

export const generateDraft = (feature, payload, signal) =>
  API.post('/ai/generate', { feature, ...payload }, signal ? { signal } : undefined);

export const getAiCapabilities = () => API.get('/ai/models');
