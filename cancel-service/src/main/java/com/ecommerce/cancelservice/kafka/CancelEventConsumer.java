package com.ecommerce.cancelservice.kafka;

import com.ecommerce.cancelservice.event.PaymentCancelledEvent;
import com.ecommerce.cancelservice.repository.CancelRepository;
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

    @KafkaListener(topics = "payment-cancelled", groupId = "cancel-service")
    @Transactional
    public void handlePaymentCancelled(PaymentCancelledEvent event) {
        log.info("Received payment-cancelled event: orderId={}", event.getOrderId());

        cancelRepository.findAll().stream()
                .filter(cancel -> cancel.getOrderId().equals(event.getOrderId()))
                .findFirst()
                .ifPresent(cancel -> {
                    cancel.complete();
                    log.info("Cancel completed: cancelId={}", cancel.getId());
                });
    }
}
