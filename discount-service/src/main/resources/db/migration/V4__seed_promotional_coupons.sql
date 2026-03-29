-- 프로모션 쿠폰: 10% / 20% 정률, 500원 정액 (수량 제한 없음, 코드 기준 멱등 삽입)

INSERT INTO coupons (
    code,
    name,
    description,
    coupon_type,
    discount_value,
    min_order_amount,
    max_discount_amount,
    total_quantity,
    issued_quantity,
    valid_from,
    valid_until,
    is_active,
    created_at,
    updated_at
)
SELECT
    'WELCOME10',
    '10% 할인 쿠폰',
    '주문 금액 대비 10% 정률 할인',
    'PERCENTAGE',
    10.00,
    NULL,
    NULL,
    NULL,
    0,
    '2026-01-01 00:00:00',
    '2028-12-31 23:59:59',
    TRUE,
    NOW(),
    NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM coupons c WHERE c.code = 'WELCOME10');

INSERT INTO coupons (
    code,
    name,
    description,
    coupon_type,
    discount_value,
    min_order_amount,
    max_discount_amount,
    total_quantity,
    issued_quantity,
    valid_from,
    valid_until,
    is_active,
    created_at,
    updated_at
)
SELECT
    'WELCOME20',
    '20% 할인 쿠폰',
    '주문 금액 대비 20% 정률 할인',
    'PERCENTAGE',
    20.00,
    NULL,
    NULL,
    NULL,
    0,
    '2026-01-01 00:00:00',
    '2028-12-31 23:59:59',
    TRUE,
    NOW(),
    NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM coupons c WHERE c.code = 'WELCOME20');

INSERT INTO coupons (
    code,
    name,
    description,
    coupon_type,
    discount_value,
    min_order_amount,
    max_discount_amount,
    total_quantity,
    issued_quantity,
    valid_from,
    valid_until,
    is_active,
    created_at,
    updated_at
)
SELECT
    'FIXED500',
    '500원 할인 쿠폰',
    '주문 금액에서 500원 정액 할인',
    'FIXED_AMOUNT',
    500.00,
    NULL,
    NULL,
    NULL,
    0,
    '2026-01-01 00:00:00',
    '2028-12-31 23:59:59',
    TRUE,
    NOW(),
    NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM coupons c WHERE c.code = 'FIXED500');
