import API from './api';

export const getNotifications = () => API.get('/notifications');
export const createNotification = (notification) => API.post('/notifications', notification);
