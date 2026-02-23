import API from './api';

export const getAllStudents = () => API.get('/students');
export const getStudentById = (id) => API.get(`/students/${id}`);
export const createStudent = (student) => API.post('/students', student);
export const searchStudents = (q) => API.get(`/students/search?q=${encodeURIComponent(q)}`);
