package com.ecommerce.cancelservice.dto.response;

import com.ecommerce.cancelservice.entity.CancelItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

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
}
