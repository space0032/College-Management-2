export const LEAVE_LIMITS = {
    SICK: 12,
    CASUAL: 5,
    EARNED: 15,
    DUTY: 999 // Effectively unlimited for on-duty
};

// Backend whitelist (LeaveController.VALID_STATUSES)
export const LEAVE_STATUSES = {
    PENDING: { label: 'Pending', color: '#d97706', bg: '#fffbeb' },
    APPROVED: { label: 'Approved', color: '#059669', bg: '#ecfdf5' },
    REJECTED: { label: 'Rejected', color: '#dc2626', bg: '#fef2f2' },
    CANCELLED: { label: 'Cancelled', color: '#64748b', bg: '#f1f5f9' }
};

export const LEAVE_TYPES = {
    'SICK': { label: 'Sick Leave', icon: '🤒', color: '#ef4444', bg: '#fef2f2' },
    'CASUAL': { label: 'Casual Leave', icon: '🏖️', color: '#3b82f6', bg: '#eff6ff' },
    'EARNED': { label: 'Earned Leave', icon: '⭐', color: '#f59e0b', bg: '#fffbeb' },
    'DUTY': { label: 'On-Duty', icon: '💼', color: '#10b981', bg: '#ecfdf5' }
};

export const calculateDays = (start, end) => {
    if (!start || !end) return 0;
    const s = new Date(String(start).slice(0, 10));
    const e = new Date(String(end).slice(0, 10));
    if (isNaN(s.getTime()) || isNaN(e.getTime())) return 0;
    const diffTime = Math.abs(e - s);
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
};