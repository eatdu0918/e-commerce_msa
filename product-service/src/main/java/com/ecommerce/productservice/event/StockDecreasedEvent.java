package com.ecommerce.productservice.event;

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
public class StockDecreasedEvent {

    String eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    BigDecimal totalAmount;
    Long userCouponId;
    List<StockItemEvent> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class StockItemEvent {
        Long productId;
        Integer quantity;
        Integer remainingStock;
    }
}
