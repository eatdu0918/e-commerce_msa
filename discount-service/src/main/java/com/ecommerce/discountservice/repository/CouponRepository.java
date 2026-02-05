package com.ecommerce.discountservice.repository;

import com.ecommerce.discountservice.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findByIdAndIsActiveTrue(Long id);

    Optional<Coupon> findByCodeAndIsActiveTrue(String code);

    boolean existsByCode(String code);

    Page<Coupon> findAllByIsActiveTrue(Pageable pageable);
}
