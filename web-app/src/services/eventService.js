import api from './api';

export const getEvents = (signal) => api.get('/events', signal ? { signal } : undefined);
export const getEvent = (id) => api.get(`/events/${id}`);
export const createEvent = (data) => api.post('/events', data);
export const updateEvent = (id, data) => api.put(`/events/${id}`, data);
export const deleteEvent = (id) => api.delete(`/events/${id}`);

export const registerEvent = (eventId, enrollmentId) => api.post(`/events/${eventId}/register`, { enrollmentId });
export const unregisterEvent = (eventId, enrollmentId) => api.post(`/events/${eventId}/unregister`, { enrollmentId });

export const getEventRegistrations = (eventId) => api.get(`/events/${eventId}/registrations`);
export const markAttendance = (registrationId, status) => api.put(`/events/registrations/${registrationId}/attendance`, { status });

export const getStudentEvents = (studentId, signal) => api.get(`/events/student/${studentId}`, signal ? { signal } : undefined);

// Budgeting
export const getEventBudgets = (eventId) => api.get(`/events/${eventId}/budget`);
export const addEventBudget = (eventId, data) => api.post(`/events/${eventId}/budget`, data);
export const updateBudgetActualCost = (id, data) => api.put(`/events/budget/${id}/actual-cost`, data);
export const deleteEventBudget = (id) => api.delete(`/events/budget/${id}`);

// Polls
export const getEventPolls = (eventId) => api.get(`/events/${eventId}/polls`);
export const createEventPoll = (eventId, data) => api.post(`/events/${eventId}/polls`, data);
export const closeEventPoll = (pollId) => api.put(`/events/polls/${pollId}/close`);
export const voteEventPoll = (pollId, data) => api.post(`/events/polls/${pollId}/vote`, data);
export const deleteEventPoll = (pollId) => api.delete(`/events/polls/${pollId}`);

// Event Collaborators
export const getEventCollaborators = (eventId) => api.get(`/event-details/${eventId}/collaborators`);
export const addEventCollaborator = (eventId, data) => api.post(`/event-details/${eventId}/collaborators`, data);
export const deleteEventCollaborator = (id) => api.delete(`/event-details/collaborators/${id}`);

// Event Resources
export const getEventResources = (eventId) => api.get(`/event-details/${eventId}/resources`);
export const addEventResource = (eventId, data) => api.post(`/event-details/${eventId}/resources`, data);
export const updateEventResourceStatus = (id, data) => api.put(`/event-details/resources/${id}`, data);
export const deleteEventResource = (id) => api.delete(`/event-details/resources/${id}`);

// Event Volunteers
export const getEventVolunteers = (eventId) => api.get(`/event-details/${eventId}/volunteers`);
export const registerEventVolunteer = (eventId, data) => api.post(`/event-details/${eventId}/volunteers`, data);
export const updateEventVolunteer = (id, data) => api.put(`/event-details/volunteers/${id}`, data);
