package com.ecommerce.discountservice.kafka;

import com.ecommerce.discountservice.config.KafkaConfig;
import com.ecommerce.discountservice.event.CouponRestoredEvent;
import com.ecommerce.discountservice.event.CouponUseFailedEvent;
import com.ecommerce.discountservice.event.CouponUsedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscountEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCouponUsedEvent(CouponUsedEvent event) {
        log.info("Publishing coupon-used event: orderId={}, userCouponId={}",
                event.getOrderId(), event.getUserCouponId());
        kafkaTemplate.send(KafkaConfig.TOPIC_COUPON_USED, event.getOrderNumber(), event);
    }

    public void sendCouponUseFailedEvent(CouponUseFailedEvent event) {
        log.info("Publishing coupon-use-failed event: orderId={}, reason={}",
                event.getOrderId(), event.getReason());
        kafkaTemplate.send(KafkaConfig.TOPIC_COUPON_USE_FAILED, event.getOrderNumber(), event);
    }

    public void sendCouponRestoredEvent(CouponRestoredEvent event) {
        log.info("Publishing coupon-restored event: orderId={}, userCouponId={}",
                event.getOrderId(), event.getUserCouponId());
        kafkaTemplate.send(KafkaConfig.TOPIC_COUPON_RESTORED, event.getOrderNumber(), event);
    }
}
