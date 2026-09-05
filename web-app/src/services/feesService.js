import API from './api';

export const getPendingFees = () => API.get('/fees/pending');
export const getAllFees = () => API.get('/fees');
export const getPaymentHistory = (feeId) => API.get(`/fees/history/${feeId}`);
export const recordPayment = (paymentData) => API.post('/fees/pay', paymentData);
export const getFeeCategories = () => API.get('/fees/categories');
export const createFeeEntry = (entryData) => API.post('/fees/entry', entryData);
export const getProgramFees = (department, academicYear, specialization) => {
  let url = `/fees/structure?department=${encodeURIComponent(department)}&academicYear=${encodeURIComponent(academicYear || new Date().getFullYear())}`;
  if (specialization && String(specialization).trim()) {
    url += `&specialization=${encodeURIComponent(String(specialization).trim())}`;
  }
  return API.get(url);
};
export const saveProgramFees = (department, academicYear, fees, specialization) =>
  API.put('/fees/structure', { department, academicYear, fees, specialization: specialization || '' });
