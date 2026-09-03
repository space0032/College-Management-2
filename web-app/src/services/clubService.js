import api from './api';

export const getClubs = () => api.get('/clubs');
export const getClub = (id) => api.get(`/clubs/${id}`);
export const createClub = (clubData) => api.post('/clubs', clubData);
export const updateClub = (id, clubData) => api.put(`/clubs/${id}`, clubData);
export const deleteClub = (id) => api.delete(`/clubs/${id}`);

export const joinClub = (clubId, enrollmentId) => api.post(`/clubs/${clubId}/join`, { enrollmentId });
export const leaveClub = (clubId, enrollmentId) => api.post(`/clubs/${clubId}/leave`, { enrollmentId });

export const getClubMembers = (clubId) => api.get(`/clubs/${clubId}/members`);
export const getPendingMemberships = (clubId) => api.get(`/clubs/${clubId}/pending`);

export const approveMembership = (membershipId) => api.put(`/clubs/memberships/${membershipId}/approve`);
export const rejectMembership = (membershipId) => api.put(`/clubs/memberships/${membershipId}/reject`);

export const getStudentClubs = (studentId) => api.get(`/clubs/student/${studentId}`);
export const getMyMemberships = (studentId) => api.get(`/clubs/memberships/student/${studentId}`);

export const getClubAnnouncements = (clubId) => api.get(`/clubs/${clubId}/announcements`);
export const postClubAnnouncement = (clubId, annData) => api.post(`/clubs/${clubId}/announcements`, annData);
