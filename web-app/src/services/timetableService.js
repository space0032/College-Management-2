import API from './api';

export const getTimetable = (department, semester, specialization) => {
  let url = `/timetable?department=${encodeURIComponent(department)}&semester=${encodeURIComponent(semester)}`;
  if (specialization && String(specialization).trim()) {
    url += `&specialization=${encodeURIComponent(String(specialization).trim())}`;
  }
  return API.get(url);
};
export const saveTimetableEntry = (entry) => API.post('/timetable', entry);
export const deleteTimetableEntry = (id) => API.delete(`/timetable/${id}`);
