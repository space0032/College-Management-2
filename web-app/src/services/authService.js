import API from './api';

export const loginUser = (credentials) => API.post('/auth/login', credentials);
export const logoutUser = () => API.post('/auth/logout');
export const getSession = () => API.get('/auth/session');
