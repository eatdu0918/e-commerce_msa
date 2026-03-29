package com.ecommerce.orderservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponUsedEvent {

    String eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    Long userCouponId;
    Long couponId;
    BigDecimal discountAmount;
    /** 표시용 쿠폰 스냅샷(discount-service에서 설정) */
    String couponName;
    String couponCode;
    String couponType;
    BigDecimal couponRuleValue;
}
