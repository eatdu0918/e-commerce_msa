package com.ecommerce.refundservice.kafka;

import com.ecommerce.refundservice.config.KafkaConfig;
import com.ecommerce.refundservice.event.RefundCompletedEvent;
import com.ecommerce.refundservice.event.RefundFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendRefundCompletedEvent(RefundCompletedEvent event) {
        log.info("Publishing refund-completed event: refundId={}, orderId={}",
                event.getRefundId(), event.getOrderId());
        kafkaTemplate.send(KafkaConfig.TOPIC_REFUND_COMPLETED, event.getRefundNumber(), event);
    }

    public void sendRefundFailedEvent(RefundFailedEvent event) {
        log.info("Publishing refund-failed event: refundId={}, orderId={}",
                event.getRefundId(), event.getOrderId());
        kafkaTemplate.send(KafkaConfig.TOPIC_REFUND_FAILED, event.getRefundNumber(), event);
    }
}
