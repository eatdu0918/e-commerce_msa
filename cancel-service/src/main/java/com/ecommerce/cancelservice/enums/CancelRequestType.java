package com.ecommerce.cancelservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelRequestType {
    ORDER_CANCEL("ORDER_CANCEL"),
    RETURN_REFUND("RETURN_REFUND");

    private final String description;
}
