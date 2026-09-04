import API from './api';

export const getAllFaculty = (page, size) => {
    if (page !== undefined && size !== undefined) {
        return API.get(`/faculty?page=${page}&size=${size}`);
    }
    return API.get('/faculty');
};
export const getFacultyById = (id) => API.get(`/faculty/${id}`);
export const createFaculty = (faculty) => API.post('/faculty', faculty);
export const updateFaculty = (id, faculty) => API.put(`/faculty/${id}`, faculty);
export const deleteFaculty = (id) => API.delete(`/faculty/${id}`);
export const searchFaculty = (q) => API.get(`/faculty/search?q=${encodeURIComponent(q)}`);

// Self-service endpoints
export const getMyProfile = () => API.get('/faculty/me');
export const updateMyProfile = (data) => API.put('/faculty/me', data);
export const getMyCourses = () => API.get('/faculty/me/courses');
export const getMyWorkload = () => API.get('/faculty/me/workload');
export const getMyFeedback = () => API.get('/faculty/me/feedback');
export const getMySchedule = () => API.get('/faculty/me/schedule');

// Bulk import
export const importFaculty = (formData) => API.post('/faculty/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
});
export const downloadTemplate = () => API.get('/faculty/template', { responseType: 'blob' });
