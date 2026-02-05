package com.ecommerce.cancelservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelStatus {
    REQUESTED("취소 요청"),
    APPROVED("취소 승인"),
    REJECTED("취소 거부"),
    COMPLETED("취소 완료");

    private final String description;
}
