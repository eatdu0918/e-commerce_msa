-- 카테고리 불일치 수정 (이미지에 맞춰 상품 정보 변경)

-- 1. Smart Watch (Electronics -> Accessories)
-- Assuming 'Accessories' ID is 3 (Based on V5)
UPDATE products 
SET category_id = (SELECT category_id FROM categories WHERE name = 'Accessories')
WHERE name = 'Smart Watch';

-- 2. Noise Cancelling Pro (Image: product1.png -> Leather Jacket) -> Clothing
UPDATE products 
SET name = 'Leather Biker Jacket', 
    description = 'Classic leather biker jacket with zipper details.', 
    category_id = (SELECT category_id FROM categories WHERE name = 'Clothing')
WHERE name = 'Noise Cancelling Pro';

-- 3. Studio Monitor Headphones (Image: product1.png -> Leather Jacket) -> Clothing
UPDATE products 
SET name = 'Vintage Leather Jacket', 
    description = 'Vintage style leather jacket.', 
    category_id = (SELECT category_id FROM categories WHERE name = 'Clothing')
WHERE name = 'Studio Monitor Headphones';

-- 4. Wireless Earbuds (Image: product2.png -> Sneakers) -> Clothing
UPDATE products 
SET name = 'Minimalist White Sneakers', 
    description = 'Clean and simple white sneakers for everyday wear.', 
    category_id = (SELECT category_id FROM categories WHERE name = 'Clothing')
WHERE name = 'Wireless Earbuds';

-- 5. Bluetooth Speaker (Image: product3.png -> Bag) -> Accessories
UPDATE products 
SET name = 'Classic Leather Bag', 
    description = 'Timeless leather bag design.', 
    category_id = (SELECT category_id FROM categories WHERE name = 'Accessories')
WHERE name = 'Bluetooth Speaker';
