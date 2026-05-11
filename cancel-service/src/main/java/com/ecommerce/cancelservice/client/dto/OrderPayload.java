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
 * order-service에서 주문 정보를 조회할 때 사용하는 데이터 구조입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPayload {
    String status;
    String statusBeforeCancelRequest;
    /** 실제 주문 상태와 취소 상태를 결합하여 보여주는 상태 값입니다. */
    String progressStatus;

    /** 상품 합계 금액입니다. */
    BigDecimal totalAmount;

    /**
     * 최종 결제 대상 금액입니다.
     */
    BigDecimal finalAmount;

    /** 결제 서비스에서 실제 승인된 금액입니다. */
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
