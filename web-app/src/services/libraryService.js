import API from './api';

export const getAllBooks = () => API.get('/library/books');
export const addBook = (book) => API.post('/library/books', book);
export const updateBook = (id, book) => API.put(`/library/books/${id}`, book);
