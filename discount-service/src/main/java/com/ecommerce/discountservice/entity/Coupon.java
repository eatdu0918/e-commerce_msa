package com.ecommerce.discountservice.entity;

import com.ecommerce.discountservice.enums.CouponType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_valid_until", columnList = "valid_until")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    Long id;

    @Column(nullable = false, unique = true, length = 50)
    String code;

    @Column(nullable = false, length = 100)
    String name;

    @Column(length = 500)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false, length = 20)
    CouponType couponType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    BigDecimal discountValue;

    @Column(name = "min_order_amount", precision = 12, scale = 2)
    BigDecimal minOrderAmount;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    BigDecimal maxDiscountAmount;

    @Column(name = "total_quantity")
    Integer totalQuantity;

    @Column(name = "issued_quantity")
    @Builder.Default
    Integer issuedQuantity = 0;

    @Column(name = "valid_from", nullable = false)
    LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    LocalDateTime validUntil;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    public static Coupon create(String code, String name, String description,
                                CouponType couponType, BigDecimal discountValue,
                                BigDecimal minOrderAmount, BigDecimal maxDiscountAmount,
                                Integer totalQuantity, LocalDateTime validFrom, LocalDateTime validUntil) {
        return Coupon.builder()
                .code(code)
                .name(name)
                .description(description)
                .couponType(couponType)
                .discountValue(discountValue)
                .minOrderAmount(minOrderAmount)
                .maxDiscountAmount(maxDiscountAmount)
                .totalQuantity(totalQuantity)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .build();
    }

    public void update(String name, String description, CouponType couponType,
                       BigDecimal discountValue, BigDecimal minOrderAmount,
                       BigDecimal maxDiscountAmount, Integer totalQuantity,
                       LocalDateTime validFrom, LocalDateTime validUntil, Boolean isActive) {
        this.name = name;
        this.description = description;
        this.couponType = couponType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountAmount = maxDiscountAmount;
        this.totalQuantity = totalQuantity;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.isActive = isActive;
    }

    public void incrementIssuedQuantity() {
        this.issuedQuantity++;
    }

    public void decrementIssuedQuantity() {
        if (this.issuedQuantity > 0) {
            this.issuedQuantity--;
        }
    }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && now.isAfter(validFrom) && now.isBefore(validUntil);
    }

    public boolean hasAvailableQuantity() {
        return totalQuantity == null || issuedQuantity < totalQuantity;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (couponType == CouponType.PERCENTAGE) {
            discount = orderAmount.multiply(discountValue).divide(BigDecimal.valueOf(100));
        } else {
            discount = discountValue;
        }

        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }

        return discount;
    }
}
