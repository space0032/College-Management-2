export const LEAVE_LIMITS = {
    SICK: 12,
    CASUAL: 5,
    EARNED: 15,
    DUTY: 999 // Effectively unlimited for on-duty
};

export const LEAVE_TYPES = {
    'SICK': { label: 'Sick Leave', icon: '🤒', color: '#ef4444', bg: '#fef2f2' },
    'CASUAL': { label: 'Casual Leave', icon: '🏖️', color: '#3b82f6', bg: '#eff6ff' },
    'EARNED': { label: 'Earned Leave', icon: '⭐', color: '#f59e0b', bg: '#fffbeb' },
    'DUTY': { label: 'On-Duty', icon: '💼', color: '#10b981', bg: '#ecfdf5' }
};

export const calculateDays = (start, end) => {
    if (!start || !end) return 0;
    const s = new Date(start);
    const e = new Date(end);
    const diffTime = Math.abs(e - s);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    return diffDays;
};
