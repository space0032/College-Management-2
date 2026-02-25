import api from './api';

export const getAssignments = (role, userId) => {
    if (role === 'FACULTY') return api.get(`/assignments?facultyId=${userId}`);
    if (role === 'STUDENT') return api.get(`/assignments?courseId=1`); // Simplified for demo
    return api.get('/assignments'); // Admin viewing all
};

export const createAssignment = (data) => api.post('/assignments', data);

export const getSubmissions = (assignmentId) => api.get(`/assignments/${assignmentId}/submissions`);

export const getStudentSubmission = (assignmentId, studentId) => api.get(`/assignments/${assignmentId}/submissions/student/${studentId}`);

export const submitAssignment = (assignmentId, data) => api.post(`/assignments/${assignmentId}/submissions`, data);

export const gradeSubmission = (submissionId, data) => api.put(`/submissions/${submissionId}/grade`, data);
