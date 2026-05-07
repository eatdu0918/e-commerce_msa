package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.entity.ProcessedEvent;
import com.ecommerce.orderservice.enums.CancelRequestKind;
import com.ecommerce.orderservice.enums.OrderStatus;
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
        // ????    ??    - discount-service? ?  ?       ??
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

                // ?????product-service ?? ?? ?  ?? ?     ??
                // ?   ?   ?  ?     ? ?  ????      ?order-cancelled ??  ??    ?
                OrderCancelledEvent cancelledEvent = OrderCancelledEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .userId(order.getUserId())
                        .userCouponId(order.getUserCouponId())
                        .items(Collections.emptyList())
                        .build();

                orderEventProducer.sendOrderCancelledEvent(cancelledEvent);
                log.info("     ?  ???    (?????   ?: orderId={}", event.getOrderId());
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
                order.applyDiscount(
                        event.getDiscountAmount(),
                        event.getCouponName(),
                        event.getCouponCode(),
                        event.getCouponType(),
                        event.getCouponRuleValue());
            }
            order.markPaidAndApplyFulfillmentFastForward();
            log.info("     ?          ??  ?   ?? orderId={}, status={}, finalAmount={}",
                    order.getId(), order.getStatus(), order.getFinalAmount());
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
                log.info("     ?  ???    (?   ??    ??  ): orderId={}", event.getOrderId());
            }
        });

        markProcessed(event.getEventId(), "coupon-use-failed");
    }

    /**
     *    ????   ??    ??   ????      ?  ??+ order-cancelled ??  ??    ?
     * ??product-service: ????   ?? discount-service: ?   ?   ??
     */
    /**
     * PG/REST ?    ?   ?  ? ?  ? ?   ????Saga(coupon-used)  ????? ??   ?????
     *      ?   ??     ?    UI?? ??  ??   ??       ??   ??
     */
    @KafkaListener(topics = "payment-completed", groupId = "order-service")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        if (isDuplicate(event.getEventId(), "payment-completed"))
            return;

        log.info("Received payment-completed event: orderId={}, paymentId={}",
                event.getOrderId(), event.getPaymentId());

        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            OrderStatus before = order.getStatus();
            order.reconcileDiscountFromPaidAmountIfUnset(event.getAmount());
            order.markPaidAndApplyFulfillmentFastForward();
            if (before != order.getStatus()) {
                log.info("   ???    ??     ?       ?? orderId={}, before={}, after={}",
                        order.getId(), before, order.getStatus());
            }
        });

        markProcessed(event.getEventId(), "payment-completed");
    }

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
                log.info("     ?  ???    (   ????  ): orderId={}", event.getOrderId());
            }
        });

        markProcessed(event.getEventId(), "payment-failed");
    }

    /**
     * ?   ??  ???    ?    ??     ?   ???  ???       ?  ???   (?    ???  ?   ??? ??  ??
     */
    @KafkaListener(topics = "cancel-requested", groupId = "order-service")
    @Transactional
    public void handleCancelRequested(CancelRequestedEvent event) {
        if (isDuplicate(event.getEventId(), "cancel-requested"))
            return;

        log.info("Received cancel-requested event: cancelId={}, orderId={}",
                event.getCancelId(), event.getOrderId());

        orderRepository.findByIdWithItems(event.getOrderId()).ifPresent(order -> {
            if (event.getUserId() != null && !event.getUserId().equals(order.getUserId())) {
                log.warn("cancel-requested ??????  ?  ?   ?  ?? orderId={}, orderUserId={}, eventUserId={}",
                        event.getOrderId(), order.getUserId(), event.getUserId());
            } else {
                CancelRequestKind kind = CancelRequestKind.fromEventPayload(event.getRequestType());
                order.markCancelRequested(kind);
                log.info("     ?  ???    ?       ?? orderId={}, status={}", order.getId(), order.getStatus());
            }
        });

        markProcessed(event.getEventId(), "cancel-requested");
    }

    /**
     * ?  ??   ? ???  ???    ??   ??   ??     ?       ???? ?
     */
    @KafkaListener(topics = "cancel-rejected", groupId = "order-service")
    @Transactional
    public void handleCancelRejected(CancelRejectedEvent event) {
        if (isDuplicate(event.getEventId(), "cancel-rejected"))
            return;

        log.info("Received cancel-rejected event: cancelId={}, orderId={}",
                event.getCancelId(), event.getOrderId());

        orderRepository.findByIdWithItems(event.getOrderId()).ifPresent(order -> {
            if (event.getUserId() != null && !event.getUserId().equals(order.getUserId())) {
                log.warn("cancel-rejected ??????  ?  ?   ?  ?? orderId={}, orderUserId={}, eventUserId={}",
                        event.getOrderId(), order.getUserId(), event.getUserId());
            } else {
                order.restoreAfterCancelRejected();
                log.info("?  ??   ? ??     ?       ?: orderId={}, status={}", order.getId(), order.getStatus());
            }
        });

        markProcessed(event.getEventId(), "cancel-rejected");
    }

    /**
     * ?  ???  ????    ??   ????      ?  ??+ order-cancelled ??  ??    ?
     * ??product-service: ????   ?? discount-service: ?   ?   ?? payment-service:    ???  ??
     */
    @KafkaListener(topics = "cancel-approved", groupId = "order-service")
    @Transactional
    public void handleCancelApproved(CancelApprovedEvent event) {
        if (isDuplicate(event.getEventId(), "cancel-approved"))
            return;

        log.info("Received cancel-approved event: cancelId={}, orderId={}",
                event.getCancelId(), event.getOrderId());

        orderRepository.findByIdWithItems(event.getOrderId()).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.CANCEL_REQUESTED || order.canCancel()) {
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
                log.info("     ?  ???    (?  ???  ??: orderId={}, cancelId={}",
                        event.getOrderId(), event.getCancelId());
            }
        });

        markProcessed(event.getEventId(), "cancel-approved");
    }

    private boolean isDuplicate(String eventId, String eventType) {
        if (eventId != null && processedEventRepository.existsByEventId(eventId)) {
            log.warn("   ????  ???  ?? eventId={}, eventType={}", eventId, eventType);
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
