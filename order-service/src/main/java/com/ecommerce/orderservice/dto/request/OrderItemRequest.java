package com.ecommerce.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemRequest {

    @NotNull(message = "?  ? ID???   ??  ??")
    Long productId;

    @NotBlank(message = "?  ?   ? ?   ??  ??")
    String productName;

    String imageUrl;

    @NotNull(message = "??????   ??  ??")
    @Positive(message = "?????0  ????   ???  ??")
    BigDecimal unitPrice;

    @NotNull(message = "??  ?? ?   ??  ??")
    @Positive(message = "??  ?? 0  ????   ???  ??")
    Integer quantity;
}
