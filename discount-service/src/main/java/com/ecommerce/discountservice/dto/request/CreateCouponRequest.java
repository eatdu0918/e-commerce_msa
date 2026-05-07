package com.ecommerce.discountservice.dto.request;

import com.ecommerce.discountservice.enums.CouponType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCouponRequest {

    @NotBlank(message = "?   ??   ???   ??  ??")
    String code;

    @NotBlank(message = "?   ???  ?? ?   ??  ??")
    String name;

    String description;

    @NotNull(message = "?   ?????? ?   ??  ??")
    CouponType couponType;

    @NotNull(message = "?      ?? ?   ??  ??")
    @Positive(message = "?      ?? 0  ????   ???  ??")
    BigDecimal discountValue;

    BigDecimal minOrderAmount;

    BigDecimal maxDiscountAmount;

    Integer totalQuantity;

    @NotNull(message = "?    ??  ??? ?   ??  ??")
    LocalDateTime validFrom;

    @NotNull(message = "?    ?   ??? ?   ??  ??")
    LocalDateTime validUntil;
}
