package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.entity.ProcessedEvent;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.event.CouponUsedEvent;
import com.ecommerce.paymentservice.event.OrderCancelledEvent;
import com.ecommerce.paymentservice.event.PaymentCancelledEvent;
import com.ecommerce.paymentservice.event.PaymentCompletedEvent;
import com.ecommerce.paymentservice.event.PaymentFailedEvent;
import com.ecommerce.paymentservice.outbox.OutboxEventPublisher;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import com.ecommerce.paymentservice.repository.ProcessedEventRepository;
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
    private final OutboxEventPublisher outboxEventPublisher;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "coupon-used", groupId = "payment-service")
    @Transactional
    public void handleCouponUsed(CouponUsedEvent event) {
        if (isDuplicate(event.getEventId(), "coupon-used"))
            return;

        log.info("Received coupon-used event: orderId={}", event.getOrderId());

        paymentRepository.findByOrderId(event.getOrderId()).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                log.info("이미 완료된 결제, Outbox/Kafka 재발행 생략: paymentId={}, orderId={}",
                        payment.getId(), event.getOrderId());
                markProcessed(event.getEventId(), "coupon-used");
                return;
            }
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

                outboxEventPublisher.publishPaymentCompletedEvent(completedEvent);

                markProcessed(event.getEventId(), "coupon-used");
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

                outboxEventPublisher.publishPaymentFailedEvent(failedEvent);
            }
        });
    }

    @KafkaListener(topics = "order-cancelled", groupId = "payment-service")
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if (isDuplicate(event.getEventId(), "order-cancelled"))
            return;

        log.info("Received order-cancelled event: orderId={}", event.getOrderId());

        paymentRepository.findByOrderId(event.getOrderId()).ifPresent(payment -> {
            payment.cancel();
            log.info("Payment cancelled: paymentId={}", payment.getId());

            PaymentCancelledEvent cancelledEvent = PaymentCancelledEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .paymentId(payment.getId())
                    .paymentNumber(payment.getPaymentNumber())
                    .orderId(payment.getOrderId())
                    .orderNumber(payment.getOrderNumber())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .build();

            outboxEventPublisher.publishPaymentCancelledEvent(cancelledEvent);

            markProcessed(event.getEventId(), "order-cancelled");
        });
    }

    private boolean isDuplicate(String eventId, String eventType) {
        if (eventId != null && processedEventRepository.existsByEventId(eventId)) {
            log.warn("중복 이벤트 무시: eventId={}, eventType={}", eventId, eventType);
            return true;
        }
        return false;
    }

    private void markProcessed(String eventId, String eventType) {
        if (eventId != null) {
            processedEventRepository.save(ProcessedEvent.create(eventId, eventType));
        }
    }
}
