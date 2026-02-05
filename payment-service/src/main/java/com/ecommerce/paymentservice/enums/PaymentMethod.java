package com.ecommerce.paymentservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CREDIT_CARD("신용카드"),
    DEBIT_CARD("체크카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PAY("모바일결제");

    private final String description;
}
