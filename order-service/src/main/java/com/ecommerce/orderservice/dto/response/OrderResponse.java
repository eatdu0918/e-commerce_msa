package com.ecommerce.orderservice.dto.response;

import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {

    Long id;
    Long userId;
    String orderNumber;
    OrderStatus status;
    /**
     * {@link com.ecommerce.orderservice.entity.Order#getStatusBeforeCancelRequest()}
     * — 취소 요청 처리 UI·검증용 (없으면 null).
     */
    OrderStatus statusBeforeCancelRequest;
    String statusDescription;
    BigDecimal totalAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    Long userCouponId;
    String shippingAddress;
    String recipientName;
    String recipientPhone;
    List<OrderItemResponse> items;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    /** 결제 서비스 조회 결과 (목록 통합 조회 시에만 채움). 없거나 미조회 시 null */
    String paymentStatus;

    /** 목록·상세·진행 바 라벨에 공통 사용 (결제 완료 반영 등). */
    OrderStatus progressStatus;

    /**
     * cancel-service 진행 중 취소 요약 상태(REQUESTED/APPROVED/COMPLETED). 집계 조회 시에만 채움.
     */
    String activeCancelStatus;

    /** 집계 조회 시 진행 중 취소 건 ID. 관리자 화면에서 승인·거절 연동용. */
    Long activeCancelId;

    /**
     * 진행 중 취소 건의 요청 유형({@code ORDER_CANCEL} | {@code RETURN_REFUND}). 집계 조회 시에만 채움.
     */
    String activeCancelRequestType;

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .statusBeforeCancelRequest(order.getStatusBeforeCancelRequest())
                .statusDescription(order.getStatus().getDescription())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .userCouponId(order.getUserCouponId())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .progressStatus(order.getStatus())
                .build();
    }

    public static OrderResponse fromWithoutItems(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .statusBeforeCancelRequest(order.getStatusBeforeCancelRequest())
                .statusDescription(order.getStatus().getDescription())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .userCouponId(order.getUserCouponId())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .progressStatus(order.getStatus())
                .build();
    }
}
