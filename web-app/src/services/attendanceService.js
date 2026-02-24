import API from './api';

export const getAttendance = (courseId, date) =>
  API.get(`/attendance?courseId=${encodeURIComponent(courseId)}&date=${encodeURIComponent(date)}`);
export const getStudentAttendance = (studentId) => API.get(`/attendance/student/${studentId}`);
export const getCourseStats = (courseId) => API.get(`/attendance/stats?courseId=${encodeURIComponent(courseId)}`);
export const markAttendance = (record) => API.post('/attendance', record);
export const bulkMarkAttendance = (records) => API.post('/attendance/bulk', records);
