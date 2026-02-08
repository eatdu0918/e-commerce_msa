-- 잘못된 이미지 매핑을 가진 데이터 삭제
DELETE FROM products WHERE name IN ('Modern Headphones', 'Gaming Mouse', 'Mechanical Keyboard', '4K Monitor');

-- 검증된 이미지를 사용하는 새로운 더미 데이터 추가
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Classic Analog Watch', 'Timeless design with leather strap.', 159.99, 30, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_watch_1770130025997.png', true, NOW(), NOW()),
('Wool Blend Coat', 'Elegant coat suitable for formal occasions.', 189.99, 15, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_coat_1770130127479.png', true, NOW(), NOW()),
('Leather Satchel', 'Vintage style leather satchel for daily use.', 119.99, 25, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_leather_bag_1770129999630.png', true, NOW(), NOW()),
('Canvas Sneakers', 'Lightweight and comfortable canvas sneakers.', 59.99, 100, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_white_sneakers_1770129984418.png', true, NOW(), NOW());
