package com.ecommerce.cancelservice.dto.request;

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
public class CancelItemRequest {

    @NotNull(message = "?  ? ID???   ??  ??")
    Long productId;

    @NotBlank(message = "?  ?   ? ?   ??  ??")
    String productName;

    @NotNull(message = "??  ?? ?   ??  ??")
    @Positive(message = "??  ?? 0  ????   ???  ??")
    Integer quantity;

    @NotNull(message = "??????   ??  ??")
    @Positive(message = "?????0  ????   ???  ??")
    BigDecimal unitPrice;
}
