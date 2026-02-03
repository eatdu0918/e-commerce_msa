-- 카테고리 테이블 생성
CREATE TABLE categories (
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    parent_id BIGINT,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (category_id),
    FOREIGN KEY (parent_id) REFERENCES categories(category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- products 테이블에 category_id 컬럼 추가
ALTER TABLE products ADD COLUMN category_id BIGINT;
ALTER TABLE products ADD INDEX idx_category_id (category_id);
