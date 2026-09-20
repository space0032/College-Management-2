import API from './api';

export const getAllBooks = () => API.get('/library/books');
export const addBook = (bookData) => API.post('/library/books', bookData);
export const updateBook = (id, book) => API.put(`/library/books/${id}`, book);
export const deleteBook = (id) => API.delete(`/library/books/${id}`);
export const getAllIssues = () => API.get('/library/issues');
export const getIssuesByStudent = (studentId) => API.get(`/library/issues/student/${studentId}`);
export const issueBook = (data) => API.post('/library/issue', data);
export const returnBook = (issueId, data) => API.post(`/library/return/${issueId}`, data);
export const getFines = (studentId) => API.get(`/library/fines/${studentId}`);
export const sendReminders = () => API.post('/library/send-reminders');
export const requestBook = (data) => API.post('/book-requests', data);
export const getBookRequests = () => API.get('/book-requests');
export const approveBookRequest = (id) => API.post(`/book-requests/${id}/approve`);
export const rejectBookRequest = (id, remarks = '') => API.post(`/book-requests/${id}/reject`, { remarks });
