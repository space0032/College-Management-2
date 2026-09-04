import API from './api';

// Generous timeout: the server may chain TokenRa -> OpenRouter before responding.
const AI_TIMEOUT_MS = 55000;

export const generateDraft = (feature, payload, signal) =>
  API.post('/ai/generate', { feature, ...payload },
    { ...(signal ? { signal } : {}), timeout: AI_TIMEOUT_MS });

export const getAiCapabilities = () => API.get('/ai/models');
