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

    /**
     * cancel-service 취소·반품 신청(cancel-requested) 시 주문을 취소 요청 중으로 둘 수 있는 상태.
     * ({@link #canCancel()}는 출고 전 즉시 취소 API 등에만 사용)
     */
    public boolean canMarkCancelRequested() {
        return this != CANCELLED && this != CANCEL_REQUESTED;
    }

    public boolean canUpdateStatus() {
        return this != CANCELLED && this != DELIVERED && this != CANCEL_REQUESTED;
    }
}
