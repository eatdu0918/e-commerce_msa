package com.ecommerce.discountservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponRestoredEvent {

    String eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    Long userCouponId;
    Long couponId;
}
