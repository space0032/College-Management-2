import API from './api';

export const getAnnouncements = () => API.get('/announcements');
export const addAnnouncement = (announcement) => API.post('/announcements', announcement);
export const updateAnnouncement = (id, announcement) => API.put(`/announcements/${id}`, announcement);
export const deleteAnnouncement = (id) => API.delete(`/announcements/${id}`);
