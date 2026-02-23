package com.ecommerce.paymentservice.outbox;

import com.ecommerce.paymentservice.config.KafkaConfig;
import com.ecommerce.paymentservice.entity.OutboxEvent;
import com.ecommerce.paymentservice.event.PaymentCancelledEvent;
import com.ecommerce.paymentservice.event.PaymentCompletedEvent;
import com.ecommerce.paymentservice.event.PaymentFailedEvent;
import com.ecommerce.paymentservice.repository.OutboxEventRepository;
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

    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        saveOutboxEvent(
                "Payment",
                event.getOrderNumber(),
                "PaymentCompletedEvent",
                KafkaConfig.TOPIC_PAYMENT_COMPLETED,
                event
        );
        log.info("Outbox 이벤트 저장 완료: PaymentCompletedEvent, paymentId={}", event.getPaymentId());
    }

    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        saveOutboxEvent(
                "Payment",
                event.getOrderNumber(),
                "PaymentFailedEvent",
                KafkaConfig.TOPIC_PAYMENT_FAILED,
                event
        );
        log.info("Outbox 이벤트 저장 완료: PaymentFailedEvent, paymentId={}", event.getPaymentId());
    }

    public void publishPaymentCancelledEvent(PaymentCancelledEvent event) {
        saveOutboxEvent(
                "Payment",
                event.getOrderNumber(),
                "PaymentCancelledEvent",
                KafkaConfig.TOPIC_PAYMENT_CANCELLED,
                event
        );
        log.info("Outbox 이벤트 저장 완료: PaymentCancelledEvent, paymentId={}", event.getPaymentId());
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
            log.error("Outbox 이벤트 직렬화 실패: eventType={}", eventType, e);
            throw new RuntimeException("이벤트 직렬화 실패", e);
        }
    }
}
