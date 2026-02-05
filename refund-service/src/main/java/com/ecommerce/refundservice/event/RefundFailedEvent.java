package com.ecommerce.refundservice.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundFailedEvent {
    String eventId;
    Long refundId;
    String refundNumber;
    Long orderId;
    Long cancelId;
    Long paymentId;
    Long userId;
    BigDecimal amount;
    String reason;
}
