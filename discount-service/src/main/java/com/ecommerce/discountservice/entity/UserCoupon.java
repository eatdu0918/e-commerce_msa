package com.ecommerce.discountservice.entity;

import com.ecommerce.discountservice.enums.CouponStatus;
import com.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_coupon", columnNames = {"user_id", "coupon_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    CouponStatus status = CouponStatus.AVAILABLE;

    @Column(name = "used_at")
    LocalDateTime usedAt;

    @Column(name = "order_id")
    Long orderId;

    public static UserCoupon create(Long userId, Coupon coupon) {
        return UserCoupon.builder()
                .userId(userId)
                .coupon(coupon)
                .build();
    }

    public void use(Long orderId) {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }

    public void restore() {
        this.status = CouponStatus.AVAILABLE;
        this.usedAt = null;
        this.orderId = null;
    }

    public void expire() {
        this.status = CouponStatus.EXPIRED;
    }

    public boolean isAvailable() {
        return status == CouponStatus.AVAILABLE && coupon.isValid();
    }
}
