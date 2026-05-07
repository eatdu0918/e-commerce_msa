package com.ecommerce.paymentservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CREDIT_CARD("?     ? ?),
    DEBIT_CARD("      ?  ?),
    BANK_TRANSFER("?   ??  "),
    VIRTUAL_ACCOUNT("   ?     ?),
    MOBILE_PAY("    ??  ??),
    TOSSPAYMENTS("?   ??  ?  ??);

    private final String description;
}
