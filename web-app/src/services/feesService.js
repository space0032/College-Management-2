import API from './api';

export const getPendingFees = () => API.get('/fees/pending');
export const getAllFees = () => API.get('/fees');
export const getStudentFees = (studentId) => API.get(`/fees/student/${studentId}`);
export const getPaymentHistory = (feeId) => API.get(`/fees/history/${feeId}`);
export const recordPayment = (paymentData) => API.post('/fees/pay', paymentData);
export const getFeeCategories = () => API.get('/fees/categories');
export const createFeeEntry = (entryData) => API.post('/fees/entry', entryData);
export const searchFees = (params) => API.get('/fees/search', { params });
export const getFeeSummary = () => API.get('/fees/summary');
export const getFeeLedger = (studentId) => API.get(`/fees/ledger/student/${studentId}`);
export const postFeeAdjustment = (feeId, data) => API.post(`/fees/${feeId}/adjustments`, data);
export const getPaymentRequests = (status) => API.get('/fees/requests', { params: status ? { status } : {} });
export const createPaymentRequest = (data) => API.post('/fees/requests', data);
export const reviewPaymentRequest = (id, data) => API.post(`/fees/requests/${id}/review`, data);
export const previewBulkFees = (data) => API.post('/fees/bulk/preview', data);
export const assignBulkFees = (data) => API.post('/fees/bulk', data);
export const getFeeReminders = () => API.get('/fees/reminders');
export const generateFeeReminders = (daysAhead = 7) => API.post('/fees/reminders/generate', { daysAhead });
export const getProgramFees = (department, academicYear, specialization) => {
  let url = `/fees/structure?department=${encodeURIComponent(department)}&academicYear=${encodeURIComponent(academicYear || new Date().getFullYear())}`;
  if (specialization && String(specialization).trim()) {
    url += `&specialization=${encodeURIComponent(String(specialization).trim())}`;
  }
  return API.get(url);
};
export const saveProgramFees = (department, academicYear, fees, specialization) =>
  API.put('/fees/structure', { department, academicYear, fees, specialization: specialization || '' });
