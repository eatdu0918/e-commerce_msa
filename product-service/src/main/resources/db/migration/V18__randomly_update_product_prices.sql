-- 모든 상품의 가격을 1000, 2000, 3000원 중 하나로 랜덤하게 수정
UPDATE products 
SET price = CASE FLOOR(RAND() * 3)
    WHEN 0 THEN 1000
    WHEN 1 THEN 2000
    WHEN 2 THEN 3000
END;
