-- 카테고리 데이터 삽입
INSERT INTO categories (name, description, display_order, is_active, created_at, updated_at) VALUES
('Electronics', 'Electronic devices and accessories', 1, true, NOW(), NOW()),
('Clothing', 'Apparel for men and women', 2, true, NOW(), NOW()),
('Accessories', 'Jewelry, bags, and more', 3, true, NOW(), NOW());

-- 상품 데이터 삽입 (이미지 경로 포함)
-- Electronics
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Smart Watch', 'Latest model smart watch with health tracking features.', 299.99, 50, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_watch_1770130025997.png', true, NOW(), NOW());

-- Clothing
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Classic Coat', 'Warm and stylish wool coat for winter.', 129.99, 30, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_coat_1770130127479.png', true, NOW(), NOW()),
('Casual Dress', 'Comfortable cotton dress for everyday wear.', 49.99, 100, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_dress_1770130098881.png', true, NOW(), NOW()),
('White Sneakers', 'Clean and simple white sneakers.', 89.99, 60, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_white_sneakers_1770129984418.png', true, NOW(), NOW());

-- Accessories
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Leather Bag', 'Genuine leather bag with multiple compartments.', 149.99, 20, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_leather_bag_1770129999630.png', true, NOW(), NOW()),
('Stylish Hat', 'Wide-brim hat perfect for sunny days.', 29.99, 40, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_hat_1770130071329.png', true, NOW(), NOW()),
('Gold Earrings', 'Elegant gold-plated hoop earrings.', 39.99, 80, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_earrings_1770130143031.png', true, NOW(), NOW()),
('Silk Scarf', 'Soft and luxurious silk scarf with floral pattern.', 24.99, 50, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_scarf_1770130055870.png', true, NOW(), NOW()),
('Aviator Sunglasses', 'Classic aviator sunglasses with UV protection.', 59.99, 70, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_sunglasses_1770130041859.png', true, NOW(), NOW()),
('Leather Jacket', 'Black leather jacket with zipper details.', 199.99, 15, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_leather_jacket_1770129969469.png', true, NOW(), NOW()),
('Travel Backpack', 'Durable backpack suitable for hiking and travel.', 79.99, 45, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_backpack_1770130171952.png', true, NOW(), NOW()),
('Winter Boots', 'Insulated boots to keep feet warm in snow.', 109.99, 25, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_boots_1770130114393.png', true, NOW(), NOW());

-- Additional items repurposing images for variety if needed, or stick to unique images.
-- Using product1-4 placeholders if they exist in valid logical categories.
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Modern Headphones', 'Noise-cancelling over-ear headphones.', 199.99, 35, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product1.png', true, NOW(), NOW()),
('Gaming Mouse', 'High-precision gaming mouse with RGB lighting.', 49.99, 60, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product2.png', true, NOW(), NOW()),
('Mechanical Keyboard', 'Clicky mechanical keyboard for typing enthusiasts.', 89.99, 40, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product3.png', true, NOW(), NOW()),
('4K Monitor', '27-inch 4K UHD monitor for professional work.', 399.99, 10, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product4.png', true, NOW(), NOW());
