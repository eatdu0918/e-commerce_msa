package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.event.CouponUsedEvent;
import com.ecommerce.paymentservice.event.OrderCancelledEvent;
import com.ecommerce.paymentservice.event.PaymentCompletedEvent;
import com.ecommerce.paymentservice.event.PaymentFailedEvent;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @KafkaListener(topics = "coupon-used", groupId = "payment-service")
    @Transactional
    public void handleCouponUsed(CouponUsedEvent event) {
        log.info("Received coupon-used event: orderId={}", event.getOrderId());

        paymentRepository.findByOrderId(event.getOrderId()).ifPresent(payment -> {
            try {
                payment.complete();
                log.info("Payment completed: paymentId={}", payment.getId());

                PaymentCompletedEvent completedEvent = PaymentCompletedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .paymentId(payment.getId())
                        .paymentNumber(payment.getPaymentNumber())
                        .orderId(payment.getOrderId())
                        .orderNumber(payment.getOrderNumber())
                        .userId(payment.getUserId())
                        .amount(payment.getAmount())
                        .paymentMethod(payment.getPaymentMethod().name())
                        .build();

                paymentEventProducer.sendPaymentCompletedEvent(completedEvent);
            } catch (Exception e) {
                log.error("Payment processing failed: paymentId={}", payment.getId(), e);
                payment.fail(e.getMessage());

                PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .paymentId(payment.getId())
                        .paymentNumber(payment.getPaymentNumber())
                        .orderId(payment.getOrderId())
                        .orderNumber(payment.getOrderNumber())
                        .userId(payment.getUserId())
                        .amount(payment.getAmount())
                        .reason(e.getMessage())
                        .build();

                paymentEventProducer.sendPaymentFailedEvent(failedEvent);
            }
        });
    }

    @KafkaListener(topics = "order-cancelled", groupId = "payment-service")
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order-cancelled event: orderId={}", event.getOrderId());

        paymentRepository.findByOrderId(event.getOrderId()).ifPresent(payment -> {
            payment.cancel();
            log.info("Payment cancelled: paymentId={}", payment.getId());
        });
    }
}
