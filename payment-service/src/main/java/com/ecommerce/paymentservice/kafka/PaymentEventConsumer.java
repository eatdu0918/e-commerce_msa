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
                log.info("?? ? ?   ??   ?? Outbox/Kafka ?? ????  : paymentId={}, orderId={}",
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
     * ?  ?    ?  ???  ????    ???  ???      ???   ??   ??   ??(order-cancelled??    ???  ??    ?
     */
    @KafkaListener(topics = "cancel-approved", groupId = "payment-service")
    @Transactional
    public void handleCancelApproved(CancelApprovedEvent event) {
        if (isDuplicate(event.getEventId(), "cancel-approved"))
            return;

        log.info("Received cancel-approved (   ??   ??: cancelId={}, orderId={}",
                event.getCancelId(), event.getOrderId());

        var paymentOp = paymentRepository.findByOrderId(event.getOrderId());
        if (paymentOp.isEmpty()) {
            log.warn("cancel-approved    ???  ?: orderId={}??????   ????  ??? ??  ??  . cancelId={}",
                    event.getOrderId(), event.getCancelId());
        }
        paymentOp.ifPresent(payment -> {
            if (applyPaymentSettlementForCancelApproved(payment, event.getRequestType())) {
                log.info("?  ?    ?  ???  ????       ???       ?? paymentId={}, status={}",
                        payment.getId(), payment.getStatus());
                publishPaymentCancelledOutbox(payment);
            } else {
                log.info("   ???? ? ?  ????  ?? cancel-approved    ??   ????  : paymentId={}, status={}",
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
                log.info("     ?  ????       ???  ??   ?? paymentId={}, status={}",
                        payment.getId(), payment.getStatus());
                publishPaymentCancelledOutbox(payment);
            } else {
                log.info("   ???? ? ?  ????  ?? order-cancelled    ??   ????  : paymentId={}, status={}",
                        payment.getId(), payment.getStatus());
            }
        });

        markProcessed(event.getEventId(), "order-cancelled");
    }

    /**
     * ?  ???  ????  ??       ??  ?? {@link PaymentStatus#REFUNDED}, ?  ????     ?  ???{@link PaymentStatus#CANCELLED}.
     * {@code order-cancelled}    ?  ? {@link PaymentStatus#CANCELLED} ?   ??   ?? ?        ??  ?? ????   ?    ??   ??  .
     */
    private boolean applyPaymentSettlementForCancelApproved(Payment payment, String requestTypeFromEvent) {
        boolean returnRefund = requestTypeFromEvent != null
                && "RETURN_REFUND".equalsIgnoreCase(requestTypeFromEvent.trim());
        PaymentStatus status = payment.getStatus();
        if (returnRefund) {
            if (status == PaymentStatus.REFUNDED) {
                return false;
            }
            payment.refund();
            return true;
        }
        return applyPaymentSettlementForOrderCancellation(payment);
    }

    /**
     *      ?  ???  ???  ????  ?? ??   ?  ?{@link PaymentStatus#CANCELLED} ?   ???
     * ?? ? ?   ??   ?????  ??   ?  ???    ???  ??     ?  ??? ???????  ??  ??
     *
     * @return {@code true}?? ???   ??     ? ?? ?Outbox ??  ??    ???   ??
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
            log.warn("   ????  ???  ?? eventId={}, eventType={}", eventId, eventType);
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
