package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.enums.OrderStatus;

/**
 *      DB ?   ??    ???    ?????  ? ??  ,       ?    ??  ??  ????  ??   ?    ??  ???   .
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
     * cancel-service    ?     ? ??  ???      DB??   ????  ??  .
     * ?  ?  ?    ??     ??  ??Kafka    ???   ???      ?  ? ?  ?  ???    ?   ?  ???  .
     */
    public static OrderStatus resolveForDisplayWithActiveCancel(
            OrderStatus orderStatus,
            String paymentStatus,
            String activeCancelStatus,
            boolean skipConfirmAndPreparing,
            boolean skipShippingAndDelivered) {
        /*
         *    ?  ? ?  ?  ??  ?? ??         ?  ?? ? ?    CANCEL_REQUESTED??   ??Kafka    ????.
         * ??????      ?  ? ?     ?   ?       ?   ?       ? ??  ?   ?   ???
         */
        if (orderStatus == OrderStatus.CANCEL_REQUESTED && isPaymentCancelledOrRefunded(paymentStatus)) {
            return OrderStatus.CANCELLED;
        }
        OrderStatus progress = resolveForDisplay(orderStatus, paymentStatus);
        if (activeCancelStatus == null || orderStatus == OrderStatus.CANCELLED) {
            return applyCheckoutSkipToProgress(progress, paymentStatus, skipConfirmAndPreparing, skipShippingAndDelivered);
        }
        if ("REQUESTED".equals(activeCancelStatus)) {
            return OrderStatus.CANCEL_REQUESTED;
        }
        if ("APPROVED".equals(activeCancelStatus) || "COMPLETED".equals(activeCancelStatus)) {
            return OrderStatus.CANCELLED;
        }
        return applyCheckoutSkipToProgress(progress, paymentStatus, skipConfirmAndPreparing, skipShippingAndDelivered);
    }

    /**
     *     ?   ? ?  ??  ???  ???   ??  ?   ?  ? ?   ??   ?? ???  B    ???   ??      ?      ??    ??   ?   ??   ???
     */
    private static OrderStatus applyCheckoutSkipToProgress(
            OrderStatus progress,
            String paymentStatus,
            boolean skipConfirmAndPreparing,
            boolean skipShippingAndDelivered) {
        if (!isPaymentCompleted(paymentStatus)) {
            return progress;
        }
        if (progress == OrderStatus.CANCELLED || progress == OrderStatus.CANCEL_REQUESTED) {
            return progress;
        }
        if (skipShippingAndDelivered && skipConfirmAndPreparing) {
            if (progress == OrderStatus.PENDING
                    || progress == OrderStatus.CONFIRMED
                    || progress == OrderStatus.PREPARING
                    || progress == OrderStatus.SHIPPING) {
                return OrderStatus.DELIVERED;
            }
        }
        if (skipConfirmAndPreparing) {
            if (progress == OrderStatus.PENDING
                    || progress == OrderStatus.CONFIRMED
                    || progress == OrderStatus.PREPARING) {
                return OrderStatus.SHIPPING;
            }
        }
        return progress;
    }

    private static boolean isPaymentCompleted(String paymentStatus) {
        return paymentStatus != null && "COMPLETED".equalsIgnoreCase(paymentStatus.trim());
    }

    private static boolean isPaymentCancelledOrRefunded(String paymentStatus) {
        if (paymentStatus == null) {
            return false;
        }
        String s = paymentStatus.trim().toUpperCase();
        return "CANCELLED".equals(s) || "REFUNDED".equals(s);
    }

    /**
     * ?  ?       ?     ????  ?    ??  ?:     ? ??  ???   /?  ???   ) ? ?     ???  ?  ??   ?   .
     * cancel-service??active ?   ?? REJECTED??   ???? ??  .
     */
    public static boolean blocksAdminFulfillmentAdvance(String paymentStatus, String activeCancelStatus) {
        if (activeCancelStatus != null && !activeCancelStatus.isBlank()) {
            return true;
        }
        return isPaymentCancelledOrRefunded(paymentStatus);
    }
}
