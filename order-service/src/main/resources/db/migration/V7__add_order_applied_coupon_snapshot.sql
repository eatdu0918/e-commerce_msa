-- 주문에 적용된 쿠폰 스냅샷(표시용). coupon-used 시점에 저장
ALTER TABLE orders
    ADD COLUMN applied_coupon_name VARCHAR(100) NULL COMMENT '적용 쿠폰명 스냅샷' AFTER user_coupon_id,
    ADD COLUMN applied_coupon_code VARCHAR(50) NULL COMMENT '적용 쿠폰 코드 스냅샷' AFTER applied_coupon_name,
    ADD COLUMN applied_coupon_type VARCHAR(20) NULL COMMENT 'PERCENTAGE | FIXED_AMOUNT' AFTER applied_coupon_code,
    ADD COLUMN applied_coupon_rule_value DECIMAL(12, 2) NULL COMMENT '쿠폰 정의 할인율/정액 값' AFTER applied_coupon_type;
