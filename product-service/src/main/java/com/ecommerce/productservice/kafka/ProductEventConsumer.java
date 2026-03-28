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
import java.util.Map;
import java.util.stream.Collectors;
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
            
            // 1. 멱등성 체크
            if (isDuplicate(event.getEventId(), "order-created")) {
                log.info("Duplicate event detected: eventId={}", event.getEventId());
                return;
            }

            log.info("Starting inventory processing for order: orderId={}, pieces={}", 
                    event.getOrderId(), event.getItems().size());

            // 2. 상품별로 수량 집계 (동일 상품 ID 중복 시 합산)
            Map<Long, Integer> aggregatedItems = event.getItems().stream()
                    .collect(Collectors.groupingBy(
                            OrderCreatedEvent.OrderItemEvent::getProductId,
                            Collectors.summingInt(OrderCreatedEvent.OrderItemEvent::getQuantity)
                    ));

            List<StockDecreasedEvent.StockItemEvent> decreasedItemsCount = new ArrayList<>();

            // 3. 재고 차감 처리
            for (Map.Entry<Long, Integer> entry : aggregatedItems.entrySet()) {
                Long productId = entry.getKey();
                Integer totalQuantity = entry.getValue();

                log.debug("Decreasing stock: productId={}, totalQuantity={}", productId, totalQuantity);
                StockResponse response = stockService.decreaseStock(StockRequest.of(productId, totalQuantity));
                
                decreasedItemsCount.add(StockDecreasedEvent.StockItemEvent.builder()
                        .productId(productId)
                        .quantity(totalQuantity)
                        .remainingStock(response.getStockQuantity())
                        .build());
            }

            // 4. 이벤트 처리 완료 마킹 (unique constraint를 통한 동시성 제어)
            markProcessed(event.getEventId(), "order-created");

            // 5. 성공 이벤트 발행 (동일 트랜재션 내에서 아웃박스 테이블에 저장)
            StockDecreasedEvent successEvent = StockDecreasedEvent.builder()
                    .eventId(event.getEventId())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .totalAmount(event.getTotalAmount())
                    .userCouponId(event.getUserCouponId())
                    .items(decreasedItemsCount)
                    .build();

            outboxEventPublisher.publishStockDecreasedEvent(successEvent);
            log.info("Inventory processed successfully for order: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error during order processing. Transaction will be rolled back. error={}", e.getMessage(), e);
            // 런타임 예외를 던져 트랜잭션 롤백 유도
            throw new RuntimeException("Inventory processing error: " + e.getMessage(), e);
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
