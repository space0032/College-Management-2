import api from './api';

export const getAssignments = (role, userId, studentId, signal) => {
    const cfg = signal ? { signal } : undefined;
    if (role === 'FACULTY') return api.get(`/assignments?facultyId=${userId}`, cfg);
    if (role === 'STUDENT') {
        if (studentId) return api.get(`/assignments?studentId=${studentId}`, cfg);
        return api.get(`/assignments?studentId=${userId}`, cfg);
    }
    return api.get('/assignments', cfg); // Admin viewing all
};

export const getAssignmentsByCourseIds = (courseIds) => {
    const ids = (courseIds || []).join(',');
    return api.get(`/assignments?courseIds=${encodeURIComponent(ids)}`);
};

export const createAssignment = (data) => api.post('/assignments', data);

export const getSubmissions = (assignmentId) => api.get(`/assignments/${assignmentId}/submissions`);

export const getStudentSubmission = (assignmentId, studentId) => api.get(`/assignments/${assignmentId}/submissions/student/${studentId}`);

export const submitAssignment = (assignmentId, data) => api.post(`/assignments/${assignmentId}/submissions`, data);

export const gradeSubmission = (submissionId, data) => api.put(`/submissions/${submissionId}/grade`, data);
