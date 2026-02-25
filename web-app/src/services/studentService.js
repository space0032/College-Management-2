import API from './api';

export const getAllStudents = () => API.get('/students');
export const getStudentById = (id) => API.get(`/students/${id}`);
export const createStudent = (student) => API.post('/students', student);
export const updateStudent = (id, student) => API.put(`/students/${id}`, student);
export const deleteStudent = (id) => API.delete(`/students/${id}`);
export const searchStudents = (q) => API.get(`/students/search?q=${encodeURIComponent(q)}`);
