package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.entity.ProcessedEvent;
import com.ecommerce.orderservice.event.*;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "stock-decreased", groupId = "order-service")
    public void handleStockDecreased(StockDecreasedEvent event) {
        log.info("Received stock-decreased event: orderId={}", event.getOrderId());
        // 재고 차감 성공 - discount-service에서 후속 처리
    }

    @KafkaListener(topics = "stock-decrease-failed", groupId = "order-service")
    @Transactional
    public void handleStockDecreaseFailed(StockDecreaseFailedEvent event) {
        if (isDuplicate(event.getEventId(), "stock-decrease-failed"))
            return;

        log.info("Received stock-decrease-failed event: orderId={}, reason={}",
                event.getOrderId(), event.getReason());

        orderRepository.findByIdWithItems(event.getOrderId()).ifPresent(order -> {
            if (order.canCancel()) {
                order.cancel();

                // 재고는 product-service 내부에서 이미 롤백됨
                // 쿠폰/결제가 진행됐을 수 있으므로 order-cancelled 이벤트 발행
                OrderCancelledEvent cancelledEvent = OrderCancelledEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .userId(order.getUserId())
                        .userCouponId(order.getUserCouponId())
                        .items(Collections.emptyList())
                        .build();

                orderEventProducer.sendOrderCancelledEvent(cancelledEvent);
                log.info("주문 취소 완료 (재고 부족): orderId={}", event.getOrderId());
            }
        });

        markProcessed(event.getEventId(), "stock-decrease-failed");
    }

    @KafkaListener(topics = "coupon-used", groupId = "order-service")
    @Transactional
    public void handleCouponUsed(CouponUsedEvent event) {
        if (isDuplicate(event.getEventId(), "coupon-used"))
            return;

        log.info("Received coupon-used event: orderId={}, discountAmount={}",
                event.getOrderId(), event.getDiscountAmount());

        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            if (event.getDiscountAmount() != null && event.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                order.applyDiscount(event.getDiscountAmount());
            }
            order.confirm();
            log.info("주문 확정: orderId={}, finalAmount={}", order.getId(), order.getFinalAmount());
        });

        markProcessed(event.getEventId(), "coupon-used");
    }

    @KafkaListener(topics = "coupon-use-failed", groupId = "order-service")
    @Transactional
    public void handleCouponUseFailed(CouponUseFailedEvent event) {
        if (isDuplicate(event.getEventId(), "coupon-use-failed"))
            return;

        log.info("Received coupon-use-failed event: orderId={}, reason={}",
                event.getOrderId(), event.getReason());

        orderRepository.findByIdWithItems(event.getOrderId()).ifPresent(order -> {
            if (order.canCancel()) {
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
                log.info("주문 취소 완료 (쿠폰 적용 실패): orderId={}", event.getOrderId());
            }
        });

        markProcessed(event.getEventId(), "coupon-use-failed");
    }

    /**
     * 결제 실패 시 보상 트랜잭션: 주문 취소 + order-cancelled 이벤트 발행
     * → product-service: 재고 복원, discount-service: 쿠폰 복원
     */
    @KafkaListener(topics = "payment-failed", groupId = "order-service")
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        if (isDuplicate(event.getEventId(), "payment-failed"))
            return;

        log.info("Received payment-failed event: orderId={}, reason={}",
                event.getOrderId(), event.getReason());

        orderRepository.findByIdWithItems(event.getOrderId()).ifPresent(order -> {
            if (order.canCancel()) {
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
                log.info("주문 취소 완료 (결제 실패): orderId={}", event.getOrderId());
            }
        });

        markProcessed(event.getEventId(), "payment-failed");
    }

    /**
     * 취소 승인 시 보상 트랜잭션: 주문 취소 + order-cancelled 이벤트 발행
     * → product-service: 재고 복원, discount-service: 쿠폰 복원, payment-service: 결제 취소
     */
    @KafkaListener(topics = "cancel-approved", groupId = "order-service")
    @Transactional
    public void handleCancelApproved(CancelApprovedEvent event) {
        if (isDuplicate(event.getEventId(), "cancel-approved"))
            return;

        log.info("Received cancel-approved event: cancelId={}, orderId={}",
                event.getCancelId(), event.getOrderId());

        orderRepository.findByIdWithItems(event.getOrderId()).ifPresent(order -> {
            if (order.canCancel()) {
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
                log.info("주문 취소 완료 (취소 승인): orderId={}, cancelId={}",
                        event.getOrderId(), event.getCancelId());
            }
        });

        markProcessed(event.getEventId(), "cancel-approved");
    }

    private boolean isDuplicate(String eventId, String eventType) {
        if (eventId != null && processedEventRepository.existsByEventId(eventId)) {
            log.warn("중복 이벤트 무시: eventId={}, eventType={}", eventId, eventType);
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
