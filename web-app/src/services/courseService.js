import API from './api';

export const getAllCourses = (page = 1, size = 10, search = '', signal) => {
  // Allow getAllCourses(1, 500, abortSignal) — treat AbortSignal in 3rd slot as signal.
  if (search && typeof search === 'object' && typeof search.aborted === 'boolean') {
    signal = search;
    search = '';
  }
  const q = search ? `&search=${encodeURIComponent(search)}` : '';
  return API.get(`/courses?page=${page}&size=${size}${q}`, signal ? { signal } : undefined);
};
export const getCourseById = (id) => API.get(`/courses/${id}`);
export const createCourse = (course) => API.post('/courses', course);
export const updateCourse = (id, course) => API.put(`/courses/${id}`, course);
export const deleteCourse = (id) => API.delete(`/courses/${id}`);
