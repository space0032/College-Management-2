import axios from 'axios';
import SessionManager from '../utils/SessionManager';

const API = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:7000/api',
  timeout: 10000,
});

API.interceptors.request.use((config) => {
  const token = SessionManager.getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Centralized session teardown clears the cached user too.
      SessionManager.clearSession();
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default API;
