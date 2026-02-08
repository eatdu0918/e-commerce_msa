-- 잘못된 이미지 경로 재수정 (V8에서 /assets/ 경로를 사용했으나 실제 파일은 root에 있음)

-- /assets/product1.png -> /product1.png
UPDATE products 
SET image_url = '/product1.png' 
WHERE image_url = '/assets/product1.png';

-- /assets/product2.png -> /product2.png
UPDATE products 
SET image_url = '/product2.png' 
WHERE image_url = '/assets/product2.png';

-- /assets/product3.png -> /product3.png
UPDATE products 
SET image_url = '/product3.png' 
WHERE image_url = '/assets/product3.png';

-- /assets/product4.png -> /product4.png
-- (If any remain)
UPDATE products 
SET image_url = '/product4.png' 
WHERE image_url = '/assets/product4.png';
