package com.ecommerce.orderservice.kafka;

import com.ecommerce.orderservice.entity.ProcessedEvent;
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
    /**
     * PG/REST 등으로 결제가 먼저 완료된 뒤 Saga(coupon-used)보다 빨리 반영될 때
     * 주문 상태를 주문 상세 UI와 일치시키기 위한 보정 처리.
     */
    @KafkaListener(topics = "payment-completed", groupId = "order-service")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        if (isDuplicate(event.getEventId(), "payment-completed"))
            return;

        log.info("Received payment-completed event: orderId={}, paymentId={}",
                event.getOrderId(), event.getPaymentId());

        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.confirm();
                log.info("주문 확정 (결제 완료 이벤트): orderId={}", order.getId());
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
                log.info("주문 취소 완료 (결제 실패): orderId={}", event.getOrderId());
            }
        });

        markProcessed(event.getEventId(), "payment-failed");
    }

    /**
     * 고객 취소 신청 접수 — 주문 상태를 취소 요청 중으로 표시 (상세·재신청 방지와 동기화)
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
                log.warn("cancel-requested 사용자 불일치로 무시: orderId={}, orderUserId={}, eventUserId={}",
                        event.getOrderId(), order.getUserId(), event.getUserId());
            } else {
                order.markCancelRequested();
                log.info("주문 취소 요청 상태 반영: orderId={}, status={}", order.getId(), order.getStatus());
            }
        });

        markProcessed(event.getEventId(), "cancel-requested");
    }

    /**
     * 취소 거부 시 취소 요청 표시 해제 후 주문 정상 처리 재개
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
                log.warn("cancel-rejected 사용자 불일치로 무시: orderId={}, orderUserId={}, eventUserId={}",
                        event.getOrderId(), order.getUserId(), event.getUserId());
            } else {
                order.restoreAfterCancelRejected();
                log.info("취소 거부 후 주문 상태 복귀: orderId={}, status={}", order.getId(), order.getStatus());
            }
        });

        markProcessed(event.getEventId(), "cancel-rejected");
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
