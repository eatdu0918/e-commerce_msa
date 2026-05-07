package com.ecommerce.discountservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponType {
    PERCENTAGE("?    ?   "),
    FIXED_AMOUNT("?    ?   ");

    private final String description;
}
