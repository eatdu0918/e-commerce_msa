package com.ecommerce.cancelservice.kafka;

import com.ecommerce.cancelservice.config.KafkaConfig;
import com.ecommerce.cancelservice.event.CancelApprovedEvent;
import com.ecommerce.cancelservice.event.CancelRejectedEvent;
import com.ecommerce.cancelservice.event.CancelRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCancelRequestedEvent(CancelRequestedEvent event) {
        log.info("Publishing cancel-requested event: cancelId={}, orderNumber={}",
                event.getCancelId(), event.getOrderNumber());
        kafkaTemplate.send(KafkaConfig.TOPIC_CANCEL_REQUESTED, event.getCancelNumber(), event);
    }

    public void sendCancelApprovedEvent(CancelApprovedEvent event) {
        log.info("Publishing cancel-approved event: cancelId={}, orderNumber={}",
                event.getCancelId(), event.getOrderNumber());
        kafkaTemplate.send(KafkaConfig.TOPIC_CANCEL_APPROVED, event.getCancelNumber(), event);
    }

    public void sendCancelRejectedEvent(CancelRejectedEvent event) {
        log.info("Publishing cancel-rejected event: cancelId={}, orderNumber={}",
                event.getCancelId(), event.getOrderNumber());
        kafkaTemplate.send(KafkaConfig.TOPIC_CANCEL_REJECTED, event.getCancelNumber(), event);
    }
}
