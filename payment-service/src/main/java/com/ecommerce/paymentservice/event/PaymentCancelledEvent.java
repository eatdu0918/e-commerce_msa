package com.ecommerce.paymentservice.event;

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
@JsonTypeName("PaymentCancelledEvent")
public class PaymentCancelledEvent {
    String eventId;
    Long paymentId;
    String paymentNumber;
    Long orderId;
    String orderNumber;
    Long userId;
    BigDecimal amount;
}
