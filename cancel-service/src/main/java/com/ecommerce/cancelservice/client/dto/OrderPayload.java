package com.ecommerce.cancelservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * order-service 주문 응답 중 취소 검증·환불액 산출에 필요한 필드만 수신.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPayload {
    String status;
    String statusBeforeCancelRequest;
    /** 목록·단건 집계와 동일한 표시용 진행 상태(스킵 결제 시 DB보다 앞설 수 있음). */
    String progressStatus;

    /** 주문 상품 합계(할인 전). */
    BigDecimal totalAmount;

    /**
 * 고객 실제 결제·청구 기준 금액(할인 반영).
     * 결제 연동 보정 직후 관리자 주문 조회 응답과 맞춘다.
     */
    BigDecimal finalAmount;

    /** payment-service 기준 실제 승인 금액. finalAmount가 지연·불일치일 때 환불 산출 우선 근거로 사용. */
    BigDecimal paymentAmount;

    List<OrderItemLinePayload> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderItemLinePayload {
        Long productId;
        Integer quantity;
        BigDecimal totalPrice;
    }
}
