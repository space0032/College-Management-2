import API from './api';

export const getAllCourses = () => API.get('/courses');
export const getCourseById = (id) => API.get(`/courses/${id}`);
export const createCourse = (course) => API.post('/courses', course);
export const updateCourse = (id, course) => API.put(`/courses/${id}`, course);
export const deleteCourse = (id) => API.delete(`/courses/${id}`);
