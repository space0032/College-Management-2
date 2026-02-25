import api from './api';

export const getMyVolunteerTasks = (userId) =>
    api.get(`/volunteers/my-tasks?userId=${userId}`);

export const getVolunteerOpportunities = () =>
    api.get('/volunteers/opportunities');

export const applyToVolunteer = (userId, eventId, taskDescription) =>
    api.post('/volunteers/apply', { userId, eventId, taskDescription });
