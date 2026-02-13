import { describe, it, expect, vi, afterEach } from 'vitest';
import { formatDate, formatDateTime, formatRelative } from './date';

describe('formatDate', () => {
    it('formats a date string with default format', () => {
        expect(formatDate('2025-03-15')).toBe('2025년 3월 15일');
    });

    it('formats a Date object', () => {
        expect(formatDate(new Date('2025-01-01T00:00:00'))).toBe('2025년 1월 1일');
    });

    it('formats with custom format', () => {
        expect(formatDate('2025-12-25', 'YYYY/MM/DD')).toBe('2025/12/25');
    });

    it('returns empty string for undefined', () => {
        expect(formatDate(undefined)).toBe('');
    });
});

describe('formatDateTime', () => {
    it('formats a datetime string with default format', () => {
        expect(formatDateTime('2025-03-15T14:30:00')).toBe('2025.03.15 14:30');
    });

    it('returns empty string for undefined', () => {
        expect(formatDateTime(undefined)).toBe('');
    });

    it('formats with custom format', () => {
        expect(formatDateTime('2025-06-01T09:00:00', 'MM/DD HH:mm')).toBe('06/01 09:00');
    });
});

describe('formatRelative', () => {
    afterEach(() => {
        vi.useRealTimers();
    });

    it('returns empty string for undefined', () => {
        expect(formatRelative(undefined)).toBe('');
    });

    it('returns "오늘" for today', () => {
        const now = new Date();
        expect(formatRelative(now.toISOString())).toBe('오늘');
    });

    it('returns "어제" for yesterday', () => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2025-03-15T12:00:00'));
        expect(formatRelative('2025-03-14T12:00:00')).toBe('어제');
    });

    it('returns "N일 전" for 2-6 days ago', () => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2025-03-15T12:00:00'));
        expect(formatRelative('2025-03-12T12:00:00')).toBe('3일 전');
    });

    it('returns formatted date for 7+ days ago', () => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2025-03-15T12:00:00'));
        expect(formatRelative('2025-03-01T12:00:00')).toBe('2025.03.01');
    });
});
