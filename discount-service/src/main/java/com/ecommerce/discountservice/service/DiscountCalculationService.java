package com.ecommerce.discountservice.service;

import com.ecommerce.discountservice.dto.request.CalculateDiscountRequest;
import com.ecommerce.discountservice.dto.response.DiscountCalculationResponse;
import com.ecommerce.discountservice.entity.Coupon;
import com.ecommerce.discountservice.entity.UserCoupon;
import com.ecommerce.discountservice.exception.DiscountDomainException;
import com.ecommerce.discountservice.exception.DiscountDomainExceptionCode;
import com.ecommerce.discountservice.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountCalculationService {

    private final UserCouponRepository userCouponRepository;

    @Transactional(readOnly = true)
    public DiscountCalculationResponse calculateDiscount(Long userId, CalculateDiscountRequest request) {
        log.info("할인 계산 시도: userId={}, userCouponId={}, orderAmount={}",
                userId, request.getUserCouponId(), request.getOrderAmount());

        UserCoupon userCoupon = userCouponRepository.findByIdAndUserId(request.getUserCouponId(), userId)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.UserCouponNotFoundException));

        if (!userCoupon.isAvailable()) {
            return DiscountCalculationResponse.notApplicable(
                    userCoupon.getId(),
                    userCoupon.getCoupon().getId(),
                    request.getOrderAmount(),
                    "사용할 수 없는 쿠폰입니다."
            );
        }

        Coupon coupon = userCoupon.getCoupon();

        if (coupon.getMinOrderAmount() != null &&
                request.getOrderAmount().compareTo(coupon.getMinOrderAmount()) < 0) {
            return DiscountCalculationResponse.notApplicable(
                    userCoupon.getId(),
                    coupon.getId(),
                    request.getOrderAmount(),
                    String.format("최소 주문 금액(%s원) 이상이어야 합니다.", coupon.getMinOrderAmount())
            );
        }

        BigDecimal discountAmount = coupon.calculateDiscount(request.getOrderAmount());

        log.info("할인 계산 완료: discountAmount={}", discountAmount);

        return DiscountCalculationResponse.applicable(
                userCoupon.getId(),
                coupon.getId(),
                coupon.getName(),
                request.getOrderAmount(),
                discountAmount
        );
    }
}
