import api from './api';

export const getResources = (courseId = null, signal) => {
    let url = '/resources';
    if (courseId) {
        url += `?courseId=${courseId}`;
    }
    return api.get(url, signal ? { signal } : undefined);
};

export const getResourceCategories = (signal) => api.get('/resources/categories', signal ? { signal } : undefined);

export const addResource = (resourceData) => api.post('/resources', resourceData);

export const deleteResource = (id) => api.delete(`/resources/${id}`);

export const incrementDownload = (id) => api.post(`/resources/${id}/download`);
