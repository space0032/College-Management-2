import API from './api';

export const getAllStudents = (page, size) => {
    if (page !== undefined && size !== undefined) {
        return API.get(`/students?page=${page}&size=${size}`);
    }
    return API.get('/students');
};
export const getStudentById = (id) => API.get(`/students/${id}`);
export const createStudent = (student) => API.post('/students', student);
export const updateStudent = (id, student) => API.put(`/students/${id}`, student);
export const deleteStudent = (id) => API.delete(`/students/${id}`);
export const searchStudents = (q) => API.get(`/students/search?q=${encodeURIComponent(q)}`);
export const downloadStudentTemplate = () => API.get('/students/template', { responseType: 'blob' });
export const getStudentCourses = (studentId, signal) => API.get(`/students/${studentId}/courses`, signal ? { signal } : undefined);
