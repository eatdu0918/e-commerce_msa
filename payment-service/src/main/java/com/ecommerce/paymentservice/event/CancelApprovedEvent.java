package com.ecommerce.paymentservice.event;

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
    BigDecimal refundAmount;
    /** ORDER_CANCEL | RETURN_REFUND. 없으면 기존과 동일하게 결제 취소(CANCELLED) 처리 */
    String requestType;
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
