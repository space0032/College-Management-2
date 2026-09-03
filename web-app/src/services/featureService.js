import API from './api';

// Complaints
export const getAllComplaints = () => API.get('/complaints');
export const getStudentComplaints = (studentId) => API.get(`/complaints/student/${studentId}`);
export const createComplaint = (payload) => API.post('/complaints', payload);
export const updateComplaintStatus = (id, payload) => API.put(`/complaints/${id}/status`, payload);

// Hostel Attendance
export const markHostelAttendance = (payload) => API.post('/hostel/attendance/mark', payload);
export const getHostelAttendanceByDate = (date) => API.get(`/hostel/attendance/date/${date}`);
export const getHostelAttendanceByStudent = (studentId) => API.get(`/hostel/attendance/student/${studentId}`);

// Wardens
export const getWardens = () => API.get('/wardens');
export const getWardenById = (id) => API.get(`/wardens/${id}`);
export const addWarden = (warden) => API.post('/wardens', warden);
export const updateWarden = (id, warden) => API.put(`/wardens/${id}`, warden);
export const deleteWarden = (id) => API.delete(`/wardens/${id}`);

// Course Registration
export const getPendingRegistrations = () => API.get('/course-registrations/pending');
export const registerForCourse = (payload) => API.post('/course-registrations', payload);
export const approveRegistration = (id) => API.post(`/course-registrations/${id}/approve`);
export const rejectRegistration = (id) => API.post(`/course-registrations/${id}/reject`);
export const dropCourse = (studentId, courseId) =>
  API.delete(`/course-registrations/drop?studentId=${studentId}&courseId=${courseId}`);
export const getCourseRegistrationIds = (studentId) => API.get(`/course-registrations/student/${studentId}/ids`);
export const getEnrolledStudents = (courseId) => API.get(`/course-registrations/course/${courseId}/students`);

// Feedback
export const getStudentFeedback = (studentId) => API.get(`/feedback/student/${studentId}`);
export const submitFeedback = (payload) => API.post('/feedback', payload);

// Book Requests
export const getPendingBookRequests = () => API.get('/book-requests');
export const getStudentBookRequests = (studentId) => API.get(`/book-requests/student/${studentId}`);
export const createBookRequest = (payload) => API.post('/book-requests', payload);
export const approveBookRequest = (id) => API.post(`/book-requests/${id}/approve`);
export const rejectBookRequest = (id, remarks) => API.post(`/book-requests/${id}/reject`, { remarks });

// Fee Transactions
export const getStudentFeeTransactions = (studentId) => API.get(`/fee-transactions/student/${studentId}`);
export const recordFeeTransaction = (payload) => API.post('/fee-transactions', payload);
