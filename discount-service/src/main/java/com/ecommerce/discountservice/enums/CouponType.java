package com.ecommerce.discountservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponType {
    PERCENTAGE("정률 할인"),
    FIXED_AMOUNT("정액 할인");

    private final String description;
}
