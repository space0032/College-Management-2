import API from './api';

export const getTimetable = (department, semester) =>
  API.get(`/timetable?department=${encodeURIComponent(department)}&semester=${encodeURIComponent(semester)}`);
export const saveTimetableEntry = (entry) => API.post('/timetable', entry);
export const deleteTimetableEntry = (id) => API.delete(`/timetable/${id}`);
