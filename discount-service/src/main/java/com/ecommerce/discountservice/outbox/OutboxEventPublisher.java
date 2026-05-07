package com.ecommerce.discountservice.outbox;

import com.ecommerce.discountservice.config.KafkaConfig;
import com.ecommerce.discountservice.entity.OutboxEvent;
import com.ecommerce.discountservice.event.CouponRestoredEvent;
import com.ecommerce.discountservice.event.CouponUseFailedEvent;
import com.ecommerce.discountservice.event.CouponUsedEvent;
import com.ecommerce.discountservice.repository.OutboxEventRepository;
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

    public void publishCouponUsedEvent(CouponUsedEvent event) {
        saveOutboxEvent(
                "Coupon",
                event.getOrderNumber(),
                "CouponUsedEvent",
                KafkaConfig.TOPIC_COUPON_USED,
                event
        );
        log.info("Outbox ??  ???????   : CouponUsedEvent, orderId={}", event.getOrderId());
    }

    public void publishCouponUseFailedEvent(CouponUseFailedEvent event) {
        saveOutboxEvent(
                "Coupon",
                event.getOrderNumber(),
                "CouponUseFailedEvent",
                KafkaConfig.TOPIC_COUPON_USE_FAILED,
                event
        );
        log.info("Outbox ??  ???????   : CouponUseFailedEvent, orderId={}", event.getOrderId());
    }

    public void publishCouponRestoredEvent(CouponRestoredEvent event) {
        saveOutboxEvent(
                "Coupon",
                event.getOrderNumber(),
                "CouponRestoredEvent",
                KafkaConfig.TOPIC_COUPON_RESTORED,
                event
        );
        log.info("Outbox ??  ???????   : CouponRestoredEvent, orderId={}", event.getOrderId());
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
