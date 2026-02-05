package com.ecommerce.productservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCancelledEvent {

    String eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    Long userCouponId;
    List<OrderItemEvent> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderItemEvent {
        Long productId;
        Integer quantity;
    }
}
