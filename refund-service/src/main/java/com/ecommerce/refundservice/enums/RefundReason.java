package com.ecommerce.refundservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundReason {
    ORDER_CANCEL("     ?  ??),
    PRODUCT_DEFECT("?  ? ?  ??),
    WRONG_PRODUCT("??  ??),
    CHANGE_OF_MIND("??      ??),
    OTHER("   ?");

    private final String description;
}
