import API from './api';

export const getPendingFees = () => API.get('/fees/pending');
