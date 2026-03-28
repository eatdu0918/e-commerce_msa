-- 취소 신청 직전 주문 상태 (고객 취소 거부 시 복원)
ALTER TABLE orders
    ADD COLUMN status_before_cancel_request VARCHAR(20) NULL COMMENT '취소 신청 직전 주문 상태 (거부 시 복원용)' AFTER status;
