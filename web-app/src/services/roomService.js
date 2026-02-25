import api from './api';

export const getRooms = () => api.get('/rooms');

export const checkAvailability = (day, timeSlot) =>
    api.get(`/rooms/availability?day=${encodeURIComponent(day)}&timeSlot=${encodeURIComponent(timeSlot)}`);
