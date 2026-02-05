package com.ecommerce.discountservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponStatus {
    AVAILABLE("사용 가능"),
    USED("사용 완료"),
    EXPIRED("기간 만료");

    private final String description;
}
