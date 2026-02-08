-- 더미 데이터 추가 (페이징 테스트용)

-- Electronics (Headphones image -> High-end Audio)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Noise Cancelling Pro', 'Premium noise cancelling headphones with 30h battery.', 249.99, 100, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_headphones_1770130000000.png', true, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('Studio Monitor Headphones', 'Professional grade studio reference headphones.', 199.99, 50, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_headphones_1770130000000.png', true, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('Wireless Earbuds', 'Compact wireless earbuds with charging case.', 129.99, 200, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_earrings_1770130143031.png', true, DATE_SUB(NOW(), INTERVAL 3 DAY), NOW()),
('Bluetooth Speaker', 'Portable bluetooth speaker with deep bass.', 79.99, 80, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_watch_1770130025997.png', true, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW());

-- Clothing (Coat image -> Various Coats)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Trench Coat', 'Classic beige trench coat.', 149.99, 30, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_coat_1770130127479.png', true, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
('Winter Parka', 'Heavy duty winter parka with faux fur hood.', 299.99, 20, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_coat_1770130127479.png', true, DATE_SUB(NOW(), INTERVAL 12 DAY), NOW()),
('Denim Jacket', 'Vintage wash denim jacket.', 89.99, 60, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_leather_jacket_1770129969469.png', true, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
('Bomber Jacket', 'Military style bomber jacket in green.', 119.99, 40, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_leather_jacket_1770129969469.png', true, DATE_SUB(NOW(), INTERVAL 16 DAY), NOW());

-- Clothing (Dress image -> Various Dresses)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Summer Floral Dress', 'Lightweight floral print dress.', 59.99, 80, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_dress_1770130098881.png', true, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW()),
('Evening Gown', 'Elegant black evening gown.', 199.99, 15, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_dress_1770130098881.png', true, DATE_SUB(NOW(), INTERVAL 22 DAY), NOW()),
('Midi Skirt', 'Pleated midi skirt in pastel color.', 49.99, 50, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_dress_1770130098881.png', true, DATE_SUB(NOW(), INTERVAL 25 DAY), NOW());

-- Accessories (Bag image -> Various Bags)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Crossbody Bag', 'Minimalist leather crossbody bag.', 89.99, 45, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_leather_bag_1770129999630.png', true, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('Tote Bag', 'Large capacity tote bag for work.', 129.99, 35, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_leather_bag_1770129999630.png', true, DATE_SUB(NOW(), INTERVAL 6 DAY), NOW()),
('Clutch', 'Evening clutch with chain strap.', 69.99, 60, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_leather_bag_1770129999630.png', true, DATE_SUB(NOW(), INTERVAL 8 DAY), NOW());

-- Accessories (Hat image -> Various Hats)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Fedora Hat', 'Classic fedora hat in wool felt.', 49.99, 25, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_hat_1770130071329.png', true, DATE_SUB(NOW(), INTERVAL 11 DAY), NOW()),
('Beanie', 'Warm knit beanie for winter.', 19.99, 100, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_hat_1770130071329.png', true, DATE_SUB(NOW(), INTERVAL 30 DAY), NOW()),
('Cap', 'Casual baseball cap.', 24.99, 80, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_hat_1770130071329.png', true, DATE_SUB(NOW(), INTERVAL 35 DAY), NOW());

-- Accessories (Watch image -> Watches)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Digital Sport Watch', 'Durable digital watch with stopwatch.', 39.99, 90, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_watch_1770130025997.png', true, DATE_SUB(NOW(), INTERVAL 40 DAY), NOW()),
('Luxury Gold Watch', 'Premium gold plated watch.', 299.99, 10, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_watch_1770130025997.png', true, DATE_SUB(NOW(), INTERVAL 42 DAY), NOW());

-- Clothing (Sneakers image -> Shoes)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Running Shoes', 'Lightweight running shoes.', 109.99, 50, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_white_sneakers_1770129984418.png', true, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW()),
('High Top Sneakers', 'Stylish high top sneakers.', 99.99, 40, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_white_sneakers_1770129984418.png', true, DATE_SUB(NOW(), INTERVAL 4 DAY), NOW());

-- Accessories (Sunglasses)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Wayfarer Sunglasses', 'Iconic wayfarer style sunglasses.', 149.99, 60, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_sunglasses_1770130041859.png', true, DATE_SUB(NOW(), INTERVAL 1 DAY), NOW()),
('Round Sunglasses', 'Retro round metal sunglasses.', 129.99, 55, (SELECT category_id FROM categories WHERE name = 'Accessories'), '/product_sunglasses_1770130041859.png', true, DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- Clothing (Boots)
INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Chelsea Boots', 'Classic leather chelsea boots.', 159.99, 30, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_boots_1770130114393.png', true, DATE_SUB(NOW(), INTERVAL 14 DAY), NOW()),
('Ankle Boots', 'Suede ankle boots with heel.', 139.99, 25, (SELECT category_id FROM categories WHERE name = 'Clothing'), '/product_boots_1770130114393.png', true, DATE_SUB(NOW(), INTERVAL 18 DAY), NOW());

