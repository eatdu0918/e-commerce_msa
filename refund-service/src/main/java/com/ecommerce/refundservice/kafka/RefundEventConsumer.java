package com.ecommerce.refundservice.kafka;

import com.ecommerce.refundservice.entity.ProcessedEvent;
import com.ecommerce.refundservice.entity.Refund;
import com.ecommerce.refundservice.enums.RefundReason;
import com.ecommerce.refundservice.event.CancelApprovedEvent;
import com.ecommerce.refundservice.event.RefundCompletedEvent;
import com.ecommerce.refundservice.event.RefundFailedEvent;
import com.ecommerce.refundservice.repository.ProcessedEventRepository;
import com.ecommerce.refundservice.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundEventConsumer {

        private final RefundRepository refundRepository;
        private final RefundEventProducer refundEventProducer;
        private final ProcessedEventRepository processedEventRepository;

        @KafkaListener(topics = "cancel-approved", groupId = "refund-service")
        @Transactional
        public void handleCancelApproved(CancelApprovedEvent event) {
                if (isDuplicate(event.getEventId(), "cancel-approved"))
                        return;

                log.info("Received cancel-approved event: cancelId={}, orderId={}",
                                event.getCancelId(), event.getOrderId());

                try {
                        BigDecimal totalAmount = event.getItems().stream()
                                        .map(item -> item.getUnitPrice()
                                                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        Refund refund = Refund.create(
                                        event.getOrderId(),
                                        event.getCancelId(),
                                        0L,
                                        event.getUserId(),
                                        RefundReason.ORDER_CANCEL,
                                        "취소 승인에 의한 자동 환불",
                                        totalAmount);

                        refund.process();
                        Refund savedRefund = refundRepository.save(refund);

                        savedRefund.complete();

                        RefundCompletedEvent completedEvent = RefundCompletedEvent.builder()
                                        .eventId(UUID.randomUUID().toString())
                                        .refundId(savedRefund.getId())
                                        .refundNumber(savedRefund.getRefundNumber())
                                        .orderId(savedRefund.getOrderId())
                                        .cancelId(savedRefund.getCancelId())
                                        .paymentId(savedRefund.getPaymentId())
                                        .userId(savedRefund.getUserId())
                                        .amount(savedRefund.getAmount())
                                        .build();

                        refundEventProducer.sendRefundCompletedEvent(completedEvent);
                        log.info("Refund completed: refundId={}", savedRefund.getId());

                        markProcessed(event.getEventId(), "cancel-approved");

                } catch (Exception e) {
                        log.error("Refund processing failed for cancelId={}", event.getCancelId(), e);

                        Refund refund = Refund.create(
                                        event.getOrderId(),
                                        event.getCancelId(),
                                        0L,
                                        event.getUserId(),
                                        RefundReason.ORDER_CANCEL,
                                        "취소 승인에 의한 자동 환불",
                                        BigDecimal.ZERO);
                        refund.fail(e.getMessage());
                        Refund savedRefund = refundRepository.save(refund);

                        RefundFailedEvent failedEvent = RefundFailedEvent.builder()
                                        .eventId(UUID.randomUUID().toString())
                                        .refundId(savedRefund.getId())
                                        .refundNumber(savedRefund.getRefundNumber())
                                        .orderId(savedRefund.getOrderId())
                                        .cancelId(savedRefund.getCancelId())
                                        .paymentId(savedRefund.getPaymentId())
                                        .userId(savedRefund.getUserId())
                                        .amount(savedRefund.getAmount())
                                        .reason(e.getMessage())
                                        .build();

                        refundEventProducer.sendRefundFailedEvent(failedEvent);
                }
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
