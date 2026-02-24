import API from './api';

export const getDrives = () => API.get('/placements/drives');
export const addDrive = (drive) => API.post('/placements/drives', drive);
export const deleteDrive = (id) => API.delete(`/placements/drives/${id}`);
export const getCompanies = () => API.get('/placements/companies');
export const addCompany = (company) => API.post('/placements/companies', company);
export const deleteCompany = (id) => API.delete(`/placements/companies/${id}`);
export const getApplicationsForStudent = (studentId) => API.get(`/placements/applications/student/${studentId}`);
export const getApplicationsForDrive = (driveId) => API.get(`/placements/applications/drive/${driveId}`);
export const applyForDrive = (data) => API.post('/placements/apply', data);
export const updateAppStatus = (appId, status) => API.put(`/placements/applications/${appId}/status`, { status });
