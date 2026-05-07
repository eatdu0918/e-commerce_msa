package com.ecommerce.refundservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundStatus {
    PENDING("??   ?? ?),
    PROCESSING("??      ?  ?),
    COMPLETED("??   ?   "),
    FAILED("??   ??  ");

    private final String description;
}
