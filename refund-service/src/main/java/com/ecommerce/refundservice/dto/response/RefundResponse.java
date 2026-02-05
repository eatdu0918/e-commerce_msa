package com.ecommerce.refundservice.dto.response;

import com.ecommerce.refundservice.entity.Refund;
import com.ecommerce.refundservice.enums.RefundReason;
import com.ecommerce.refundservice.enums.RefundStatus;
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
public class RefundResponse {

    Long id;
    Long orderId;
    Long cancelId;
    Long paymentId;
    Long userId;
    String refundNumber;
    RefundStatus status;
    String statusDescription;
    RefundReason refundReason;
    String refundReasonDescription;
    String refundDetail;
    BigDecimal amount;
    LocalDateTime completedAt;
    LocalDateTime failedAt;
    String failureReason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static RefundResponse from(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getOrderId())
                .cancelId(refund.getCancelId())
                .paymentId(refund.getPaymentId())
                .userId(refund.getUserId())
                .refundNumber(refund.getRefundNumber())
                .status(refund.getStatus())
                .statusDescription(refund.getStatus().getDescription())
                .refundReason(refund.getRefundReason())
                .refundReasonDescription(refund.getRefundReason().getDescription())
                .refundDetail(refund.getRefundDetail())
                .amount(refund.getAmount())
                .completedAt(refund.getCompletedAt())
                .failedAt(refund.getFailedAt())
                .failureReason(refund.getFailureReason())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }
}
