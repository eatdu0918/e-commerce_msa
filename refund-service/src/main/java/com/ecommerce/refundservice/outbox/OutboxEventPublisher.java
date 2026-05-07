package com.ecommerce.refundservice.outbox;

import com.ecommerce.refundservice.config.KafkaConfig;
import com.ecommerce.refundservice.entity.OutboxEvent;
import com.ecommerce.refundservice.event.RefundCompletedEvent;
import com.ecommerce.refundservice.event.RefundFailedEvent;
import com.ecommerce.refundservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publishRefundCompletedEvent(RefundCompletedEvent event) {
        saveOutboxEvent(
                "Refund",
                event.getOrderId().toString(),
                "RefundCompletedEvent",
                KafkaConfig.TOPIC_REFUND_COMPLETED,
                event
        );
        log.info("Outbox ??  ???????   : RefundCompletedEvent, refundId={}", event.getRefundId());
    }

    public void publishRefundFailedEvent(RefundFailedEvent event) {
        saveOutboxEvent(
                "Refund",
                event.getOrderId().toString(),
                "RefundFailedEvent",
                KafkaConfig.TOPIC_REFUND_FAILED,
                event
        );
        log.info("Outbox ??  ???????   : RefundFailedEvent, refundId={}", event.getRefundId());
    }

    private void saveOutboxEvent(String aggregateType, String aggregateId,
                                  String eventType, String topic, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.create(
                    aggregateType,
                    aggregateId,
                    eventType,
                    topic,
                    payload
            );
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Outbox ??  ??   ?????  : eventType={}", eventType, e);
            throw new RuntimeException("??  ??   ?????  ", e);
        }
    }
}
