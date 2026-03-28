-- 상품·카테고리 다국어(한글) 표시용 컬럼
ALTER TABLE categories
    ADD COLUMN name_ko VARCHAR(100) NULL COMMENT '카테고리명(한국어)' AFTER name,
    ADD COLUMN description_ko VARCHAR(500) NULL COMMENT '설명(한국어)' AFTER description;

ALTER TABLE products
    ADD COLUMN name_ko VARCHAR(255) NULL COMMENT '상품명(한국어)' AFTER name,
    ADD COLUMN description_ko TEXT NULL COMMENT '상품 설명(한국어)' AFTER description;

UPDATE categories SET name_ko = '전자기기', description_ko = '전자제품과 액세서리' WHERE name = 'Electronics';
UPDATE categories SET name_ko = '의류', description_ko = '남녀 의류' WHERE name = 'Clothing';
UPDATE categories SET name_ko = '액세서리', description_ko = '주얼리, 가방 등' WHERE name = 'Accessories';

UPDATE products SET name_ko = '스마트워치', description_ko = '건강 추적 기능을 갖춘 최신 스마트워치.' WHERE name = 'Smart Watch';
UPDATE products SET name_ko = '클래식 코트', description_ko = '겨울철 따뜻하고 세련된 울 코트.' WHERE name = 'Classic Coat';
UPDATE products SET name_ko = '캐주얼 원피스', description_ko = '일상에 편한 면 원피스.' WHERE name = 'Casual Dress';
UPDATE products SET name_ko = '화이트 스니커즈', description_ko = '깔끔한 화이트 스니커즈.' WHERE name = 'White Sneakers';
UPDATE products SET name_ko = '가죽 가방', description_ko = '다수의 수납공간이 있는 천연 가죽 가방.' WHERE name = 'Leather Bag';
UPDATE products SET name_ko = '스타일리시 모자', description_ko = '햇살이 강한 날에 어울리는 와이드 브림 모자.' WHERE name = 'Stylish Hat';
UPDATE products SET name_ko = '골드 귀걸이', description_ko = '우아한 골드 도금 후프 귀걸이.' WHERE name = 'Gold Earrings';
UPDATE products SET name_ko = '실크 스카프', description_ko = '부드럽고 고급스러운 꽃무늬 실크 스카프.' WHERE name = 'Silk Scarf';
UPDATE products SET name_ko = '에비에이터 선글라스', description_ko = '자외선 차단 클래식 에비에이터 스타일.' WHERE name = 'Aviator Sunglasses';
UPDATE products SET name_ko = '가죽 자켓', description_ko = '지퍼 디테일의 블랙 가죽 자켓.' WHERE name = 'Leather Jacket';
UPDATE products SET name_ko = '여행용 백팩', description_ko = '등산과 여행에 적합한 견고한 백팩.' WHERE name = 'Travel Backpack';
UPDATE products SET name_ko = '겨울 부츠', description_ko = '눈 속에서도 발을 따뜻하게 지켜주는 방한 부츠.' WHERE name = 'Winter Boots';
UPDATE products SET name_ko = '모던 헤드폰', description_ko = '노이즈 캔슬링 오버이어 헤드폰.' WHERE name = 'Modern Headphones';
UPDATE products SET name_ko = '게이밍 마우스', description_ko = 'RGB 조명의 고정밀 게이밍 마우스.' WHERE name = 'Gaming Mouse';
UPDATE products SET name_ko = '기계식 키보드', description_ko = '타건감이 살아 있는 기계식 키보드.' WHERE name = 'Mechanical Keyboard';
UPDATE products SET name_ko = '4K 모니터', description_ko = '전문 작업용 27형 4K UHD 모니터.' WHERE name = '4K Monitor';
UPDATE products SET name_ko = '클래식 아날로그 시계', description_ko = '가죽 스트랩의 타임리스 디자인.' WHERE name = 'Classic Analog Watch';
UPDATE products SET name_ko = '울 블렌드 코트', description_ko = '포멀한 자리에 어울리는 코트.' WHERE name = 'Wool Blend Coat';
UPDATE products SET name_ko = '가죽 새들백', description_ko = '데일리에 어울리는 빈티지 가죽 메신저백.' WHERE name = 'Leather Satchel';
UPDATE products SET name_ko = '캔버스 스니커즈', description_ko = '가볍고 편안한 캔버스 스니커즈.' WHERE name = 'Canvas Sneakers';
UPDATE products SET name_ko = '노이즈 캔슬링 프로', description_ko = '30시간 배터리의 프리미엄 노이즈 캔슬링 헤드폰.' WHERE name = 'Noise Cancelling Pro';
UPDATE products SET name_ko = '스튜디오 모니터 헤드폰', description_ko = '프로페셔널 스튜디오 레퍼런스 헤드폰.' WHERE name = 'Studio Monitor Headphones';
UPDATE products SET name_ko = '무선 이어버드', description_ko = '충전 케이스가 있는 컴팩트 무선 이어버드.' WHERE name = 'Wireless Earbuds';
UPDATE products SET name_ko = '블루투스 스피커', description_ko = '풍부한 베이스의 휴대용 블루투스 스피커.' WHERE name = 'Bluetooth Speaker';
UPDATE products SET name_ko = '트렌치 코트', description_ko = '클래식 베이지 트렌치 코트.' WHERE name = 'Trench Coat';
UPDATE products SET name_ko = '윈터 파카', description_ko = '퍼 후드가 달린 헤비듀티 겨울 파카.' WHERE name = 'Winter Parka';
UPDATE products SET name_ko = '데님 자켓', description_ko = '빈티지 워싱 데님 자켓.' WHERE name = 'Denim Jacket';
UPDATE products SET name_ko = '봄버 자켓', description_ko = '그린 컬러의 밀리터리 스타일 봄버 자켓.' WHERE name = 'Bomber Jacket';
UPDATE products SET name_ko = '썸머 플로럴 원피스', description_ko = '가벼운 꽃무늬 원피스.' WHERE name = 'Summer Floral Dress';
UPDATE products SET name_ko = '이브닝 드레스', description_ko = '우아한 블랙 이브닝 드레스.' WHERE name = 'Evening Gown';
UPDATE products SET name_ko = '미디 스커트', description_ko = '파스텔 컬러 플리츠 미디 스커트.' WHERE name = 'Midi Skirt';
UPDATE products SET name_ko = '크로스바디 백', description_ko = '미니멀 가죽 크로스바디 백.' WHERE name = 'Crossbody Bag';
UPDATE products SET name_ko = '토트백', description_ko = '출근용 대용량 토트백.' WHERE name = 'Tote Bag';
UPDATE products SET name_ko = '클러치 백', description_ko = '체인 스트랩 이브닝 클러치.' WHERE name = 'Clutch';
UPDATE products SET name_ko = '페도라 햇', description_ko = '울 펠트 클래식 페도라.' WHERE name = 'Fedora Hat';
UPDATE products SET name_ko = '비니', description_ko = '겨울용 따뜻한 니트 비니.' WHERE name = 'Beanie';
UPDATE products SET name_ko = '캡', description_ko = '캐주얼 야구 모자.' WHERE name = 'Cap';
UPDATE products SET name_ko = '디지털 스포츠 시계', description_ko = '스톱워치 기능의 견고한 디지털 시계.' WHERE name = 'Digital Sport Watch';
UPDATE products SET name_ko = '럭셔리 골드 시계', description_ko = '프리미엄 골드 플레이팅 시계.' WHERE name = 'Luxury Gold Watch';
UPDATE products SET name_ko = '런닝화', description_ko = '가벼운 러닝화.' WHERE name = 'Running Shoes';
UPDATE products SET name_ko = '하이탑 스니커즈', description_ko = '스타일리시한 하이탑 스니커즈.' WHERE name = 'High Top Sneakers';
UPDATE products SET name_ko = '웨이페어러 선글라스', description_ko = '아이코닉 웨이페어러 스타일 선글라스.' WHERE name = 'Wayfarer Sunglasses';
UPDATE products SET name_ko = '라운드 선글라스', description_ko = '레트로 라운드 메탈 선글라스.' WHERE name = 'Round Sunglasses';
UPDATE products SET name_ko = '첼시 부츠', description_ko = '클래식 가죽 첼시 부츠.' WHERE name = 'Chelsea Boots';
UPDATE products SET name_ko = '앵클 부츠', description_ko = '굽이 있는 스웨이드 앵클 부츠.' WHERE name = 'Ankle Boots';
UPDATE products SET name_ko = '프리미엄 무선 헤드폰', description_ko = '40시간 배터리와 편안한 착용감의 프로 노이즈 캔슬링 무선 헤드폰.' WHERE name = 'Premium Wireless Headphones';
UPDATE products SET name_ko = '어쿠스틱 스마트 스피커', description_ko = '음성 비서와 360도 몰입 사운드의 고음질 스마트 스피커.' WHERE name = 'Acoustic Smart Speaker';
UPDATE products SET name_ko = '프리시전 RGB 게이밍 마우스', description_ko = '26K DPI 센서와 맞춤형 RGB의 초경량 게이밍 마우스.' WHERE name = 'Precision RGB Gaming Mouse';
UPDATE products SET name_ko = '택타일 기계식 키보드', description_ko = '핫스왑 스위치와 알루미늄 프레임의 프리미엄 기계식 키보드.' WHERE name = 'Tactile Mechanical Keyboard';
