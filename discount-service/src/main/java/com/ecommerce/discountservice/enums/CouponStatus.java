package com.ecommerce.discountservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponStatus {
    AVAILABLE("?     ??),
    USED("?   ?  "),
    EXPIRED("       ");

    private final String description;
}
