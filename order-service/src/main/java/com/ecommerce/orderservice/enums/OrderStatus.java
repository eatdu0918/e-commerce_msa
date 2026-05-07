package com.ecommerce.orderservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("    ?  ?),
    CONFIRMED("    ?  "),
    PREPARING("?        "),
    SHIPPING("    ?),
    DELIVERED("    ?  "),
    /**         ?   ??      ?   ?  ?*/
    CANCEL_REQUESTED("    ?    ?),
    CANCELLED("       ");

    private final String description;

    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED || this == PREPARING;
    }

    /**
     * cancel-service    /    ?  (cancel-requested) ???   ?         ??    ?       ?  ? ???    .
     *      ?SHIPPING)?       ?  ?  ??      ?.    /?  ?      ?   ?   ?
     */
    public boolean allowsInboundCancelRequest(CancelRequestKind kind) {
        if (this == CANCELLED || this == CANCEL_REQUESTED) {
            return false;
        }
        if (this == SHIPPING) {
            return false;
        }
        if (kind == CancelRequestKind.RETURN_REFUND) {
            return this == DELIVERED;
        }
        return this == PENDING || this == CONFIRMED || this == PREPARING;
    }

    public boolean canUpdateStatus() {
        return this != CANCELLED && this != DELIVERED && this != CANCEL_REQUESTED;
    }
}
