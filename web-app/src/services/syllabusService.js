import api from './api';

export const getSyllabiBycourse = (courseId) =>
    api.get(`/syllabus?courseId=${courseId}`);

export const getAllSyllabi = () =>
    api.get('/syllabus');

export const addSyllabus = (data) =>
    api.post('/syllabus', data);

export const deleteSyllabus = (id) =>
    api.delete(`/syllabus/${id}`);
