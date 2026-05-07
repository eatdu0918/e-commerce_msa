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
     * ???  ???       ??UI   ?   ??(??   ?null).
     */
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
    List<OrderItemResponse> items;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    /**    ????  ??   ??   ??(    ?????    ????   ? ? ?). ??  ??   ?????null */
    String paymentStatus;

    /**
     *    ????  ???    ?????   ?  ??    ?PG    ?).    ??   ?  ?  ?    ??      ?? ?  ? ? ?.
     *      DB??finalAmount  ????   ????   ?    ?    ??????  .
     */
    BigDecimal paymentAmount;

    /**       ?           ???  ???  ??????(   ???       ????. */
    OrderStatus progressStatus;

    /**
     * cancel-service     ? ??  ???    ?   (REQUESTED/APPROVED/COMPLETED).    ??   ????   ? ? ?.
     */
    String activeCancelStatus;

    /**    ??   ????    ? ??  ?? ?ID. ?  ?    ?   ? ?  ?  ?     ???   ?? */
    Long activeCancelId;

    /**
     *     ? ??  ??   ???    ?   ({@code ORDER_CANCEL} | {@code RETURN_REFUND}).    ??   ????   ? ? ?.
     */
    String activeCancelRequestType;

    /**     ?    ??     ?    ?  ?    ????  ???   ?    ???(??  ??    ??       ??????? */
    boolean skipConfirmAndPreparing;
    /**     ?    ??   ??      ????  ???   ?    ??? */
    boolean skipShippingAndDelivered;

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
                .appliedCouponName(order.getAppliedCouponName())
                .appliedCouponCode(order.getAppliedCouponCode())
                .appliedCouponType(order.getAppliedCouponType())
                .appliedCouponRuleValue(order.getAppliedCouponRuleValue())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .progressStatus(order.getStatus())
                .skipConfirmAndPreparing(order.isSkipConfirmAndPreparing())
                .skipShippingAndDelivered(order.isSkipShippingAndDelivered())
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
                .appliedCouponName(order.getAppliedCouponName())
                .appliedCouponCode(order.getAppliedCouponCode())
                .appliedCouponType(order.getAppliedCouponType())
                .appliedCouponRuleValue(order.getAppliedCouponRuleValue())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .progressStatus(order.getStatus())
                .skipConfirmAndPreparing(order.isSkipConfirmAndPreparing())
                .skipShippingAndDelivered(order.isSkipShippingAndDelivered())
                .build();
    }
}
