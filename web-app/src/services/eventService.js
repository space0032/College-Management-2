import api from './api';

export const getEvents = () => api.get('/events');
export const getEvent = (id) => api.get(`/events/${id}`);
export const createEvent = (data) => api.post('/events', data);
export const updateEvent = (id, data) => api.put(`/events/${id}`, data);
export const deleteEvent = (id) => api.delete(`/events/${id}`);

export const registerEvent = (eventId, studentId) => api.post(`/events/${eventId}/register`, { studentId });
export const unregisterEvent = (eventId, studentId) => api.post(`/events/${eventId}/unregister`, { studentId });

export const getEventRegistrations = (eventId) => api.get(`/events/${eventId}/registrations`);
export const markAttendance = (registrationId, status) => api.put(`/events/registrations/${registrationId}/attendance`, { status });

export const getStudentEvents = (studentId) => api.get(`/events/student/${studentId}`);

// Budgeting
export const getEventBudgets = (eventId) => api.get(`/events/${eventId}/budget`);
export const addEventBudget = (eventId, data) => api.post(`/events/${eventId}/budget`, data);
export const deleteEventBudget = (id) => api.delete(`/events/budget/${id}`);

// Polls
export const getEventPolls = (eventId) => api.get(`/events/${eventId}/polls`);
export const createEventPoll = (eventId, data) => api.post(`/events/${eventId}/polls`, data);
export const closeEventPoll = (pollId) => api.put(`/events/polls/${pollId}/close`);
export const deleteEventPoll = (pollId) => api.delete(`/events/polls/${pollId}`); // Backend handles ID via split
