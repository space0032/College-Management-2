import API from './api';

export const getHostels = () => API.get('/hostels');
export const addHostel = (hostel) => API.post('/hostels', hostel);
export const updateHostel = (id, hostel) => API.put(`/hostels/${id}`, hostel);
export const getRooms = () => API.get('/hostels/rooms');
export const addRoom = (room) => API.post('/hostels/rooms', room);
export const deleteHostel = (id) => API.delete(`/hostels/${id}`);
export const deleteRoom = (id) => API.delete(`/hostels/rooms/${id}`);
export const getAllocations = () => API.get('/hostels/allocations');
export const allocateRoom = (allocation) => API.post('/hostels/allocations', allocation);
export const vacateRoom = (id) => API.delete(`/hostels/allocations/${id}`);
