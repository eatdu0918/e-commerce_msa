package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.event.*;
import com.ecommerce.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @KafkaListener(topics = "stock-decreased", groupId = "order-service")
    public void handleStockDecreased(StockDecreasedEvent event) {
        log.info("Received stock-decreased event: orderId={}", event.getOrderId());
    }

    @KafkaListener(topics = "stock-decrease-failed", groupId = "order-service")
    @Transactional
    public void handleStockDecreaseFailed(StockDecreaseFailedEvent event) {
        log.info("Received stock-decrease-failed event: orderId={}, reason={}",
                event.getOrderId(), event.getReason());

        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.cancel();
            log.info("Order cancelled due to stock decrease failure: orderId={}", event.getOrderId());
        });
    }

    @KafkaListener(topics = "coupon-used", groupId = "order-service")
    @Transactional
    public void handleCouponUsed(CouponUsedEvent event) {
        log.info("Received coupon-used event: orderId={}, discountAmount={}",
                event.getOrderId(), event.getDiscountAmount());

        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            if (event.getDiscountAmount() != null && event.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                order.applyDiscount(event.getDiscountAmount());
            }
            order.confirm();
            log.info("Order confirmed: orderId={}, finalAmount={}", order.getId(), order.getFinalAmount());
        });
    }

    @KafkaListener(topics = "coupon-use-failed", groupId = "order-service")
    @Transactional
    public void handleCouponUseFailed(CouponUseFailedEvent event) {
        log.info("Received coupon-use-failed event: orderId={}, reason={}",
                event.getOrderId(), event.getReason());

        orderRepository.findByIdWithItems(event.getOrderId()).ifPresent(order -> {
            order.cancel();

            List<OrderCancelledEvent.OrderItemEvent> items = order.getOrderItems().stream()
                    .map(item -> OrderCancelledEvent.OrderItemEvent.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .build())
                    .toList();

            OrderCancelledEvent cancelledEvent = OrderCancelledEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userId(order.getUserId())
                    .userCouponId(order.getUserCouponId())
                    .items(items)
                    .build();

            orderEventProducer.sendOrderCancelledEvent(cancelledEvent);
            log.info("Order cancelled due to coupon use failure: orderId={}", event.getOrderId());
        });
    }
}
