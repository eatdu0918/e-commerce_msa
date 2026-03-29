package com.ecommerce.discountservice.service;

import com.ecommerce.discountservice.dto.request.BulkGrantCouponRequest;
import com.ecommerce.discountservice.dto.request.CreateCouponRequest;
import com.ecommerce.discountservice.dto.request.UpdateCouponRequest;
import com.ecommerce.discountservice.dto.response.BulkGrantCouponResponse;
import com.ecommerce.discountservice.dto.response.CouponResponse;
import com.ecommerce.discountservice.dto.response.PageResponse;
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
        log.info("쿠폰 생성 시도: code={}", request.getCode());

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
        log.info("쿠폰 생성 완료: couponId={}, code={}", savedCoupon.getId(), savedCoupon.getCode());

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
        log.info("쿠폰 수정 시도: couponId={}", couponId);

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

        log.info("쿠폰 수정 완료: couponId={}", couponId);
        return CouponResponse.from(coupon);
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        log.info("쿠폰 삭제 시도: couponId={}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));

        coupon.deactivate();
        log.info("쿠폰 삭제(비활성화) 완료: couponId={}", couponId);
    }

    @Transactional
    public UserCouponResponse claimCoupon(Long userId, String couponCode) {
        log.info("쿠폰 발급 시도: userId={}, code={}", userId, couponCode);

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

        log.info("쿠폰 발급 완료: userCouponId={}, userId={}, couponId={}",
                savedUserCoupon.getId(), userId, coupon.getId());

        return UserCouponResponse.from(savedUserCoupon);
    }

    @Transactional
    public BulkGrantCouponResponse bulkGrantCoupon(BulkGrantCouponRequest request) {
        String couponCode = request.getCouponCode();
        List<Long> userIds = request.getUserIds().stream().distinct().toList();

        log.info("쿠폰 일괄 발급 시도: code={}, userCount={}", couponCode, userIds.size());

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
                log.warn("쿠폰 수량 소진으로 일괄 발급 중단: code={}", couponCode);
                break;
            }
            coupon.incrementIssuedQuantity();
            userCouponRepository.save(UserCoupon.create(userId, coupon));
            granted++;
        }

        log.info("쿠폰 일괄 발급 완료: code={}, granted={}, skipped={}", couponCode, granted, skipped);

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
        log.info("쿠폰 사용 처리: userCouponId={}, orderId={}", userCouponId, orderId);

        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new DiscountDomainException(DiscountDomainExceptionCode.UserCouponNotFoundException));

        if (!userCoupon.isAvailable()) {
            throw new DiscountDomainException(DiscountDomainExceptionCode.UserCouponNotAvailableException);
        }

        userCoupon.use(orderId);
        log.info("쿠폰 사용 완료: userCouponId={}, orderId={}", userCouponId, orderId);
    }

    @Transactional
    public void restoreCoupon(Long orderId) {
        log.info("쿠폰 복원 시도: orderId={}", orderId);

        userCouponRepository.findByOrderId(orderId).ifPresent(userCoupon -> {
            userCoupon.restore();
            log.info("쿠폰 복원 완료: userCouponId={}, orderId={}", userCoupon.getId(), orderId);
        });
    }
}
