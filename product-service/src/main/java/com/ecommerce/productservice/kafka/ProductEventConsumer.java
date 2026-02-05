package com.ecommerce.productservice.kafka;

import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.event.*;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;

    @KafkaListener(topics = "order-created", groupId = "product-service")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order-created event: orderId={}, items={}",
                event.getOrderId(), event.getItems().size());

        List<StockDecreasedEvent.StockItemEvent> decreasedItems = new ArrayList<>();
        List<Product> productsToRestore = new ArrayList<>();

        try {
            for (OrderCreatedEvent.OrderItemEvent item : event.getItems()) {
                Optional<Product> productOpt = productRepository.findByIdAndIsActiveTrue(item.getProductId());

                if (productOpt.isEmpty()) {
                    rollbackStock(productsToRestore, decreasedItems);
                    sendStockDecreaseFailed(event, item.getProductId(), "상품을 찾을 수 없습니다.");
                    return;
                }

                Product product = productOpt.get();

                if (product.getStockQuantity() < item.getQuantity()) {
                    rollbackStock(productsToRestore, decreasedItems);
                    sendStockDecreaseFailed(event, item.getProductId(),
                            String.format("재고 부족 (현재: %d, 요청: %d)",
                                    product.getStockQuantity(), item.getQuantity()));
                    return;
                }

                product.decreaseStock(item.getQuantity());
                productsToRestore.add(product);
                decreasedItems.add(StockDecreasedEvent.StockItemEvent.builder()
                        .productId(product.getId())
                        .quantity(item.getQuantity())
                        .remainingStock(product.getStockQuantity())
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

            productEventProducer.sendStockDecreasedEvent(successEvent);
            log.info("Stock decreased successfully for order: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("Error processing order-created event: orderId={}", event.getOrderId(), e);
            rollbackStock(productsToRestore, decreasedItems);
            sendStockDecreaseFailed(event, null, "재고 처리 중 오류 발생: " + e.getMessage());
        }
    }

    private void rollbackStock(List<Product> products, List<StockDecreasedEvent.StockItemEvent> decreasedItems) {
        for (int i = 0; i < decreasedItems.size(); i++) {
            StockDecreasedEvent.StockItemEvent item = decreasedItems.get(i);
            Product product = products.get(i);
            product.increaseStock(item.getQuantity());
            log.info("Rolled back stock for product: productId={}, quantity={}",
                    item.getProductId(), item.getQuantity());
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
        productEventProducer.sendStockDecreaseFailedEvent(failedEvent);
    }

    @KafkaListener(topics = "order-cancelled", groupId = "product-service")
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order-cancelled event: orderId={}", event.getOrderId());

        for (OrderCancelledEvent.OrderItemEvent item : event.getItems()) {
            productRepository.findByIdAndIsActiveTrue(item.getProductId())
                    .ifPresent(product -> {
                        product.increaseStock(item.getQuantity());
                        log.info("Stock restored for product: productId={}, quantity={}",
                                item.getProductId(), item.getQuantity());
                    });
        }

        log.info("Stock restoration completed for order: orderId={}", event.getOrderId());
    }
}
