import API from './api';

export const getAllCourses = (page = 1, size = 10, search = '') => {
  const q = search ? `&search=${encodeURIComponent(search)}` : '';
  return API.get(`/courses?page=${page}&size=${size}${q}`);
};
export const getCourseById = (id) => API.get(`/courses/${id}`);
export const createCourse = (course) => API.post('/courses', course);
export const updateCourse = (id, course) => API.put(`/courses/${id}`, course);
export const deleteCourse = (id) => API.delete(`/courses/${id}`);
