-- 깨진 이미지 경로 수정 (기존 파일로 대체)

-- Headphones -> product_headphones_... (Broken) -> product1.png (Placeholder)
UPDATE products 
SET image_url = '/assets/product1.png' 
WHERE name IN ('Noise Cancelling Pro', 'Studio Monitor Headphones') OR image_url LIKE '%product_headphones_%';

-- Earbuds -> product_earrings_... (Broken/Wrong) -> product2.png (Placeholder)
UPDATE products 
SET image_url = '/assets/product2.png' 
WHERE name = 'Wireless Earbuds' OR image_url LIKE '%product_earrings_%';

-- Speaker -> product_watch_... (Wrong) -> product3.png (Placeholder)
UPDATE products 
SET image_url = '/assets/product3.png' 
WHERE name = 'Bluetooth Speaker';

-- Ensure all other broken paths are mapped to valid placeholders if they exist
-- We can add more specific updates if needed based on the file list we saw earlier.
-- Valid files seen: 
-- product1.png, product2.png, product3.png, product4.png
-- product_backpack_....png, product_boots_....png, etc.

-- Let's fix the specific items added in V7 that might have broken paths or mismatched images
-- "High Top Sneakers" used white_sneakers (Correct)
-- "Running Shoes" used white_sneakers (Correct)
-- "Crossbody Bag" used leather_bag (Correct)

-- Just fixing the ones we know are definitely broken/missing from V7 insert
