package com.ecommerce.orderservice.outbox;

import com.ecommerce.orderservice.entity.OutboxEvent;
import com.ecommerce.orderservice.event.OrderCancelledEvent;
import com.ecommerce.orderservice.event.OrderCreatedEvent;
import com.ecommerce.orderservice.config.KafkaConfig;
import com.ecommerce.orderservice.repository.OutboxEventRepository;
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

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        saveOutboxEvent(
                "Order",
                event.getOrderNumber(),
                "OrderCreatedEvent",
                KafkaConfig.TOPIC_ORDER_CREATED,
                event
        );
        log.info("Outbox ??  ???????   : OrderCreatedEvent, orderId={}", event.getOrderId());
    }

    public void publishOrderCancelledEvent(OrderCancelledEvent event) {
        saveOutboxEvent(
                "Order",
                event.getOrderNumber(),
                "OrderCancelledEvent",
                KafkaConfig.TOPIC_ORDER_CANCELLED,
                event
        );
        log.info("Outbox ??  ???????   : OrderCancelledEvent, orderId={}", event.getOrderId());
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
