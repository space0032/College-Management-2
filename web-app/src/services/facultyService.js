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
