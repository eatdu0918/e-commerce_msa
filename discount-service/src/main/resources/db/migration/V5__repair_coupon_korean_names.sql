-- Flyway/클라이언트 문자셋 불일치 등으로 name/description 한글이 깨진 행 보정 (코드 기준 멱등)

UPDATE coupons
SET
    name = '10% 할인 쿠폰',
    description = '주문 금액 대비 10% 정률 할인'
WHERE code = 'WELCOME10';

UPDATE coupons
SET
    name = '20% 할인 쿠폰',
    description = '주문 금액 대비 20% 정률 할인'
WHERE code = 'WELCOME20';

UPDATE coupons
SET
    name = '500원 할인 쿠폰',
    description = '주문 금액에서 500원 정액 할인'
WHERE code = 'FIXED500';
