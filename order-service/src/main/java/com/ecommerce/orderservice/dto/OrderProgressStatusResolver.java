package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.enums.OrderStatus;

/**
 * 주문 DB 상태와 결제 완료 여부를 바탕으로, 목록·상세·스테퍼에서 동일하게 쓰는 표시용 상태.
 */
public final class OrderProgressStatusResolver {

    private OrderProgressStatusResolver() {
    }

    public static OrderStatus resolveForDisplay(OrderStatus orderStatus, String paymentStatus) {
        if (orderStatus == OrderStatus.PENDING && isPaymentCompleted(paymentStatus)) {
            return OrderStatus.CONFIRMED;
        }
        return orderStatus;
    }

    private static boolean isPaymentCompleted(String paymentStatus) {
        return paymentStatus != null && "COMPLETED".equalsIgnoreCase(paymentStatus.trim());
    }
}
