-- 상품명·설명을 실제 이미지 파일(경로·V10 주석)에 맞게 정리
-- Gold Earrings: V8의 product_earrings% LIKE 조건으로 스니커즈 플레이스홀더로 덮어씌워진 경우 복구
UPDATE products
SET image_url = '/product_earrings_1770130143031.png'
WHERE name = 'Gold Earrings';

-- product_watch: 클래식 손목시계 이미지(스마트워치 UI 아님)
UPDATE products
SET name = 'Classic Leather Watch',
    description = 'Round dial wristwatch with genuine leather strap and polished metal case.',
    name_ko = '클래식 가죽 밴드 시계',
    description_ko = '가죽 스트랩과 메탈 케이스의 라운드 다이얼 손목시계.'
WHERE name = 'Smart Watch';

-- product_leather_jacket: 데님/봄버 명칭은 이미지와 불일치
UPDATE products
SET name = 'Vintage Brown Leather Jacket',
    description = 'Deep brown leather jacket with classic collar and asymmetric zip.',
    name_ko = '빈티지 브라운 가죽 재킷',
    description_ko = '클래식 칼라와 지퍼 디테일의 딥 브라운 가죽 재킷.'
WHERE name = 'Denim Jacket';

UPDATE products
SET name = 'Black Zip Leather Jacket',
    description = 'Sleek black leather jacket with front zipper and tailored silhouette.',
    name_ko = '블랙 지퍼 가죽 재킷',
    description_ko = '전면 지퍼와 슬림 실루엣의 블랙 가죽 재킷.'
WHERE name = 'Bomber Jacket';

-- product_dress: 원피스 이미지를 스커트/이브닝으로 표기한 항목 수정
UPDATE products
SET name = 'Floral Print Maxi Dress',
    description = 'Lightweight maxi dress with floral pattern and flowy skirt.',
    name_ko = '플로럴 프린트 맥시 원피스',
    description_ko = '꽃무늬와 흐르는 스커트 실루엣의 가벼운 맥시 원피스.'
WHERE name = 'Evening Gown';

UPDATE products
SET name = 'Fit-and-Flare Day Dress',
    description = 'Sleeveless day dress with fitted bodice and flared skirt.',
    name_ko = '핏앤플레어 데이 원피스',
    description_ko = '상의 핏이 잡힌 퍼프 실루엣의 민소매 데이 원피스.'
WHERE name = 'Midi Skirt';

-- product_hat: V5 기준 와이드 브림 햇 이미지에 맞춤 (페도라/비니/캡 명칭 정정)
UPDATE products
SET name = 'Wide-Brim Ribbon Hat',
    description = 'Elegant wide-brim hat with satin ribbon, ideal for sun protection.',
    name_ko = '와이드 브림 리본 햇',
    description_ko = '새틴 리본이 돋보이는 와이드 브림 햇, 자외선 차단에 적합.'
WHERE name = 'Fedora Hat';

UPDATE products
SET name = 'Contrast Band Sun Hat',
    description = 'Wide-brim summer hat with contrasting band detail.',
    name_ko = '컨트라스트 밴드 선햇',
    description_ko = '밴드 포인트가 있는 와이드 브림 썸머 햇.'
WHERE name = 'Beanie';

UPDATE products
SET name = 'Resort Wide-Brim Hat',
    description = 'Straw-style wide brim hat for beach and outdoor wear.',
    name_ko = '리조트 와이드 브림 햇',
    description_ko = '비치와 야외 활동에 어울리는 와이드 브림 햇.'
WHERE name = 'Cap';

-- product_coat: 동일 코트 이미지 — 트렌치/파카 과장 표현 완화
UPDATE products
SET name = 'Belted Long Wool Coat',
    description = 'Long wool-blend coat with matching belt and tailored lapels.',
    name_ko = '벨티드 롱 울 코트',
    description_ko = '매칭 벨트와 라펠 디테일의 롱 울 블렌드 코트.'
WHERE name = 'Trench Coat';

UPDATE products
SET name = 'Longline Tailored Coat',
    description = 'Full-length wool coat with structured shoulders and warm lining.',
    name_ko = '롱라인 테일러드 코트',
    description_ko = '어깨 라인이 잡힌 풀렝스 울 코트, 보온 안감.'
WHERE name = 'Winter Parka';

-- product_sunglasses: 동일 샘플 이미지에 맞춘 중립 서술
UPDATE products
SET name = 'Metal Aviator Sunglasses',
    description = 'Lightweight metal-frame sunglasses with UV400 lenses.',
    name_ko = '메탈 에비에이터 선글라스',
    description_ko = 'UV400 렌즈의 경량 메탈 프레임 선글라스.'
WHERE name = 'Wayfarer Sunglasses';

UPDATE products
SET name = 'Classic UV Sunglasses',
    description = 'Everyday sunglasses with durable frame and sun glare reduction.',
    name_ko = '클래식 UV 선글라스',
    description_ko = '내구성 있는 프레임과 눈부심 완화에 도움을 주는 선글라스.'
WHERE name = 'Round Sunglasses';

-- product_boots: 동일 부츠 이미지
UPDATE products
SET name = 'Fur-Cuff Winter Boots',
    description = 'Insulated winter boots with soft fur cuff and sturdy outsole.',
    name_ko = '퍼 커프 윈터 부츠',
    description_ko = '퍼 커프와 견고한 아웃솔의 보온 윈터 부츠.'
WHERE name = 'Chelsea Boots';

UPDATE products
SET name = 'Lace-Up Snow Boots',
    description = 'Warm snow boots with lace-up closure and deep tread for traction.',
    name_ko = '레이스업 스노우 부츠',
    description_ko = '깊은 트레드와 레이스업 여밈의 따뜻한 스노우 부츠.'
WHERE name = 'Ankle Boots';

-- 동일 시계 이미지: 디지털/럭셔리 골드 등 과장된 표현을 화면과 일치하게 (Classic Analog Watch 상품명과 중복 방지)
UPDATE products
SET name = 'Minimal Dress Watch',
    description = 'Simple analog dial with slim leather strap for daily wear.',
    name_ko = '미니멀 드레스 워치',
    description_ko = '슬림 가죽 스트랩의 심플 아날로그 데일리 시계.'
WHERE name = 'Digital Sport Watch';

UPDATE products
SET name = 'Gold-Tone Dress Watch',
    description = 'Elegant dress watch with warm gold-tone case and leather band.',
    name_ko = '골드 톤 드레스 워치',
    description_ko = '골드 톤 케이스와 가죽 밴드의 데일리 드레스 워치.'
WHERE name = 'Luxury Gold Watch';

-- product1.png (V10: 가죽 재킷) — 명칭이 이미 재킷이면 설명만 보강
UPDATE products
SET description = 'Asymmetric leather biker jacket with zip pockets and slim fit.',
    description_ko = '지퍼 포켓과 슬림 핏의 비대칭 레더 바이커 재킷.'
WHERE name = 'Leather Biker Jacket';

UPDATE products
SET description = 'Aged-finish leather jacket with minimal hardware for a vintage look.',
    description_ko = '빈티지 느낌의 에이징 가죽과 미니멀 하드웨어.'
WHERE name = 'Vintage Leather Jacket';

-- product2.png: 화이트 스니커즈
UPDATE products
SET description = 'Crisp low-profile sneakers in white with clean stitching.',
    description_ko = '깔끔한 스티치의 화이트 로우 프로파일 스니커즈.'
WHERE name = 'Minimalist White Sneakers';

-- product3.png: 가죽 백
UPDATE products
SET description = 'Structured leather handbag with top handle and timeless silhouette.',
    description_ko = '탑 핸들과 클래식 실루엣의 스트럭처드 레더 백.'
WHERE name = 'Classic Leather Bag';

-- Canvas Sneakers: 캔버스라는 표현이 화이트 레더 스니커 이미지와 어긋날 수 있어 정리
UPDATE products
SET name = 'Low-Top White Sneakers',
    description = 'Minimal white sneakers with smooth upper and flexible sole.',
    name_ko = '로우탑 화이트 스니커즈',
    description_ko = '부드러운 어퍼와 유연한 아웃솔의 미니멀 화이트 스니커즈.'
WHERE name = 'Canvas Sneakers';
