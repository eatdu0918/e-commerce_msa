package com.ecommerce.orderservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("결제 대기"),
    CONFIRMED("주문 확인"),
    PREPARING("배송 준비 중"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    /** 취소 요청 시 중간 상태 */
    CANCEL_REQUESTED("취소 요청 중"),
    CANCELLED("주문 취소");

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
