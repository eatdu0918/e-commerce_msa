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

    @NotBlank(message = "쿠폰 코드는 필수입니다.")
    String code;

    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    String name;

    String description;

    @NotNull(message = "쿠폰 타입은 필수입니다.")
    CouponType couponType;

    @NotNull(message = "할인 값은 필수입니다.")
    @Positive(message = "할인 값은 0보다 커야 합니다.")
    BigDecimal discountValue;

    BigDecimal minOrderAmount;

    BigDecimal maxDiscountAmount;

    Integer totalQuantity;

    @NotNull(message = "유효 시작일은 필수입니다.")
    LocalDateTime validFrom;

    @NotNull(message = "유효 종료일은 필수입니다.")
    LocalDateTime validUntil;
}
