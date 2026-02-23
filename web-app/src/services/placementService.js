import API from './api';

export const getDrives = () => API.get('/placements/drives');
export const addDrive = (drive) => API.post('/placements/drives', drive);
export const deleteDrive = (id) => API.delete(`/placements/drives/${id}`);
export const getCompanies = () => API.get('/placements/companies');
export const addCompany = (company) => API.post('/placements/companies', company);
export const deleteCompany = (id) => API.delete(`/placements/companies/${id}`);
