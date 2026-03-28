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

    /**
     * cancel-service 기준 진행 중 취소와 주문 DB를 맞춰 표시한다.
     * 승인·완료 후 주문 서비스 Kafka 반영 전에도 상세·목록에서 취소 완료로 보이게 한다.
     */
    public static OrderStatus resolveForDisplayWithActiveCancel(
            OrderStatus orderStatus, String paymentStatus, String activeCancelStatus) {
        OrderStatus progress = resolveForDisplay(orderStatus, paymentStatus);
        if (activeCancelStatus == null || orderStatus == OrderStatus.CANCELLED) {
            return progress;
        }
        if ("REQUESTED".equals(activeCancelStatus)) {
            return OrderStatus.CANCEL_REQUESTED;
        }
        if ("APPROVED".equals(activeCancelStatus) || "COMPLETED".equals(activeCancelStatus)) {
            return OrderStatus.CANCELLED;
        }
        return progress;
    }

    private static boolean isPaymentCompleted(String paymentStatus) {
        return paymentStatus != null && "COMPLETED".equalsIgnoreCase(paymentStatus.trim());
    }
}
