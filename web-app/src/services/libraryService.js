import API from './api';

export const getAllBooks = () => API.get('/library/books');
export const addBook = (bookData) => API.post('/library/books', bookData);
export const getAllIssues = () => API.get('/library/issues');
export const getIssuesByStudent = (studentId) => API.get(`/library/issues/student/${studentId}`);
export const issueBook = (data) => API.post('/library/issue', data);
export const returnBook = (issueId, data) => API.post(`/library/return/${issueId}`, data);
export const getFines = (studentId) => API.get(`/library/fines/${studentId}`);
export const updateBook = (id, book) => API.put(`/library/books/${id}`, book);
