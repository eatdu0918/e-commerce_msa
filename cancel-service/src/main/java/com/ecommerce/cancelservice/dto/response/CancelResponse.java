package com.ecommerce.cancelservice.dto.response;

import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.enums.CancelReason;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import com.ecommerce.cancelservice.enums.CancelStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelResponse {

    Long id;
    Long orderId;
    String orderNumber;
    Long userId;
    String cancelNumber;
    CancelStatus status;
    String statusDescription;
    CancelRequestType requestType;
    String requestTypeDescription;
    CancelReason cancelReason;
    String cancelReasonDescription;
    String cancelDetail;
    String rejectedReason;
    LocalDateTime approvedAt;
    LocalDateTime rejectedAt;
    LocalDateTime completedAt;
    List<CancelItemResponse> items;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static CancelResponse from(Cancel cancel) {
        return from(cancel, null);
    }

    /**
     * {@code itemsOverride}    ??   ? ?    ???   ? ??   ??  ?????  ??   ? ?    ??  .
     */
    public static CancelResponse from(Cancel cancel, List<CancelItemResponse> itemsOverride) {
        List<CancelItemResponse> items = itemsOverride != null
                ? itemsOverride
                : (cancel.getCancelItems() != null
                        ? cancel.getCancelItems().stream().map(CancelItemResponse::from).toList()
                        : null);

        return CancelResponse.builder()
                .id(cancel.getId())
                .orderId(cancel.getOrderId())
                .orderNumber(cancel.getOrderNumber())
                .userId(cancel.getUserId())
                .cancelNumber(cancel.getCancelNumber())
                .status(cancel.getStatus())
                .statusDescription(cancel.getStatus().getDescription())
                .requestType(cancel.getRequestType())
                .requestTypeDescription(cancel.getRequestType().getDescription())
                .cancelReason(cancel.getCancelReason())
                .cancelReasonDescription(cancel.getCancelReason().getDescription())
                .cancelDetail(cancel.getCancelDetail())
                .rejectedReason(cancel.getRejectedReason())
                .approvedAt(cancel.getApprovedAt())
                .rejectedAt(cancel.getRejectedAt())
                .completedAt(cancel.getCompletedAt())
                .items(items)
                .createdAt(cancel.getCreatedAt())
                .updatedAt(cancel.getUpdatedAt())
                .build();
    }

    public static CancelResponse fromWithoutItems(Cancel cancel) {
        return CancelResponse.builder()
                .id(cancel.getId())
                .orderId(cancel.getOrderId())
                .orderNumber(cancel.getOrderNumber())
                .userId(cancel.getUserId())
                .cancelNumber(cancel.getCancelNumber())
                .status(cancel.getStatus())
                .statusDescription(cancel.getStatus().getDescription())
                .requestType(cancel.getRequestType())
                .requestTypeDescription(cancel.getRequestType().getDescription())
                .cancelReason(cancel.getCancelReason())
                .cancelReasonDescription(cancel.getCancelReason().getDescription())
                .cancelDetail(cancel.getCancelDetail())
                .rejectedReason(cancel.getRejectedReason())
                .approvedAt(cancel.getApprovedAt())
                .rejectedAt(cancel.getRejectedAt())
                .completedAt(cancel.getCompletedAt())
                .createdAt(cancel.getCreatedAt())
                .updatedAt(cancel.getUpdatedAt())
                .build();
    }
}
