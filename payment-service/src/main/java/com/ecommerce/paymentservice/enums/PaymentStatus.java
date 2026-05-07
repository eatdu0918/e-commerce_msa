package com.ecommerce.paymentservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("   ???? ?),
    COMPLETED("   ???   "),
    FAILED("   ????  "),
    CANCELLED("   ???  ??),
    REFUNDED("??   ?   ");

    private final String description;
}
