import { describe, it, expect } from 'vitest';
import { CANCEL_REASON_LABELS } from './cancel';
import type { CancelReason } from './cancel';

describe('cancel service types', () => {
    it('CANCEL_REASON_LABELS has all 6 reasons', () => {
        const reasons: CancelReason[] = [
            'CHANGE_OF_MIND',
            'WRONG_ORDER',
            'DUPLICATE_ORDER',
            'PRICE_CHANGE',
            'DELIVERY_DELAY',
            'OTHER',
        ];
        expect(Object.keys(CANCEL_REASON_LABELS)).toHaveLength(6);
        reasons.forEach(reason => {
            expect(CANCEL_REASON_LABELS[reason]).toBeDefined();
            expect(typeof CANCEL_REASON_LABELS[reason]).toBe('string');
        });
    });

    it('CANCEL_REASON_LABELS values are Korean strings', () => {
        expect(CANCEL_REASON_LABELS.CHANGE_OF_MIND).toBe('단순 변심');
        expect(CANCEL_REASON_LABELS.WRONG_ORDER).toBe('잘못된 주문');
        expect(CANCEL_REASON_LABELS.OTHER).toBe('기타');
    });
});
