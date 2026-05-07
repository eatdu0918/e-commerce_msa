package com.ecommerce.discountservice.service;

import com.ecommerce.discountservice.dto.request.BulkGrantCouponRequest;
import com.ecommerce.discountservice.dto.request.CreateCouponRequest;
import com.ecommerce.discountservice.dto.request.UpdateCouponRequest;
import com.ecommerce.discountservice.dto.response.BulkGrantCouponResponse;
import com.ecommerce.discountservice.dto.response.CouponResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.discountservice.dto.response.UserCouponResponse;
import com.ecommerce.discountservice.entity.Coupon;
import com.ecommerce.discountservice.entity.UserCoupon;
import com.ecommerce.discountservice.enums.CouponStatus;
import com.ecommerce.discountservice.exception.DiscountDomainException;
import com.ecommerce.discountservice.exception.DiscountDomainExceptionCode;
import com.ecommerce.discountservice.repository.CouponRepository;
import com.ecommerce.discountservice.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        log.info("?   ???   ??  : code={}", request.getCode());

        if (couponRepository.existsByCode(request.getCode())) {
            throw new DiscountDomainException(DiscountDomainExceptionCode.CouponAlreadyExistsException);
        }

        Coupon coupon = Coupon.create(
                request.getCode(),
                request.getName(),
                request.getDescription(),
                request.getCouponType(),
                request.getDiscountValue(),
                request.getMinOrderAmount(),
                request.getMaxDiscountAmount(),
                request.getTotalQuantity(),
                request.getValidFrom(),
                request.getValidUntil()
        );

        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("?   ???   ?   : couponId={}, code={}", savedCoupon.getId(), savedCoupon.getCode());

        return CouponResponse.from(savedCoupon);
    }

    @Transactional(readOnly = true)
    public CouponResponse getCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));
        return CouponResponse.from(coupon);
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> getAllCoupons(Pageable pageable) {
        Page<Coupon> coupons = couponRepository.findAll(pageable);
        Page<CouponResponse> responsePage = coupons.map(CouponResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> getActiveCoupons(Pageable pageable) {
        Page<Coupon> coupons = couponRepository.findAllByIsActiveTrue(pageable);
        Page<CouponResponse> responsePage = coupons.map(CouponResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional
    public CouponResponse updateCoupon(Long couponId, UpdateCouponRequest request) {
        log.info("?   ???   ??  : couponId={}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));

        coupon.update(
                request.getName(),
                request.getDescription(),
                request.getCouponType(),
                request.getDiscountValue(),
                request.getMinOrderAmount(),
                request.getMaxDiscountAmount(),
                request.getTotalQuantity(),
                request.getValidFrom(),
                request.getValidUntil(),
                request.getIsActive()
        );

        log.info("?   ???   ?   : couponId={}", couponId);
        return CouponResponse.from(coupon);
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        log.info("?   ???????  : couponId={}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));

        coupon.deactivate();
        log.info("?   ??????? ??   ) ?   : couponId={}", couponId);
    }

    @Transactional
    public UserCouponResponse claimCoupon(Long userId, String couponCode) {
        log.info("?   ?   ????  : userId={}, code={}", userId, couponCode);

        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(couponCode)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));

        if (!coupon.isValid()) {
            throw new DiscountDomainException(DiscountDomainExceptionCode.CouponNotValidException);
        }

        if (!coupon.hasAvailableQuantity()) {
            throw new DiscountDomainException(DiscountDomainExceptionCode.CouponOutOfStockException);
        }

        if (userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())) {
            throw new DiscountDomainException(DiscountDomainExceptionCode.UserCouponAlreadyClaimedException);
        }

        coupon.incrementIssuedQuantity();

        UserCoupon userCoupon = UserCoupon.create(userId, coupon);
        UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);

        log.info("?   ?   ???   : userCouponId={}, userId={}, couponId={}",
                savedUserCoupon.getId(), userId, coupon.getId());

        return UserCouponResponse.from(savedUserCoupon);
    }

    @Transactional
    public BulkGrantCouponResponse bulkGrantCoupon(BulkGrantCouponRequest request) {
        String couponCode = request.getCouponCode();
        List<Long> userIds = request.getUserIds().stream().distinct().toList();

        log.info("?   ???      ????  : code={}, userCount={}", couponCode, userIds.size());

        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(couponCode)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));

        if (!coupon.isValid()) {
            throw new DiscountDomainException(DiscountDomainExceptionCode.CouponNotValidException);
        }

        int granted = 0;
        int skipped = 0;

        for (Long userId : userIds) {
            if (userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())) {
                skipped++;
                continue;
            }
            if (!coupon.hasAvailableQuantity()) {
                log.warn("?   ???   ??? ??   ??      ??   ?? code={}", couponCode);
                break;
            }
            coupon.incrementIssuedQuantity();
            userCouponRepository.save(UserCoupon.create(userId, coupon));
            granted++;
        }

        log.info("?   ???      ???   : code={}, granted={}, skipped={}", couponCode, granted, skipped);

        return BulkGrantCouponResponse.builder()
                .couponCode(couponCode)
                .grantedCount(granted)
                .skippedCount(skipped)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserCouponResponse> getUserCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        return userCoupons.stream()
                .map(UserCouponResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserCouponResponse> getAvailableUserCoupons(Long userId) {
        List<UserCoupon> userCoupons = userCouponRepository.findByUserIdAndStatusWithCoupon(userId, CouponStatus.AVAILABLE);
        return userCoupons.stream()
                .filter(UserCoupon::isAvailable)
                .map(UserCouponResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserCoupon getUserCouponEntity(Long userCouponId, Long userId) {
        return userCouponRepository.findByIdAndUserId(userCouponId, userId)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.UserCouponNotFoundException));
    }

    @Transactional
    public void useCoupon(Long userCouponId, Long orderId) {
        log.info("?   ?????   ?? userCouponId={}, orderId={}", userCouponId, orderId);

        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.UserCouponNotFoundException));

        if (!userCoupon.isAvailable()) {
            throw new DiscountDomainException(DiscountDomainExceptionCode.UserCouponNotAvailableException);
        }

        userCoupon.use(orderId);
        log.info("?   ??????   : userCouponId={}, orderId={}", userCouponId, orderId);
    }

    @Transactional
    public void restoreCoupon(Long orderId) {
        log.info("?   ?   ????  : orderId={}", orderId);

        userCouponRepository.findByOrderId(orderId).ifPresent(userCoupon -> {
            userCoupon.restore();
            log.info("?   ?   ???   : userCouponId={}, orderId={}", userCoupon.getId(), orderId);
        });
    }
}
