package com.ecommerce.paymentservice.kafka;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.entity.ProcessedEvent;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.event.CancelApprovedEvent;
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

    /**
     * 관리자 취소 승인 시 주문과 무관하게 결제 상태를 즉시 반영 (order-cancelled와 중복되어도 멱등)
     */
    @KafkaListener(topics = "cancel-approved", groupId = "payment-service")
    @Transactional
    public void handleCancelApproved(CancelApprovedEvent event) {
        if (isDuplicate(event.getEventId(), "cancel-approved"))
            return;

        log.info("Received cancel-approved (결제 반영): cancelId={}, orderId={}",
                event.getCancelId(), event.getOrderId());

        var paymentOp = paymentRepository.findByOrderId(event.getOrderId());
        if (paymentOp.isEmpty()) {
            log.warn("cancel-approved 처리 불가: orderId={}에 대한 결제 레코드가 없습니다. cancelId={}",
                    event.getOrderId(), event.getCancelId());
        }
        paymentOp.ifPresent(payment -> {
            if (applyPaymentSettlementForOrderCancellation(payment)) {
                log.info("관리자 취소 승인에 따른 결제 상태 반영: paymentId={}, status={}",
                        payment.getId(), payment.getStatus());
                publishPaymentCancelledOutbox(payment);
            } else {
                log.info("결제 이미 취소/환불됨, cancel-approved 결제 처리 생략: paymentId={}, status={}",
                        payment.getId(), payment.getStatus());
            }
        });

        markProcessed(event.getEventId(), "cancel-approved");
    }

    @KafkaListener(topics = "order-cancelled", groupId = "payment-service")
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if (isDuplicate(event.getEventId(), "order-cancelled"))
            return;

        log.info("Received order-cancelled event: orderId={}", event.getOrderId());

        paymentRepository.findByOrderId(event.getOrderId()).ifPresent(payment -> {
            if (applyPaymentSettlementForOrderCancellation(payment)) {
                log.info("주문 취소에 따른 결제 취소 반영: paymentId={}, status={}",
                        payment.getId(), payment.getStatus());
                publishPaymentCancelledOutbox(payment);
            } else {
                log.info("결제 이미 취소/환불됨, order-cancelled 결제 처리 생략: paymentId={}, status={}",
                        payment.getId(), payment.getStatus());
            }
        });

        markProcessed(event.getEventId(), "order-cancelled");
    }

    /**
     * 주문 취소(승인/취소 이벤트) 시 결제를 {@link PaymentStatus#CANCELLED}로 맞춘다.
     * 이미 완료된 결제도 동일하게 취소 상태로 표기해 주문 취소와 의미를 일치시킨다.
     *
     * @return {@code true}이면 상태를 변경했으며 Outbox 이벤트 발행이 필요함
     */
    private boolean applyPaymentSettlementForOrderCancellation(Payment payment) {
        PaymentStatus status = payment.getStatus();
        if (status == PaymentStatus.CANCELLED || status == PaymentStatus.REFUNDED) {
            return false;
        }
        payment.cancel();
        return true;
    }

    private void publishPaymentCancelledOutbox(Payment payment) {
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
