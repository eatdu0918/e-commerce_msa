package com.ecommerce.discountservice.dto.response;

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
public class DiscountCalculationResponse {

    Long userCouponId;
    Long couponId;
    String couponName;
    BigDecimal orderAmount;
    BigDecimal discountAmount;
    BigDecimal finalAmount;
    Boolean isApplicable;
    String message;

    public static DiscountCalculationResponse applicable(Long userCouponId, Long couponId, String couponName,
                                                          BigDecimal orderAmount, BigDecimal discountAmount) {
        return DiscountCalculationResponse.builder()
                .userCouponId(userCouponId)
                .couponId(couponId)
                .couponName(couponName)
                .orderAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(orderAmount.subtract(discountAmount))
                .isApplicable(true)
                .message("    ?     ??)
                .build();
    }

    public static DiscountCalculationResponse notApplicable(Long userCouponId, Long couponId,
                                                             BigDecimal orderAmount, String reason) {
        return DiscountCalculationResponse.builder()
                .userCouponId(userCouponId)
                .couponId(couponId)
                .orderAmount(orderAmount)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(orderAmount)
                .isApplicable(false)
                .message(reason)
                .build();
    }
}
