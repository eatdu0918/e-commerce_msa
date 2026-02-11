package com.ecommerce.cancelservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundCompletedEvent {
    String eventId;
    Long refundId;
    String refundNumber;
    Long orderId;
    Long cancelId;
    Long paymentId;
    Long userId;
    BigDecimal amount;
}
