package com.ecommerce.refundservice.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundReason {
    ORDER_CANCEL("주문 취소"),
    PRODUCT_DEFECT("상품 결함"),
    WRONG_PRODUCT("오배송"),
    CHANGE_OF_MIND("단순 변심"),
    OTHER("기타");

    private final String description;
}
