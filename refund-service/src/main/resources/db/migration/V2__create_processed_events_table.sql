-- 이벤트 멱등성 처리를 위한 테이블
CREATE TABLE processed_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL UNIQUE COMMENT '이벤트 고유 ID',
    event_type VARCHAR(100) NOT NULL COMMENT '이벤트 타입',
    processed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '처리 시각',
    INDEX idx_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='처리된 이벤트 기록';
