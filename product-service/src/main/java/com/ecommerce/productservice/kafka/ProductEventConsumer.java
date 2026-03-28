package com.ecommerce.productservice.kafka;

import com.ecommerce.productservice.entity.ProcessedEvent;
import com.ecommerce.productservice.event.*;
import com.ecommerce.productservice.outbox.OutboxEventPublisher;
import com.ecommerce.productservice.repository.ProcessedEventRepository;
import com.ecommerce.productservice.dto.request.StockRequest;
import com.ecommerce.productservice.dto.response.StockResponse;
import com.ecommerce.productservice.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final OutboxEventPublisher outboxEventPublisher;
    private final ProcessedEventRepository processedEventRepository;
    private final StockService stockService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "product-service")
    @Transactional
    public void handleOrderCreated(String message) {
        log.info("Received order-created raw message: {}", message);

        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            log.info("Parsed order-created event: orderId={}, items={}",
                    event.getOrderId(), event.getItems().size());

            if (isDuplicate(event.getEventId(), "order-created")) {
                return;
            }

            List<StockDecreasedEvent.StockItemEvent> decreasedItems = new ArrayList<>();
            List<Long> productsToRestore = new ArrayList<>();

            try {
                for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
                    // 재고 차감 시도 (내부에서 검증 및 캐시 무효화 수행)
                    StockResponse response = stockService.decreaseStock(StockRequest.of(item.getProductId(), item.getQuantity()));
                    
                    productsToRestore.add(item.getProductId());
                    decreasedItems.add(StockDecreasedEvent.StockItemEvent.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .remainingStock(response.getStockQuantity())
                            .build());
                }

                StockDecreasedEvent successEvent = StockDecreasedEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .orderId(event.getOrderId())
                        .orderNumber(event.getOrderNumber())
                        .userId(event.getUserId())
                        .totalAmount(event.getTotalAmount())
                        .userCouponId(event.getUserCouponId())
                        .items(decreasedItems)
                        .build();

                outboxEventPublisher.publishStockDecreasedEvent(successEvent);
                log.info("Stock decreased successfully for order: orderId={}", event.getOrderId());

                markProcessed(event.getEventId(), "order-created");

            } catch (Exception e) {
                log.error("Error processing inventory for order: orderId={}", event.getOrderId(), e);
                rollbackStockManually(productsToRestore, decreasedItems);
                sendStockDecreaseFailed(event, null, "재고 처리 중 오류 발생: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to parse order-created message: {}", message, e);
        }
    }

    private void rollbackStockManually(List<Long> productIds, List<StockDecreasedEvent.StockItemEvent> decreasedItems) {
        for (int i = 0; i < decreasedItems.size(); i++) {
            StockDecreasedEvent.StockItemEvent item = decreasedItems.get(i);
            Long productId = productIds.get(i);
            try {
                stockService.increaseStock(StockRequest.of(productId, item.getQuantity()));
                log.info("Rolled back stock for product: productId={}, quantity={}",
                        productId, item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to rollback stock for product: productId={}", productId, e);
            }
        }
    }

    private void sendStockDecreaseFailed(OrderCreatedEvent event, Long productId, String reason) {
        StockDecreaseFailedEvent failedEvent = StockDecreaseFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .userId(event.getUserId())
                .productId(productId)
                .reason(reason)
                .build();
        outboxEventPublisher.publishStockDecreaseFailedEvent(failedEvent);
    }

    @KafkaListener(topics = "order-cancelled", groupId = "product-service")
    @Transactional
    public void handleOrderCancelled(String message) {
        log.info("Received order-cancelled raw message: {}", message);

        try {
            OrderCancelledEvent event = objectMapper.readValue(message, OrderCancelledEvent.class);
            log.info("Parsed order-cancelled event: orderId={}", event.getOrderId());

            if (isDuplicate(event.getEventId(), "order-cancelled")) {
                return;
            }

            for (OrderCancelledEvent.OrderItemEvent item : event.getItems()) {
                stockService.restoreStock(StockRequest.of(item.getProductId(), item.getQuantity()));
                log.info("Stock restored for product: productId={}, quantity={}",
                        item.getProductId(), item.getQuantity());
            }

            log.info("Stock restoration completed for order: orderId={}", event.getOrderId());

            markProcessed(event.getEventId(), "order-cancelled");
        } catch (Exception e) {
            log.error("Failed to parse order-cancelled message: {}", message, e);
        }
    }

    private boolean isDuplicate(String eventId, String eventType) {
        boolean exists = processedEventRepository.existsByEventId(eventId);
        if (exists) {
            log.warn("중복 이벤트 무시: eventId={}, eventType={}", eventId, eventType);
        }
        return exists;
    }

    private void markProcessed(String eventId, String eventType) {
        ProcessedEvent processedEvent = ProcessedEvent.create(eventId, eventType);
        processedEventRepository.save(processedEvent);
        log.info("이벤트 처리 완료 기록: eventId={}, eventType={}", eventId, eventType);
    }
}
