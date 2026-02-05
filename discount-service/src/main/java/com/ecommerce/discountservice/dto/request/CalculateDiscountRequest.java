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

    @NotNull(message = "사용자 쿠폰 ID는 필수입니다.")
    Long userCouponId;

    @NotNull(message = "주문 금액은 필수입니다.")
    @Positive(message = "주문 금액은 0보다 커야 합니다.")
    BigDecimal orderAmount;
}
