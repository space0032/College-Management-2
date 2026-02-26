import API from './api';

// Disciplinary Records
export const getDisciplinaryRecords = () => API.get('/affairs/disciplinary');
export const createDisciplinaryRecord = (data) => API.post('/affairs/disciplinary', data);
export const updateDisciplinaryRecord = (id, data) => API.put(`/affairs/disciplinary/${id}`, data);
export const deleteDisciplinaryRecord = (id) => API.delete(`/affairs/disciplinary/${id}`);

// Grievance Tickets
export const getGrievanceTickets = () => API.get('/affairs/grievances');
export const createGrievanceTicket = (data) => API.post('/affairs/grievances', data);
export const updateGrievanceTicket = (id, data) => API.put(`/affairs/grievances/${id}`, data);

// Parent Communications
export const getParentComms = () => API.get('/affairs/communications');
export const sendParentComm = (data) => API.post('/affairs/communications', data);
