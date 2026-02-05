package com.ecommerce.paymentservice.dto.response;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {

    Long id;
    Long orderId;
    String orderNumber;
    Long userId;
    String paymentNumber;
    PaymentStatus status;
    String statusDescription;
    PaymentMethod paymentMethod;
    String paymentMethodDescription;
    BigDecimal amount;
    LocalDateTime paidAt;
    LocalDateTime failedAt;
    String failureReason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .paymentNumber(payment.getPaymentNumber())
                .status(payment.getStatus())
                .statusDescription(payment.getStatus().getDescription())
                .paymentMethod(payment.getPaymentMethod())
                .paymentMethodDescription(payment.getPaymentMethod().getDescription())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .failedAt(payment.getFailedAt())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
