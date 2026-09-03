import API from './api';

export const getPendingFees = () => API.get('/fees/pending');
export const getAllFees = () => API.get('/fees');
export const getPaymentHistory = (feeId) => API.get(`/fees/history/${feeId}`);
export const recordPayment = (paymentData) => API.post('/fees/pay', paymentData);
export const getFeeCategories = () => API.get('/fees/categories');
export const createFeeEntry = (entryData) => API.post('/fees/entry', entryData);
