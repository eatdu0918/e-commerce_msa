package com.ecommerce.productservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockDecreaseFailedEvent {

    String eventId;
    Long orderId;
    String orderNumber;
    Long userId;
    Long productId;
    String reason;
}
