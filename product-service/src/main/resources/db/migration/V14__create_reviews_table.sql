-- 상품 리뷰 테이블
CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '상품 ID',
    user_id BIGINT NOT NULL COMMENT '작성자 ID',
    user_name VARCHAR(100) NOT NULL COMMENT '작성자 이름',
    score INT NOT NULL COMMENT '평점 (1~5)',
    content TEXT NOT NULL COMMENT '리뷰 내용',
    image_url VARCHAR(500) NULL COMMENT '리뷰 이미지 URL',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    deleted_at DATETIME NULL COMMENT '삭제 시각',
    created_at DATETIME NOT NULL COMMENT '작성 시각',
    updated_at DATETIME NOT NULL COMMENT '수정 시각',
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 리뷰';
