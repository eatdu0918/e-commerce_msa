package com.ecommerce.cancelservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelRequestType {
    ORDER_CANCEL("주문 취소"),
    RETURN_REFUND("반품·환불");

    private final String description;
}
