package com.ecommerce.discountservice.kafka;

import com.ecommerce.discountservice.entity.ProcessedEvent;
import com.ecommerce.discountservice.entity.UserCoupon;
import com.ecommerce.discountservice.event.*;
import com.ecommerce.discountservice.outbox.OutboxEventPublisher;
import com.ecommerce.discountservice.repository.ProcessedEventRepository;
import com.ecommerce.discountservice.repository.UserCouponRepository;
import com.ecommerce.discountservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscountEventConsumer {

    private final CouponService couponService;
    private final UserCouponRepository userCouponRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "stock-decreased", groupId = "discount-service")
    @Transactional
    public void handleStockDecreased(StockDecreasedEvent event) {
        if (isDuplicate(event.getEventId(), "stock-decreased")) return;

        log.info("Received stock-decreased event: orderId={}, userCouponId={}",
                event.getOrderId(), event.getUserCouponId());

        if (event.getUserCouponId() == null) {
            log.info("No coupon to use for order: orderId={}", event.getOrderId());
            CouponUsedEvent couponUsedEvent = CouponUsedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .userCouponId(null)
                    .couponId(null)
                    .discountAmount(null)
                    .build();
            outboxEventPublisher.publishCouponUsedEvent(couponUsedEvent);
            markProcessed(event.getEventId(), "stock-decreased");
            return;
        }

        try {
            UserCoupon userCoupon = userCouponRepository.findById(event.getUserCouponId())
                    .orElseThrow(() -> new RuntimeException("UserCoupon not found: " + event.getUserCouponId()));

            if (!userCoupon.isAvailable()) {
                throw new RuntimeException("Coupon is not available");
            }

            var discountAmount = userCoupon.getCoupon().calculateDiscount(event.getTotalAmount());

            couponService.useCoupon(event.getUserCouponId(), event.getOrderId());

            CouponUsedEvent couponUsedEvent = CouponUsedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .userCouponId(event.getUserCouponId())
                    .couponId(userCoupon.getCoupon().getId())
                    .discountAmount(discountAmount)
                    .build();

            outboxEventPublisher.publishCouponUsedEvent(couponUsedEvent);
            log.info("Coupon used successfully: orderId={}, discountAmount={}",
                    event.getOrderId(), discountAmount);

            markProcessed(event.getEventId(), "stock-decreased");

        } catch (Exception e) {
            log.error("Failed to use coupon: orderId={}, error={}",
                    event.getOrderId(), e.getMessage());

            CouponUseFailedEvent failedEvent = CouponUseFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .userCouponId(event.getUserCouponId())
                    .reason(e.getMessage())
                    .build();

            outboxEventPublisher.publishCouponUseFailedEvent(failedEvent);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "discount-service")
    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if (isDuplicate(event.getEventId(), "order-cancelled")) return;

        log.info("Received order-cancelled event: orderId={}, userCouponId={}",
                event.getOrderId(), event.getUserCouponId());

        if (event.getUserCouponId() == null) {
            log.info("No coupon to restore for order: orderId={}", event.getOrderId());
            markProcessed(event.getEventId(), "order-cancelled");
            return;
        }

        try {
            couponService.restoreCoupon(event.getOrderId());

            CouponRestoredEvent restoredEvent = CouponRestoredEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .userId(event.getUserId())
                    .userCouponId(event.getUserCouponId())
                    .build();

            outboxEventPublisher.publishCouponRestoredEvent(restoredEvent);
            log.info("Coupon restored successfully: orderId={}", event.getOrderId());

            markProcessed(event.getEventId(), "order-cancelled");

        } catch (Exception e) {
            log.error("Failed to restore coupon: orderId={}, error={}",
                    event.getOrderId(), e.getMessage());
        }
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
