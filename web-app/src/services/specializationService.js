import API from './api';

export const getSpecializations = (departmentId) =>
  departmentId
    ? API.get(`/specializations?departmentId=${encodeURIComponent(departmentId)}`)
    : API.get('/specializations');
export const addSpecialization = (spec) => API.post('/specializations', spec);
export const updateSpecialization = (id, spec) => API.put(`/specializations/${id}`, spec);
export const deleteSpecialization = (id) => API.delete(`/specializations/${id}`);
