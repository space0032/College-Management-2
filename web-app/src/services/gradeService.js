import api from './api';

export const getAllGrades = () => api.get('/grades');
export const getStudentGrades = (studentId) => api.get(`/grades/student/${studentId}`);
export const getFacultyGrades = (facultyId) => api.get(`/grades/faculty/${facultyId}`);
export const getCourseGrades = (courseId) => api.get(`/grades/course/${courseId}`);
export const getStudentCGPA = (studentId) => api.get(`/grades/student/${studentId}/cgpa`);
export const getCourseGradeDistribution = (courseId) => api.get(`/grades/course/${courseId}/distribution`);

export const saveGrade = (gradeData) => api.post('/grades', gradeData);
export const bulkSaveGrade = (grades) => api.post('/grades/bulk', grades);
