CREATE TABLE payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_number VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    payment_number VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    paid_at DATETIME,
    failed_at DATETIME,
    failure_reason VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_payment_number (payment_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
