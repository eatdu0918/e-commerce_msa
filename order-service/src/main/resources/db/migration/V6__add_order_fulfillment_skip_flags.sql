-- 결제 완료 후 배송 진행 단계 빠른 건너뛰기(체크아웃 옵션)
ALTER TABLE orders
    ADD COLUMN skip_confirm_and_preparing TINYINT(1) NOT NULL DEFAULT 0
        COMMENT '주문확정·상품준비 단계 생략 시 배송중까지 즉시 진행' AFTER recipient_phone,
    ADD COLUMN skip_shipping_and_delivered TINYINT(1) NOT NULL DEFAULT 0
        COMMENT '배송중·배송완료 단계 생략 시 즉시 배송완료(위 플래그와 함께만 허용)' AFTER skip_confirm_and_preparing;
