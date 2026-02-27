import { calculateDays } from '../constants/leaveConstants';

describe('Leave Calculation Utilities', () => {
    test('calculates correct days for single day leave', () => {
        expect(calculateDays('2026-01-01', '2026-01-01')).toBe(1);
    });

    test('calculates correct days for multi-day leave', () => {
        expect(calculateDays('2026-01-01', '2026-01-03')).toBe(3);
    });

    test('handles cross-month leave correctly', () => {
        // Jan 30 to Feb 1 (Jan has 31 days)
        // 30, 31, 1 = 3 days
        expect(calculateDays('2026-01-30', '2026-02-01')).toBe(3);
    });

    test('returns 0 for missing dates', () => {
        expect(calculateDays('', '2026-01-01')).toBe(0);
        expect(calculateDays(null, '2026-01-01')).toBe(0);
    });
});
