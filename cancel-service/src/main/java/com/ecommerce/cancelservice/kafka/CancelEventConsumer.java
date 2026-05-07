package com.ecommerce.cancelservice.kafka;

import com.ecommerce.cancelservice.entity.ProcessedEvent;
import com.ecommerce.cancelservice.event.PaymentCancelledEvent;
import com.ecommerce.cancelservice.event.RefundCompletedEvent;
import com.ecommerce.cancelservice.event.RefundFailedEvent;
import com.ecommerce.cancelservice.repository.CancelRepository;
import com.ecommerce.cancelservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelEventConsumer {

    private final CancelRepository cancelRepository;
    private final ProcessedEventRepository processedEventRepository;

    /**
     *    ???  ???    ??Cancel ?   ??COMPLETED ?    ?
     */
    @KafkaListener(topics = "payment-cancelled", groupId = "cancel-service")
    @Transactional
    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        if (isDuplicate(event.getEventId(), "payment-cancelled"))
            return;

        log.info("Received payment-cancelled event: orderId={}", event.getOrderId());

        cancelRepository.findByOrderId(event.getOrderId())
                .ifPresent(cancel -> {
                    cancel.complete();
                    log.info("Cancel completed (payment cancelled): cancelId={}", cancel.getId());
                });

        markProcessed(event.getEventId(), "payment-cancelled");
    }

    /**
     * ??   ?    ??Cancel ?   ??COMPLETED ?    ?
     */
    @KafkaListener(topics = "refund-completed", groupId = "cancel-service")
    @Transactional
    public void handleRefundCompleted(RefundCompletedEvent event) {
        if (isDuplicate(event.getEventId(), "refund-completed"))
            return;

        log.info("Received refund-completed event: cancelId={}, refundId={}",
                event.getCancelId(), event.getRefundId());

        cancelRepository.findById(event.getCancelId())
                .ifPresent(cancel -> {
                    cancel.complete();
                    log.info("Cancel completed (refund completed): cancelId={}", cancel.getId());
                });

        markProcessed(event.getEventId(), "refund-completed");
    }

    /**
     * ??   ??   ??   ??    ?(?  ?    ??      ???   )
     */
    @KafkaListener(topics = "refund-failed", groupId = "cancel-service")
    @Transactional
    public void handleRefundFailed(RefundFailedEvent event) {
        if (isDuplicate(event.getEventId(), "refund-failed"))
            return;

        log.error("Received refund-failed event: cancelId={}, refundId={}, reason={}",
                event.getCancelId(), event.getRefundId(), event.getReason());

        cancelRepository.findById(event.getCancelId())
                .ifPresent(cancel -> {
                    log.error("??   ??   ??    ?  ??   ??  ? ? cancelId={}, orderId={}",
                            cancel.getId(), cancel.getOrderId());
                });

        markProcessed(event.getEventId(), "refund-failed");
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
