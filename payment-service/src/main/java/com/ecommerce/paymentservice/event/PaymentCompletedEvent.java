package com.ecommerce.paymentservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentCompletedEvent {
    String eventId;
    Long paymentId;
    String paymentNumber;
    Long orderId;
    String orderNumber;
    Long userId;
    BigDecimal amount;
    String paymentMethod;
}
