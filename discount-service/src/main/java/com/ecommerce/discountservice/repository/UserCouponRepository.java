package com.ecommerce.discountservice.repository;

import com.ecommerce.discountservice.entity.UserCoupon;
import com.ecommerce.discountservice.enums.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    List<UserCoupon> findByUserId(Long userId);

    Page<UserCoupon> findByUserId(Long userId, Pageable pageable);

    List<UserCoupon> findByUserIdAndStatus(Long userId, CouponStatus status);

    Optional<UserCoupon> findByIdAndUserId(Long id, Long userId);

    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon WHERE uc.userId = :userId AND uc.status = :status")
    List<UserCoupon> findByUserIdAndStatusWithCoupon(@Param("userId") Long userId, @Param("status") CouponStatus status);

    Optional<UserCoupon> findByOrderId(Long orderId);
}
