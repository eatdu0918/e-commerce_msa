-- 출고 전 취소 vs 배송 중/후 반품·환불 구분 (기존 행은 주문 취소로 간주)
ALTER TABLE cancels
    ADD COLUMN request_type VARCHAR(20) NOT NULL DEFAULT 'ORDER_CANCEL'
        COMMENT 'ORDER_CANCEL: 출고 전 취소, RETURN_REFUND: 반품·환불'
        AFTER status;
