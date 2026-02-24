import API from './api';

export const getPendingFees = () => API.get('/fees/pending');
export const getAllFees = () => API.get('/fees');
export const getPaymentHistory = (feeId) => API.get(`/fees/history/${feeId}`);
export const recordPayment = (paymentData) => API.post('/fees/pay', paymentData);
