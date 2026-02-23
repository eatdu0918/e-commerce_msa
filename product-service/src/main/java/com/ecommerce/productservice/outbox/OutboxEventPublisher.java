package com.ecommerce.productservice.outbox;

import com.ecommerce.productservice.config.KafkaConfig;
import com.ecommerce.productservice.entity.OutboxEvent;
import com.ecommerce.productservice.event.StockDecreaseFailedEvent;
import com.ecommerce.productservice.event.StockDecreasedEvent;
import com.ecommerce.productservice.repository.OutboxEventRepository;
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

    public void publishStockDecreasedEvent(StockDecreasedEvent event) {
        saveOutboxEvent(
                "Product",
                event.getOrderNumber(),
                "StockDecreasedEvent",
                KafkaConfig.TOPIC_STOCK_DECREASED,
                event
        );
        log.info("Outbox 이벤트 저장 완료: StockDecreasedEvent, orderId={}", event.getOrderId());
    }

    public void publishStockDecreaseFailedEvent(StockDecreaseFailedEvent event) {
        saveOutboxEvent(
                "Product",
                event.getOrderNumber(),
                "StockDecreaseFailedEvent",
                KafkaConfig.TOPIC_STOCK_DECREASE_FAILED,
                event
        );
        log.info("Outbox 이벤트 저장 완료: StockDecreaseFailedEvent, orderId={}", event.getOrderId());
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
            log.error("Outbox 이벤트 직렬화 실패: eventType={}", eventType, e);
            throw new RuntimeException("이벤트 직렬화 실패", e);
        }
    }
}
