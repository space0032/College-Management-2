import api from './api';

export const getAllGrades = (signal) => api.get('/grades', signal ? { signal } : undefined);
export const getStudentGrades = (studentId, signal) => api.get(`/grades/student/${studentId}`, signal ? { signal } : undefined);
export const getFacultyGrades = (facultyId, signal) => api.get(`/grades/faculty/${facultyId}`, signal ? { signal } : undefined);
export const getCourseGrades = (courseId, signal) => api.get(`/grades/course/${courseId}`, signal ? { signal } : undefined);
export const getStudentCGPA = (studentId, signal) => api.get(`/grades/student/${studentId}/cgpa`, signal ? { signal } : undefined);
export const getCourseGradeDistribution = (courseId) => api.get(`/grades/course/${courseId}/distribution`);

export const saveGrade = (gradeData) => api.post('/grades', gradeData);
export const bulkSaveGrade = (grades) => api.post('/grades/bulk', grades);
