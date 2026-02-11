package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.config.KafkaConfig;
import com.ecommerce.paymentservice.event.PaymentCancelledEvent;
import com.ecommerce.paymentservice.event.PaymentCompletedEvent;
import com.ecommerce.paymentservice.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("Publishing payment-completed event: paymentId={}, orderNumber={}",
                event.getPaymentId(), event.getOrderNumber());
        kafkaTemplate.send(KafkaConfig.TOPIC_PAYMENT_COMPLETED, event.getPaymentNumber(), event);
    }

    public void sendPaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Publishing payment-failed event: paymentId={}, orderNumber={}",
                event.getPaymentId(), event.getOrderNumber());
        kafkaTemplate.send(KafkaConfig.TOPIC_PAYMENT_FAILED, event.getPaymentNumber(), event);
    }

    public void sendPaymentCancelledEvent(PaymentCancelledEvent event) {
        log.info("Publishing payment-cancelled event: paymentId={}, orderNumber={}",
                event.getPaymentId(), event.getOrderNumber());
        kafkaTemplate.send(KafkaConfig.TOPIC_PAYMENT_CANCELLED, event.getPaymentNumber(), event);
    }
}
