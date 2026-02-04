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
}
