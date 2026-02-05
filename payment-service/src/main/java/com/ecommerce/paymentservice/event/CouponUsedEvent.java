package com.ecommerce.paymentservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponUsedEvent {
    String eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    Long userCouponId;
    Long couponId;
    BigDecimal discountAmount;
}
