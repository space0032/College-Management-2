import api from './api';

export const getMonthEvents = (year, month) => api.get(`/calendar/month/${year}/${month}`);
export const addCalendarEvent = (data) => api.post('/calendar/events', data);
export const updateCalendarEvent = (id, data) => api.put(`/calendar/events/${id}`, data);
export const deleteCalendarEvent = (id) => api.delete(`/calendar/events/${id}`);
