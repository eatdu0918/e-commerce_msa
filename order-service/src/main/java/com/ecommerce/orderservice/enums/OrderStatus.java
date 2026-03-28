package com.ecommerce.orderservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("주문 대기"),
    CONFIRMED("주문 확정"),
    PREPARING("상품 준비중"),
    SHIPPING("배송중"),
    DELIVERED("배송 완료"),
    /** 고객 취소 신청 후 관리자 승인 대기 */
    CANCEL_REQUESTED("취소 요청 중"),
    CANCELLED("주문 취소");

    private final String description;

    public boolean canCancel() {
        return this == PENDING || this == CONFIRMED || this == PREPARING;
    }

    public boolean canUpdateStatus() {
        return this != CANCELLED && this != DELIVERED && this != CANCEL_REQUESTED;
    }
}
