package com.ecommerce.cancelservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelReason {
    CHANGE_OF_MIND("CHANGE_OF_MIND"),
    WRONG_ORDER("WRONG_ORDER"),
    DUPLICATE_ORDER("DUPLICATE_ORDER"),
    PRICE_CHANGE("PRICE_CHANGE"),
    DELIVERY_DELAY("DELIVERY_DELAY"),
    OTHER("OTHER");

    private final String description;
}
