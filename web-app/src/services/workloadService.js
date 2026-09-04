import api from './api';

export const getWorkloadAnalytics = () => api.get('/workload/analytics');

export const getFacultyWorkload = (name) =>
    api.get(`/workload/faculty?name=${encodeURIComponent(name)}`);

export const assignCourse = (courseId, facultyId) =>
    api.post(`/courses/${courseId}/assign`, { facultyId });

export const unassignCourse = (courseId) =>
    api.delete(`/courses/${courseId}/assign`);

export const checkConflict = (facultyId, courseId) =>
    api.get(`/workload/check-conflict?facultyId=${facultyId}&courseId=${courseId}`);

export const suggestCourses = (facultyId) =>
    api.get(`/workload/suggest?facultyId=${facultyId}`);
