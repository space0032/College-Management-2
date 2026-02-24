import api from './api';

export const getClubs = () => api.get('/clubs');
export const getClub = (id) => api.get(`/clubs/${id}`);
export const createClub = (clubData) => api.post('/clubs', clubData);
export const updateClub = (id, clubData) => api.put(`/clubs/${id}`, clubData);
export const deleteClub = (id) => api.delete(`/clubs/${id}`);

export const joinClub = (clubId, studentId) => api.post(`/clubs/${clubId}/join`, { studentId });
export const leaveClub = (clubId, studentId) => api.post(`/clubs/${clubId}/leave`, { studentId });

export const getClubMembers = (clubId) => api.get(`/clubs/${clubId}/members`);
export const getPendingMemberships = (clubId) => api.get(`/clubs/${clubId}/pending`);

export const approveMembership = (membershipId) => api.put(`/clubs/memberships/${membershipId}/approve`);
export const rejectMembership = (membershipId) => api.put(`/clubs/memberships/${membershipId}/reject`);

export const getStudentClubs = (studentId) => api.get(`/clubs/student/${studentId}`);
export const getMyMemberships = (studentId) => api.get(`/clubs/memberships/student/${studentId}`);
