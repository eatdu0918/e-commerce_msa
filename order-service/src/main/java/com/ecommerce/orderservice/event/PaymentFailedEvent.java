package com.ecommerce.orderservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentFailedEvent {
    String eventId;
    Long paymentId;
    String paymentNumber;
    Long orderId;
    String orderNumber;
    Long userId;
    BigDecimal amount;
    String reason;
}
