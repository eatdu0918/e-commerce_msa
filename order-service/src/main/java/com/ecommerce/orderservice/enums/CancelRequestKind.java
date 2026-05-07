package com.ecommerce.orderservice.enums;

/**
 * cancel-service Kafka ??  ?    ?  ???   ????  ??(ORDER_CANCEL / RETURN_REFUND).
 */
public enum CancelRequestKind {
    ORDER_CANCEL,
    RETURN_REFUND;

    public static CancelRequestKind fromEventPayload(String raw) {
        if (raw == null || raw.isBlank()) {
            return ORDER_CANCEL;
        }
        try {
            return CancelRequestKind.valueOf(raw.trim());
        } catch (IllegalArgumentException ex) {
            return ORDER_CANCEL;
        }
    }
}
