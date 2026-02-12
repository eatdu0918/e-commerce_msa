-- 카테고리 소프트 삭제 패턴 완성: deleted_at 컬럼 추가
ALTER TABLE categories ADD COLUMN deleted_at DATETIME NULL;
