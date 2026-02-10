-- 전자제품 신규 상품 추가
-- Categories: Electronics (ID는 V5에서 생성된 것을 참조)

INSERT INTO products (name, description, price, stock_quantity, category_id, image_url, is_active, created_at, updated_at) VALUES
('Premium Wireless Headphones', 'Professional grade noise-cancelling wireless headphones with 40h battery life and superior comfort.', 349.99, 50, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_headphones_premium.png', true, NOW(), NOW()),
('Acoustic Smart Speaker', 'High-fidelity smart speaker with voice assistant integration and 360-degree immersive sound.', 179.99, 120, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_smart_speaker.png', true, NOW(), NOW()),
('Precision RGB Gaming Mouse', 'Ultra-lightweight gaming mouse with 26K DPI sensor and customizable RGB lighting.', 79.99, 200, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_gaming_mouse_rgb.png', true, NOW(), NOW()),
('Tactile Mechanical Keyboard', 'Premium mechanical keyboard with hot-swappable switches and robust aluminum frame.', 149.99, 85, (SELECT category_id FROM categories WHERE name = 'Electronics'), '/product_kb_mechanical.png', true, NOW(), NOW());
