CREATE TABLE cancels (
    cancel_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_number VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    cancel_number VARCHAR(32) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    cancel_reason VARCHAR(20) NOT NULL,
    cancel_detail VARCHAR(500),
    rejected_reason VARCHAR(500),
    approved_at DATETIME,
    rejected_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_cancel_number (cancel_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cancel_items (
    cancel_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cancel_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (cancel_id) REFERENCES cancels(cancel_id),
    INDEX idx_cancel_id (cancel_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
