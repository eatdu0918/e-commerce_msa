package com.ecommerce.discountservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreatedEvent {

    String eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    BigDecimal totalAmount;
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
        String productName;
        Integer quantity;
        BigDecimal unitPrice;
    }
}
