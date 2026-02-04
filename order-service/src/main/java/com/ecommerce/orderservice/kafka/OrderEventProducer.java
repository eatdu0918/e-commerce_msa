package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.config.KafkaConfig;
import com.ecommerce.orderservice.event.OrderCancelledEvent;
import com.ecommerce.orderservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing order-created event: orderId={}, orderNumber={}",
                event.getOrderId(), event.getOrderNumber());
        kafkaTemplate.send(KafkaConfig.TOPIC_ORDER_CREATED, event.getOrderNumber(), event);
    }

    public void sendOrderCancelledEvent(OrderCancelledEvent event) {
        log.info("Publishing order-cancelled event: orderId={}, orderNumber={}",
                event.getOrderId(), event.getOrderNumber());
        kafkaTemplate.send(KafkaConfig.TOPIC_ORDER_CANCELLED, event.getOrderNumber(), event);
    }
}
