package com.ecommerce.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    Long productId;

    @NotBlank(message = "상품명은 필수입니다.")
    String productName;

    @NotNull(message = "단가는 필수입니다.")
    @Positive(message = "단가는 0보다 커야 합니다.")
    BigDecimal unitPrice;

    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 0보다 커야 합니다.")
    Integer quantity;
}
