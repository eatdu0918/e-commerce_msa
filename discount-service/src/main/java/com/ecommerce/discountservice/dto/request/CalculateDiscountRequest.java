package com.ecommerce.discountservice.dto.request;

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
public class CalculateDiscountRequest {

    @NotNull(message = "??????   ?ID???   ??  ??")
    Long userCouponId;

    @NotNull(message = "         ?? ?   ??  ??")
    @Positive(message = "         ?? 0  ????   ???  ??")
    BigDecimal orderAmount;
}
