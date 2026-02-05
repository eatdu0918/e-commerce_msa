package com.ecommerce.discountservice.kafka;

import com.ecommerce.discountservice.entity.UserCoupon;
import com.ecommerce.discountservice.event.*;
import com.ecommerce.discountservice.repository.UserCouponRepository;
import com.ecommerce.discountservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscountEventConsumer {

    private final CouponService couponService;
    private final UserCouponRepository userCouponRepository;
    private final DiscountEventProducer discountEventProducer;

    @KafkaListener(topics = "stock-decreased", groupId = "discount-service")
    public void handleStockDecreased(StockDecreasedEvent event) {
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
            discountEventProducer.sendCouponUsedEvent(couponUsedEvent);
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

            discountEventProducer.sendCouponUsedEvent(couponUsedEvent);
            log.info("Coupon used successfully: orderId={}, discountAmount={}",
                    event.getOrderId(), discountAmount);

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

            discountEventProducer.sendCouponUseFailedEvent(failedEvent);
        }
    }

    @KafkaListener(topics = "order-cancelled", groupId = "discount-service")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received order-cancelled event: orderId={}, userCouponId={}",
                event.getOrderId(), event.getUserCouponId());

        if (event.getUserCouponId() == null) {
            log.info("No coupon to restore for order: orderId={}", event.getOrderId());
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

            discountEventProducer.sendCouponRestoredEvent(restoredEvent);
            log.info("Coupon restored successfully: orderId={}", event.getOrderId());

        } catch (Exception e) {
            log.error("Failed to restore coupon: orderId={}, error={}",
                    event.getOrderId(), e.getMessage());
        }
    }
}
