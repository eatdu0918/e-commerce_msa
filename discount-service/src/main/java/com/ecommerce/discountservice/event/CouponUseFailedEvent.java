package com.ecommerce.discountservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponUseFailedEvent {

    String eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    Long userCouponId;
    String reason;
}
