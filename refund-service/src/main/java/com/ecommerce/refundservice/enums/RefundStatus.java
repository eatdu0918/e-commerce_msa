package com.ecommerce.refundservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundStatus {
    PENDING("환불 대기"),
    PROCESSING("환불 처리 중"),
    COMPLETED("환불 완료"),
    FAILED("환불 실패");

    private final String description;
}
