package com.ecommerce.cancelservice.dto.response;

import com.ecommerce.cancelservice.entity.CancelItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelItemResponse {

    Long id;
    Long productId;
    String productName;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;

    public static CancelItemResponse from(CancelItem cancelItem) {
        return CancelItemResponse.builder()
                .id(cancelItem.getId())
                .productId(cancelItem.getProductId())
                .productName(cancelItem.getProductName())
                .quantity(cancelItem.getQuantity())
                .unitPrice(cancelItem.getUnitPrice())
                .totalPrice(cancelItem.getTotalPrice())
                .build();
    }

    /** ?       ????? ??   ??   ??????   ??  ???????. */
    public static CancelItemResponse fromWithUnitPrice(CancelItem cancelItem, BigDecimal unitPrice) {
        BigDecimal safeUnit = unitPrice != null ? unitPrice : cancelItem.getUnitPrice();
        BigDecimal total = safeUnit
                .multiply(BigDecimal.valueOf(cancelItem.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return CancelItemResponse.builder()
                .id(cancelItem.getId())
                .productId(cancelItem.getProductId())
                .productName(cancelItem.getProductName())
                .quantity(cancelItem.getQuantity())
                .unitPrice(safeUnit.setScale(2, RoundingMode.HALF_UP))
                .totalPrice(total)
                .build();
    }
}
