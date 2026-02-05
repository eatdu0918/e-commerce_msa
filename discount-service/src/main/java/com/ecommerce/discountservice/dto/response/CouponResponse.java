package com.ecommerce.discountservice.dto.response;

import com.ecommerce.discountservice.entity.Coupon;
import com.ecommerce.discountservice.enums.CouponType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponResponse {

    Long id;
    String code;
    String name;
    String description;
    CouponType couponType;
    BigDecimal discountValue;
    BigDecimal minOrderAmount;
    BigDecimal maxDiscountAmount;
    Integer totalQuantity;
    Integer issuedQuantity;
    Integer remainingQuantity;
    LocalDateTime validFrom;
    LocalDateTime validUntil;
    Boolean isActive;
    Boolean isValid;
    LocalDateTime createdAt;

    public static CouponResponse from(Coupon coupon) {
        Integer remaining = coupon.getTotalQuantity() != null
                ? coupon.getTotalQuantity() - coupon.getIssuedQuantity()
                : null;

        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .couponType(coupon.getCouponType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .totalQuantity(coupon.getTotalQuantity())
                .issuedQuantity(coupon.getIssuedQuantity())
                .remainingQuantity(remaining)
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .isActive(coupon.getIsActive())
                .isValid(coupon.isValid())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}
