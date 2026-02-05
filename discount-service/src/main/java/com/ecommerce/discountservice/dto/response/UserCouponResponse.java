package com.ecommerce.discountservice.dto.response;

import com.ecommerce.discountservice.entity.UserCoupon;
import com.ecommerce.discountservice.enums.CouponStatus;
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
public class UserCouponResponse {

    Long id;
    Long userId;
    Long couponId;
    String couponCode;
    String couponName;
    String couponDescription;
    CouponType couponType;
    BigDecimal discountValue;
    BigDecimal minOrderAmount;
    BigDecimal maxDiscountAmount;
    CouponStatus status;
    LocalDateTime usedAt;
    Long orderId;
    LocalDateTime validFrom;
    LocalDateTime validUntil;
    Boolean isAvailable;
    LocalDateTime createdAt;

    public static UserCouponResponse from(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .id(userCoupon.getId())
                .userId(userCoupon.getUserId())
                .couponId(userCoupon.getCoupon().getId())
                .couponCode(userCoupon.getCoupon().getCode())
                .couponName(userCoupon.getCoupon().getName())
                .couponDescription(userCoupon.getCoupon().getDescription())
                .couponType(userCoupon.getCoupon().getCouponType())
                .discountValue(userCoupon.getCoupon().getDiscountValue())
                .minOrderAmount(userCoupon.getCoupon().getMinOrderAmount())
                .maxDiscountAmount(userCoupon.getCoupon().getMaxDiscountAmount())
                .status(userCoupon.getStatus())
                .usedAt(userCoupon.getUsedAt())
                .orderId(userCoupon.getOrderId())
                .validFrom(userCoupon.getCoupon().getValidFrom())
                .validUntil(userCoupon.getCoupon().getValidUntil())
                .isAvailable(userCoupon.isAvailable())
                .createdAt(userCoupon.getCreatedAt())
                .build();
    }
}
