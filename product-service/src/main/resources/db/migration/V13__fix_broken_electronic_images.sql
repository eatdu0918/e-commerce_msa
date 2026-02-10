-- 전자제품 신규 상품 이미지 경로 수정 (임시 Unsplash 이미지 적용)
UPDATE products SET image_url = 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&q=80&w=800' 
WHERE name = 'Premium Wireless Headphones';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1589492477829-5e65395b66cc?auto=format&fit=crop&q=80&w=800' 
WHERE name = 'Acoustic Smart Speaker';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&q=80&w=800' 
WHERE name = 'Precision RGB Gaming Mouse';

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?auto=format&fit=crop&q=80&w=800' 
WHERE name = 'Tactile Mechanical Keyboard';
