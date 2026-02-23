package com.ecommerce.orderservice.entity;

import com.ecommerce.orderservice.enums.OutboxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    @Test
    @DisplayName("OutboxEvent 생성 성공")
    void create_success() {
        // given
        String aggregateType = "Order";
        String aggregateId = "ORD-20240101-001";
        String eventType = "OrderCreatedEvent";
        String topic = "order-created";
        String payload = "{\"orderId\":1}";

        // when
        OutboxEvent event = OutboxEvent.create(aggregateType, aggregateId, eventType, topic, payload);

        // then
        assertThat(event.getAggregateType()).isEqualTo(aggregateType);
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getEventType()).isEqualTo(eventType);
        assertThat(event.getTopic()).isEqualTo(topic);
        assertThat(event.getPayload()).isEqualTo(payload);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("markAsProcessed - 상태를 PROCESSED로 변경")
    void markAsProcessed_changesStatusToProcessed() {
        // given
        OutboxEvent event = createTestEvent();

        // when
        event.markAsProcessed();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
        assertThat(event.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("markAsFailed - 상태를 FAILED로 변경하고 retryCount 증가")
    void markAsFailed_changesStatusToFailedAndIncrementsRetryCount() {
        // given
        OutboxEvent event = createTestEvent();
        int initialRetryCount = event.getRetryCount();

        // when
        event.markAsFailed();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(initialRetryCount + 1);
    }

    @Test
    @DisplayName("retry - 상태를 PENDING으로 변경")
    void retry_changesStatusToPending() {
        // given
        OutboxEvent event = createTestEvent();
        event.markAsFailed();
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);

        // when
        event.retry();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("canRetry - retryCount가 maxRetryCount 미만이면 true")
    void canRetry_returnsTrueWhenBelowMaxRetry() {
        // given
        OutboxEvent event = createTestEvent();
        int maxRetryCount = 3;

        // when & then
        assertThat(event.canRetry(maxRetryCount)).isTrue();
    }

    @Test
    @DisplayName("canRetry - retryCount가 maxRetryCount 이상이면 false")
    void canRetry_returnsFalseWhenAtOrAboveMaxRetry() {
        // given
        OutboxEvent event = createTestEvent();
        int maxRetryCount = 3;

        // retryCount를 maxRetryCount까지 증가
        event.markAsFailed();
        event.markAsFailed();
        event.markAsFailed();

        // when & then
        assertThat(event.canRetry(maxRetryCount)).isFalse();
    }

    @Test
    @DisplayName("여러 번 실패 후 재시도 가능 여부 확인")
    void canRetry_afterMultipleFailures() {
        // given
        OutboxEvent event = createTestEvent();
        int maxRetryCount = 3;

        // when
        event.markAsFailed(); // retryCount = 1
        assertThat(event.canRetry(maxRetryCount)).isTrue();

        event.markAsFailed(); // retryCount = 2
        assertThat(event.canRetry(maxRetryCount)).isTrue();

        event.markAsFailed(); // retryCount = 3
        assertThat(event.canRetry(maxRetryCount)).isFalse();
    }

    private OutboxEvent createTestEvent() {
        return OutboxEvent.create(
                "Order",
                "ORD-20240101-001",
                "OrderCreatedEvent",
                "order-created",
                "{\"orderId\":1}"
        );
    }
}
