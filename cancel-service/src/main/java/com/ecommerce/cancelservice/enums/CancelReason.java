package com.ecommerce.cancelservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelReason {
    CHANGE_OF_MIND("단순 변심"),
    WRONG_ORDER("잘못된 주문"),
    DUPLICATE_ORDER("중복 주문"),
    PRICE_CHANGE("가격 변동"),
    DELIVERY_DELAY("배송 지연"),
    OTHER("기타");

    private final String description;
}
