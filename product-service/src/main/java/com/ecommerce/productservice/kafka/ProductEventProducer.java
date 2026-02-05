package com.ecommerce.productservice.kafka;

import com.ecommerce.productservice.config.KafkaConfig;
import com.ecommerce.productservice.event.StockDecreaseFailedEvent;
import com.ecommerce.productservice.event.StockDecreasedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendStockDecreasedEvent(StockDecreasedEvent event) {
        log.info("Publishing stock-decreased event: orderId={}", event.getOrderId());
        kafkaTemplate.send(KafkaConfig.TOPIC_STOCK_DECREASED, event.getOrderNumber(), event);
    }

    public void sendStockDecreaseFailedEvent(StockDecreaseFailedEvent event) {
        log.info("Publishing stock-decrease-failed event: orderId={}, productId={}, reason={}",
                event.getOrderId(), event.getProductId(), event.getReason());
        kafkaTemplate.send(KafkaConfig.TOPIC_STOCK_DECREASE_FAILED, event.getOrderNumber(), event);
    }
}
