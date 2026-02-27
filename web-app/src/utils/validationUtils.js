/**
 * Utility functions for safe parsing and validation.
 */

export const safeParseInt = (val, fallback = 0) => {
    const parsed = parseInt(val, 10);
    return isNaN(parsed) ? fallback : parsed;
};

export const safeParseFloat = (val, fallback = 0.0) => {
    const parsed = parseFloat(val);
    return isNaN(parsed) ? fallback : parsed;
};

export const isValidDate = (date) => {
    if (!date) return false;
    const d = new Date(date);
    return d instanceof Date && !isNaN(d);
};

export const formatDateForInput = (dateString) => {
    if (!dateString) return '';
    try {
        const d = new Date(dateString);
        if (isNaN(d.getTime())) return '';
        return d.toISOString().slice(0, 16);
    } catch {
        return '';
    }
};

export const validateFileSize = (file, maxMB = 5) => {
    if (!file) return true;
    const maxSize = maxMB * 1024 * 1024;
    return file.size <= maxSize;
};

export const truncateString = (str, length = 100) => {
    if (!str) return '';
    if (str.length <= length) return str;
    return str.slice(0, length) + '...';
};
