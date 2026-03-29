package com.ecommerce.cancelservice.event;

import com.ecommerce.cancelservice.enums.CancelRequestType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonTypeName("CancelApprovedEvent")
public class CancelApprovedEvent {
    String eventId;
    Long cancelId;
    String cancelNumber;
    Long orderId;
    String orderNumber;
    Long userId;
    /**
     * 할인·쿠폰 반영 실제 결제액 기준 환불액. 없으면 구 소비자는 품목 단가 합으로 계산한다.
     */
    BigDecimal refundAmount;
    /** 출고 전 취소(ORDER_CANCEL) / 반품·환불(RETURN_REFUND). 결제 서비스 환불·취소 구분용 */
    CancelRequestType requestType;
    List<CancelItemEvent> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class CancelItemEvent {
        Long productId;
        String productName;
        Integer quantity;
        BigDecimal unitPrice;
    }
}
