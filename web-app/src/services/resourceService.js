import api from './api';

export const getResources = (courseId = null) => {
    let url = '/resources';
    if (courseId) {
        url += `?courseId=${courseId}`;
    }
    return api.get(url);
};

export const getResourceCategories = () => api.get('/resources/categories');

export const addResource = (resourceData) => api.post('/resources', resourceData);

export const deleteResource = (id) => api.delete(`/resources/${id}`);

export const incrementDownload = (id) => api.post(`/resources/${id}/download`);
