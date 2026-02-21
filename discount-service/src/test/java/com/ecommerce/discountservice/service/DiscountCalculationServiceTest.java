package com.ecommerce.discountservice.service;

import com.ecommerce.discountservice.dto.request.CalculateDiscountRequest;
import com.ecommerce.discountservice.dto.response.DiscountCalculationResponse;
import com.ecommerce.discountservice.entity.Coupon;
import com.ecommerce.discountservice.entity.UserCoupon;
import com.ecommerce.discountservice.enums.CouponType;
import com.ecommerce.discountservice.exception.DiscountDomainException;
import com.ecommerce.discountservice.repository.UserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscountCalculationServiceTest {

    @Mock
    UserCouponRepository userCouponRepository;

    @InjectMocks
    DiscountCalculationService discountCalculationService;

    private static final Long USER_ID = 100L;
    private static final Long USER_COUPON_ID = 1L;
    private static final Long COUPON_ID = 10L;

    private Coupon percentageCoupon;
    private Coupon fixedAmountCoupon;
    private UserCoupon userCoupon;

    @BeforeEach
    void setUp() {
        percentageCoupon = createTestCoupon(COUPON_ID, "PERCENT10", "10% 할인",
                CouponType.PERCENTAGE, new BigDecimal("10"),
                new BigDecimal("10000"), new BigDecimal("5000"));

        fixedAmountCoupon = createTestCoupon(COUPON_ID + 1, "FIXED3000", "3000원 할인",
                CouponType.FIXED_AMOUNT, new BigDecimal("3000"),
                new BigDecimal("15000"), null);

        userCoupon = createTestUserCoupon(USER_COUPON_ID, USER_ID, percentageCoupon);
    }

    @Nested
    @DisplayName("정률 할인 계산")
    class PercentageDiscountTest {

        @Test
        @DisplayName("정률 할인 계산 성공")
        void calculateDiscount_percentage_success() {
            // given
            BigDecimal orderAmount = new BigDecimal("50000");
            CalculateDiscountRequest request = new CalculateDiscountRequest(USER_COUPON_ID, orderAmount);

            when(userCouponRepository.findByIdAndUserId(USER_COUPON_ID, USER_ID))
                    .thenReturn(Optional.of(userCoupon));

            // when
            DiscountCalculationResponse response = discountCalculationService.calculateDiscount(USER_ID, request);

            // then
            assertThat(response.getIsApplicable()).isTrue();
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5000")); // 50000 * 10% = 5000
            assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("45000"));
        }

        @Test
        @DisplayName("정률 할인 - 최대 할인 금액 적용")
        void calculateDiscount_percentage_maxDiscountApplied() {
            // given
            BigDecimal orderAmount = new BigDecimal("100000");
            CalculateDiscountRequest request = new CalculateDiscountRequest(USER_COUPON_ID, orderAmount);

            when(userCouponRepository.findByIdAndUserId(USER_COUPON_ID, USER_ID))
                    .thenReturn(Optional.of(userCoupon));

            // when
            DiscountCalculationResponse response = discountCalculationService.calculateDiscount(USER_ID, request);

            // then
            assertThat(response.getIsApplicable()).isTrue();
            // 100000 * 10% = 10000 이지만 maxDiscountAmount가 5000이므로 5000 적용
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("95000"));
        }
    }

    @Nested
    @DisplayName("정액 할인 계산")
    class FixedAmountDiscountTest {

        @Test
        @DisplayName("정액 할인 계산 성공")
        void calculateDiscount_fixedAmount_success() {
            // given
            UserCoupon fixedUserCoupon = createTestUserCoupon(2L, USER_ID, fixedAmountCoupon);
            BigDecimal orderAmount = new BigDecimal("20000");
            CalculateDiscountRequest request = new CalculateDiscountRequest(2L, orderAmount);

            when(userCouponRepository.findByIdAndUserId(2L, USER_ID))
                    .thenReturn(Optional.of(fixedUserCoupon));

            // when
            DiscountCalculationResponse response = discountCalculationService.calculateDiscount(USER_ID, request);

            // then
            assertThat(response.getIsApplicable()).isTrue();
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("3000"));
            assertThat(response.getFinalAmount()).isEqualByComparingTo(new BigDecimal("17000"));
        }
    }

    @Nested
    @DisplayName("할인 적용 불가")
    class NotApplicableTest {

        @Test
        @DisplayName("할인 계산 실패 - 사용자 쿠폰 없음")
        void calculateDiscount_userCouponNotFound_throwsException() {
            // given
            CalculateDiscountRequest request = new CalculateDiscountRequest(999L, new BigDecimal("50000"));

            when(userCouponRepository.findByIdAndUserId(999L, USER_ID))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> discountCalculationService.calculateDiscount(USER_ID, request))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("사용자 쿠폰을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("할인 적용 불가 - 최소 주문 금액 미달")
        void calculateDiscount_minOrderAmountNotMet() {
            // given
            BigDecimal orderAmount = new BigDecimal("5000"); // 최소 주문 금액 10000원 미달
            CalculateDiscountRequest request = new CalculateDiscountRequest(USER_COUPON_ID, orderAmount);

            when(userCouponRepository.findByIdAndUserId(USER_COUPON_ID, USER_ID))
                    .thenReturn(Optional.of(userCoupon));

            // when
            DiscountCalculationResponse response = discountCalculationService.calculateDiscount(USER_ID, request);

            // then
            assertThat(response.getIsApplicable()).isFalse();
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getMessage()).contains("최소 주문 금액");
        }

        @Test
        @DisplayName("할인 적용 불가 - 쿠폰 이미 사용됨")
        void calculateDiscount_couponAlreadyUsed() {
            // given
            userCoupon.use(500L); // 쿠폰을 이미 사용 상태로 변경
            BigDecimal orderAmount = new BigDecimal("50000");
            CalculateDiscountRequest request = new CalculateDiscountRequest(USER_COUPON_ID, orderAmount);

            when(userCouponRepository.findByIdAndUserId(USER_COUPON_ID, USER_ID))
                    .thenReturn(Optional.of(userCoupon));

            // when
            DiscountCalculationResponse response = discountCalculationService.calculateDiscount(USER_ID, request);

            // then
            assertThat(response.getIsApplicable()).isFalse();
            assertThat(response.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getMessage()).contains("사용할 수 없는 쿠폰");
        }

        @Test
        @DisplayName("할인 적용 불가 - 만료된 쿠폰")
        void calculateDiscount_expiredCoupon() {
            // given
            Coupon expiredCoupon = createTestCoupon(20L, "EXPIRED", "만료 쿠폰",
                    CouponType.PERCENTAGE, new BigDecimal("10"),
                    new BigDecimal("10000"), new BigDecimal("5000"));
            ReflectionTestUtils.setField(expiredCoupon, "validFrom", LocalDateTime.now().minusDays(30));
            ReflectionTestUtils.setField(expiredCoupon, "validUntil", LocalDateTime.now().minusDays(1));

            UserCoupon expiredUserCoupon = createTestUserCoupon(3L, USER_ID, expiredCoupon);
            CalculateDiscountRequest request = new CalculateDiscountRequest(3L, new BigDecimal("50000"));

            when(userCouponRepository.findByIdAndUserId(3L, USER_ID))
                    .thenReturn(Optional.of(expiredUserCoupon));

            // when
            DiscountCalculationResponse response = discountCalculationService.calculateDiscount(USER_ID, request);

            // then
            assertThat(response.getIsApplicable()).isFalse();
            assertThat(response.getMessage()).contains("사용할 수 없는 쿠폰");
        }
    }

    private Coupon createTestCoupon(Long id, String code, String name, CouponType type,
                                     BigDecimal discountValue, BigDecimal minOrderAmount,
                                     BigDecimal maxDiscountAmount) {
        Coupon coupon = Coupon.create(
                code, name, "테스트 쿠폰", type, discountValue,
                minOrderAmount, maxDiscountAmount,
                100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30)
        );
        ReflectionTestUtils.setField(coupon, "id", id);
        return coupon;
    }

    private UserCoupon createTestUserCoupon(Long id, Long userId, Coupon coupon) {
        UserCoupon userCoupon = UserCoupon.create(userId, coupon);
        ReflectionTestUtils.setField(userCoupon, "id", id);
        return userCoupon;
    }
}
