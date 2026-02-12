-- 장바구니/찜 목록을 Redis로 전환하여 더 이상 사용하지 않는 MySQL 테이블 삭제
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS wishlist_items;
