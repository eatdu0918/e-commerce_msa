package com.ecommerce.orderservice.dto.response;

import com.ecommerce.orderservice.client.dto.PaymentInfo;
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
    String statusDescription;
    BigDecimal totalAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    Long userCouponId;
    String shippingAddress;
    String recipientName;
    String recipientPhone;
    List<OrderItemDetailResponse> items;
    PaymentInfo payment;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static OrderDetailResponse from(OrderResponse order, List<OrderItemDetailResponse> items, PaymentInfo payment) {
        return OrderDetailResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .statusDescription(order.getStatusDescription())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .userCouponId(order.getUserCouponId())
                .shippingAddress(order.getShippingAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .items(items)
                .payment(payment)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
