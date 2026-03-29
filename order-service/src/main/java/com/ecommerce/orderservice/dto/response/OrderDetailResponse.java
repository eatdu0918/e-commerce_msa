package com.ecommerce.orderservice.dto.response;

import com.ecommerce.orderservice.client.dto.PaymentInfo;
import com.ecommerce.orderservice.dto.OrderProgressStatusResolver;
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
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {

    Long id;
    Long userId;
    String orderNumber;
    OrderStatus status;
    OrderStatus statusBeforeCancelRequest;
    String statusDescription;
    BigDecimal totalAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    Long userCouponId;
    String appliedCouponName;
    String appliedCouponCode;
    String appliedCouponType;
    BigDecimal appliedCouponRuleValue;
    String shippingAddress;
    String recipientName;
    String recipientPhone;
    List<OrderItemDetailResponse> items;
    PaymentInfo payment;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    /** {@link OrderResponse#getProgressStatus()} 와 동일 규칙 */
    OrderStatus progressStatus;

    /**
     * cancel-service 기준 진행 중 취소 상태(REQUESTED/APPROVED/COMPLETED). 없으면 null.
     * 주문 DB 갱신(Kafka) 지연 시에도 취소 요청 버튼 숨김에 사용.
     */
    String activeCancelStatus;

    /** 진행 중 취소·반품 요청 건 ID(관리자 승인·거절 UI 연동). 없으면 null. */
    Long activeCancelId;

    /** 진행 중인 취소·반품 건의 요청 유형({@code ORDER_CANCEL} | {@code RETURN_REFUND}). 없으면 null. */
    String activeCancelRequestType;

    boolean skipConfirmAndPreparing;
    boolean skipShippingAndDelivered;

    public static OrderDetailResponse from(
            OrderResponse order,
            List<OrderItemDetailResponse> items,
            PaymentInfo payment,
            String activeCancelStatus,
            Long activeCancelId,
            String activeCancelRequestType) {
        String pay = payment != null ? payment.getStatus() : null;
        OrderStatus progress = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                order.getStatus(),
                pay,
                activeCancelStatus,
                order.isSkipConfirmAndPreparing(),
                order.isSkipShippingAndDelivered());
        return OrderDetailResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .statusBeforeCancelRequest(order.getStatusBeforeCancelRequest())
                .statusDescription(order.getStatusDescription())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .userCouponId(order.getUserCouponId())
                .appliedCouponName(order.getAppliedCouponName())
                .appliedCouponCode(order.getAppliedCouponCode())
                .appliedCouponType(order.getAppliedCouponType())
                .appliedCouponRuleValue(order.getAppliedCouponRuleValue())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .items(items)
                .payment(payment)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .progressStatus(progress)
                .activeCancelStatus(activeCancelStatus)
                .activeCancelId(activeCancelId)
                .activeCancelRequestType(activeCancelRequestType)
                .skipConfirmAndPreparing(order.isSkipConfirmAndPreparing())
                .skipShippingAndDelivered(order.isSkipShippingAndDelivered())
                .build();
    }
}
