package com.ecommerce.cancelservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelApprovedEvent {
    String eventId;
    Long cancelId;
    String cancelNumber;
    Long orderId;
    String orderNumber;
    Long userId;
    List<CancelItemEvent> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CancelItemEvent {
        Long productId;
        String productName;
        Integer quantity;
        BigDecimal unitPrice;
    }
}
