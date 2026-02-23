-- Transactional Outbox 테이블
CREATE TABLE outbox_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이벤트 ID',
    aggregate_type VARCHAR(100) NOT NULL COMMENT '집계 타입 (Order, Cancel 등)',
    aggregate_id VARCHAR(100) NOT NULL COMMENT '집계 ID (orderId, orderNumber 등)',
    event_type VARCHAR(100) NOT NULL COMMENT '이벤트 타입 (OrderCreatedEvent 등)',
    topic VARCHAR(100) NOT NULL COMMENT 'Kafka 토픽명',
    payload TEXT NOT NULL COMMENT '이벤트 페이로드 (JSON)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '상태 (PENDING, PROCESSED, FAILED)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '재시도 횟수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    processed_at DATETIME NULL COMMENT '처리 완료 시간',
    INDEX idx_outbox_status_created (status, created_at),
    INDEX idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Transactional Outbox 이벤트 테이블';
