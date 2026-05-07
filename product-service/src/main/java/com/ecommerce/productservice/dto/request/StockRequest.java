package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockRequest {

    @NotNull(message = "?  ? ID???   ??  ??")
    Long productId;

    @NotNull(message = "??  ?? ?   ??  ??")
    @Min(value = 1, message = "??  ?? 1 ??  ??  ????  ??")
    Integer quantity;

    public static StockRequest of(Long productId, Integer quantity) {
        return new StockRequest(productId, quantity);
    }
}
