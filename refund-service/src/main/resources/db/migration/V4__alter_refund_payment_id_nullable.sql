-- payment_id를 nullable로 변경합니다.
-- refund-service가 cancel-approved 이벤트를 수신할 때 paymentId를 포함하지 않는
-- 구버전 이벤트나 결제 서비스 조회 실패 케이스를 허용하기 위함입니다.
ALTER TABLE refunds
    MODIFY COLUMN payment_id BIGINT NULL;
