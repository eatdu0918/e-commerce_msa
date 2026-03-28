package com.ecommerce.refundservice.event;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonTypeName("RefundFailedEvent")
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
