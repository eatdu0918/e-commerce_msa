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

    /** {@link OrderResponse#getProgressStatus()} ?? ??      ??*/
    OrderStatus progressStatus;

    /**
     * cancel-service    ?     ? ??  ???   (REQUESTED/APPROVED/COMPLETED). ??   ?null.
     *      DB    ??Kafka)    ????  ???  ???       ???????????
     */
    String activeCancelStatus;

    /**     ? ??  ?      ??     ?ID(?  ?    ?  ?     ??UI ?   ). ??   ?null. */
    Long activeCancelId;

    /**     ?   ???  ?      ?   ???    ?   ({@code ORDER_CANCEL} | {@code RETURN_REFUND}). ??   ?null. */
    String activeCancelRequestType;

    /**
     * ?  ????     ?  ??ORDER_CANCEL)       ????? ?????   ?true. ??   ?    ???  ?   ???? ??   ??
     *      ?  ??   ???   ??      ??  (RETURN_REFUND) ?   ??    ??
     */
    @Builder.Default
    boolean hasRejectedOrderCancelRequest = false;

    /**       ??  (RETURN_REFUND)       ????? ?????   ?true. */
    @Builder.Default
    boolean hasRejectedReturnRefundRequest = false;

    boolean skipConfirmAndPreparing;
    boolean skipShippingAndDelivered;

    public static OrderDetailResponse from(
            OrderResponse order,
            List<OrderItemDetailResponse> items,
            PaymentInfo payment,
            String activeCancelStatus,
            Long activeCancelId,
            String activeCancelRequestType,
            boolean hasRejectedOrderCancelRequest,
            boolean hasRejectedReturnRefundRequest) {
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
                .hasRejectedOrderCancelRequest(hasRejectedOrderCancelRequest)
                .hasRejectedReturnRefundRequest(hasRejectedReturnRefundRequest)
                .skipConfirmAndPreparing(order.isSkipConfirmAndPreparing())
                .skipShippingAndDelivered(order.isSkipShippingAndDelivered())
                .build();
    }
}
