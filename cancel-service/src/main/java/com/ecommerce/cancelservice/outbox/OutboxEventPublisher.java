package com.ecommerce.cancelservice.outbox;

import com.ecommerce.cancelservice.config.KafkaConfig;
import com.ecommerce.cancelservice.entity.OutboxEvent;
import com.ecommerce.cancelservice.event.CancelApprovedEvent;
import com.ecommerce.cancelservice.event.CancelRejectedEvent;
import com.ecommerce.cancelservice.event.CancelRequestedEvent;
import com.ecommerce.cancelservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publishCancelRequestedEvent(CancelRequestedEvent event) {
        saveOutboxEvent(
                "Cancel",
                event.getOrderNumber(),
                "CancelRequestedEvent",
                KafkaConfig.TOPIC_CANCEL_REQUESTED,
                event
        );
        log.info("Outbox ??  ???????   : CancelRequestedEvent, cancelId={}", event.getCancelId());
    }

    public void publishCancelApprovedEvent(CancelApprovedEvent event) {
        saveOutboxEvent(
                "Cancel",
                event.getOrderNumber(),
                "CancelApprovedEvent",
                KafkaConfig.TOPIC_CANCEL_APPROVED,
                event
        );
        log.info("Outbox ??  ???????   : CancelApprovedEvent, cancelId={}", event.getCancelId());
    }

    public void publishCancelRejectedEvent(CancelRejectedEvent event) {
        saveOutboxEvent(
                "Cancel",
                event.getOrderNumber(),
                "CancelRejectedEvent",
                KafkaConfig.TOPIC_CANCEL_REJECTED,
                event
        );
        log.info("Outbox ??  ???????   : CancelRejectedEvent, cancelId={}", event.getCancelId());
    }

    private void saveOutboxEvent(String aggregateType, String aggregateId,
                                  String eventType, String topic, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.create(
                    aggregateType,
                    aggregateId,
                    eventType,
                    topic,
                    payload
            );
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Outbox ??  ??   ?????  : eventType={}", eventType, e);
            throw new RuntimeException("??  ??   ?????  ", e);
        }
    }
}
