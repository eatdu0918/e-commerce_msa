-- 주문당 결제 1건 (중복 생성·이중 API 호출 방지)
ALTER TABLE payments
    ADD CONSTRAINT uk_payments_order_id UNIQUE (order_id);
