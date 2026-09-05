import api from './api';

export const getRooms = (filters = {}) => {
  const params = new URLSearchParams();
  if (filters.type && filters.type !== 'All') params.append('type', filters.type);
  if (filters.minCapacity) params.append('minCapacity', filters.minCapacity);
  if (filters.building) params.append('building', filters.building);
  const qs = params.toString();
  return api.get(`/rooms${qs ? `?${qs}` : ''}`);
};

export const checkAvailability = (day, timeSlot, filters = {}) => {
  const params = new URLSearchParams({ day, timeSlot });
  if (filters.type && filters.type !== 'All') params.append('type', filters.type);
  if (filters.minCapacity) params.append('minCapacity', filters.minCapacity);
  if (filters.building) params.append('building', filters.building);
  return api.get(`/rooms/availability?${params.toString()}`);
};

export const getFreeSlots = (day, roomNumber) =>
  api.get(`/rooms/free-slots?day=${encodeURIComponent(day)}&roomNumber=${encodeURIComponent(roomNumber)}`);

export const getDayGrid = (day, filters = {}) => {
  const params = new URLSearchParams({ day });
  if (filters.type && filters.type !== 'All') params.append('type', filters.type);
  if (filters.building) params.append('building', filters.building);
  return api.get(`/rooms/day-grid?${params.toString()}`);
};

export const createRoom = (room) => api.post('/rooms', room);

export const updateRoom = (id, room) => api.put(`/rooms/${id}`, room);

export const deleteRoom = (id) => api.delete(`/rooms/${id}`);
