package com.ecommerce.orderservice.client.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentInfo {

    Long id;
    Long orderId;
    String paymentNumber;
    String status;
    BigDecimal amount;
    String paymentMethod;
    LocalDateTime paidAt;
}
