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
import com.ecommerce.discountservice.enums.CouponType;
import com.ecommerce.discountservice.exception.DiscountDomainException;
import com.ecommerce.discountservice.repository.CouponRepository;
import com.ecommerce.discountservice.repository.UserCouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    CouponRepository couponRepository;

    @Mock
    UserCouponRepository userCouponRepository;

    @InjectMocks
    CouponService couponService;

    private static final Long COUPON_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final String COUPON_CODE = "SAVE10";
    private static final String COUPON_NAME = "10% 할인 쿠폰";

    private Coupon testCoupon;
    private UserCoupon testUserCoupon;

    @BeforeEach
    void setUp() {
        testCoupon = createTestCoupon(COUPON_ID, COUPON_CODE, COUPON_NAME, CouponType.PERCENTAGE,
                new BigDecimal("10"), 100, true);
        testUserCoupon = createTestUserCoupon(1L, USER_ID, testCoupon);
    }

    @Nested
    @DisplayName("쿠폰 생성")
    class CreateCouponTest {

        @Test
        @DisplayName("쿠폰 생성 성공")
        void createCoupon_success() {
            // given
            CreateCouponRequest request = new CreateCouponRequest(
                    COUPON_CODE, COUPON_NAME, "10% 할인", CouponType.PERCENTAGE,
                    new BigDecimal("10"), new BigDecimal("10000"), new BigDecimal("5000"),
                    100, LocalDateTime.now(), LocalDateTime.now().plusDays(30)
            );

            when(couponRepository.existsByCode(COUPON_CODE)).thenReturn(false);
            when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> {
                Coupon coupon = invocation.getArgument(0);
                ReflectionTestUtils.setField(coupon, "id", COUPON_ID);
                return coupon;
            });

            // when
            CouponResponse response = couponService.createCoupon(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(COUPON_CODE);
            assertThat(response.getName()).isEqualTo(COUPON_NAME);
            assertThat(response.getCouponType()).isEqualTo(CouponType.PERCENTAGE);
            verify(couponRepository).save(any(Coupon.class));
        }

        @Test
        @DisplayName("쿠폰 생성 실패 - 중복 코드")
        void createCoupon_duplicateCode_throwsException() {
            // given
            CreateCouponRequest request = new CreateCouponRequest(
                    COUPON_CODE, COUPON_NAME, "10% 할인", CouponType.PERCENTAGE,
                    new BigDecimal("10"), null, null,
                    100, LocalDateTime.now(), LocalDateTime.now().plusDays(30)
            );

            when(couponRepository.existsByCode(COUPON_CODE)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> couponService.createCoupon(request))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("이미 존재하는 쿠폰 코드");
        }
    }

    @Nested
    @DisplayName("쿠폰 조회")
    class GetCouponTest {

        @Test
        @DisplayName("쿠폰 단건 조회 성공")
        void getCoupon_success() {
            // given
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(testCoupon));

            // when
            CouponResponse response = couponService.getCoupon(COUPON_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(COUPON_ID);
            assertThat(response.getCode()).isEqualTo(COUPON_CODE);
        }

        @Test
        @DisplayName("쿠폰 조회 실패 - 존재하지 않음")
        void getCoupon_notFound_throwsException() {
            // given
            when(couponRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.getCoupon(999L))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("쿠폰을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("전체 쿠폰 목록 조회")
        void getAllCoupons_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Coupon> couponPage = new PageImpl<>(List.of(testCoupon), pageable, 1);

            when(couponRepository.findAll(pageable)).thenReturn(couponPage);

            // when
            PageResponse<CouponResponse> response = couponService.getAllCoupons(pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("활성 쿠폰 목록 조회")
        void getActiveCoupons_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Coupon> couponPage = new PageImpl<>(List.of(testCoupon), pageable, 1);

            when(couponRepository.findAllByIsActiveTrue(pageable)).thenReturn(couponPage);

            // when
            PageResponse<CouponResponse> response = couponService.getActiveCoupons(pageable);

            // then
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("쿠폰 수정")
    class UpdateCouponTest {

        @Test
        @DisplayName("쿠폰 수정 성공")
        void updateCoupon_success() {
            // given
            UpdateCouponRequest request = new UpdateCouponRequest(
                    "업데이트된 쿠폰", "수정된 설명", CouponType.FIXED_AMOUNT,
                    new BigDecimal("3000"), new BigDecimal("10000"), new BigDecimal("3000"),
                    200, LocalDateTime.now(), LocalDateTime.now().plusDays(60), true
            );

            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(testCoupon));

            // when
            CouponResponse response = couponService.updateCoupon(COUPON_ID, request);

            // then
            assertThat(response).isNotNull();
            assertThat(testCoupon.getName()).isEqualTo("업데이트된 쿠폰");
            assertThat(testCoupon.getCouponType()).isEqualTo(CouponType.FIXED_AMOUNT);
        }

        @Test
        @DisplayName("쿠폰 수정 실패 - 존재하지 않음")
        void updateCoupon_notFound_throwsException() {
            // given
            UpdateCouponRequest request = new UpdateCouponRequest(
                    "업데이트", "설명", CouponType.PERCENTAGE,
                    new BigDecimal("10"), null, null,
                    100, LocalDateTime.now(), LocalDateTime.now().plusDays(30), true
            );

            when(couponRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.updateCoupon(999L, request))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("쿠폰을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("쿠폰 삭제")
    class DeleteCouponTest {

        @Test
        @DisplayName("쿠폰 삭제(비활성화) 성공")
        void deleteCoupon_success() {
            // given
            when(couponRepository.findById(COUPON_ID)).thenReturn(Optional.of(testCoupon));

            // when
            couponService.deleteCoupon(COUPON_ID);

            // then
            assertThat(testCoupon.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("쿠폰 삭제 실패 - 존재하지 않음")
        void deleteCoupon_notFound_throwsException() {
            // given
            when(couponRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.deleteCoupon(999L))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("쿠폰을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("쿠폰 발급")
    class ClaimCouponTest {

        @Test
        @DisplayName("쿠폰 발급 성공")
        void claimCoupon_success() {
            // given
            when(couponRepository.findByCodeAndIsActiveTrue(COUPON_CODE)).thenReturn(Optional.of(testCoupon));
            when(userCouponRepository.existsByUserIdAndCouponId(USER_ID, COUPON_ID)).thenReturn(false);
            when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(invocation -> {
                UserCoupon uc = invocation.getArgument(0);
                ReflectionTestUtils.setField(uc, "id", 1L);
                return uc;
            });

            // when
            UserCouponResponse response = couponService.claimCoupon(USER_ID, COUPON_CODE);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(USER_ID);
            assertThat(testCoupon.getIssuedQuantity()).isEqualTo(1);
            verify(userCouponRepository).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 쿠폰 없음")
        void claimCoupon_notFound_throwsException() {
            // given
            when(couponRepository.findByCodeAndIsActiveTrue("INVALID")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.claimCoupon(USER_ID, "INVALID"))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("쿠폰을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 유효하지 않은 쿠폰")
        void claimCoupon_notValid_throwsException() {
            // given
            Coupon expiredCoupon = createTestCoupon(2L, "EXPIRED", "만료쿠폰", CouponType.PERCENTAGE,
                    new BigDecimal("10"), 100, true);
            ReflectionTestUtils.setField(expiredCoupon, "validFrom", LocalDateTime.now().minusDays(30));
            ReflectionTestUtils.setField(expiredCoupon, "validUntil", LocalDateTime.now().minusDays(1));

            when(couponRepository.findByCodeAndIsActiveTrue("EXPIRED")).thenReturn(Optional.of(expiredCoupon));

            // when & then
            assertThatThrownBy(() -> couponService.claimCoupon(USER_ID, "EXPIRED"))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("사용 가능한 쿠폰이 아닙니다");
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 수량 소진")
        void claimCoupon_outOfStock_throwsException() {
            // given
            Coupon soldOutCoupon = createTestCoupon(3L, "SOLDOUT", "품절쿠폰", CouponType.PERCENTAGE,
                    new BigDecimal("10"), 10, true);
            ReflectionTestUtils.setField(soldOutCoupon, "issuedQuantity", 10);

            when(couponRepository.findByCodeAndIsActiveTrue("SOLDOUT")).thenReturn(Optional.of(soldOutCoupon));

            // when & then
            assertThatThrownBy(() -> couponService.claimCoupon(USER_ID, "SOLDOUT"))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("쿠폰 수량이 소진");
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 이미 발급받음")
        void claimCoupon_alreadyClaimed_throwsException() {
            // given
            when(couponRepository.findByCodeAndIsActiveTrue(COUPON_CODE)).thenReturn(Optional.of(testCoupon));
            when(userCouponRepository.existsByUserIdAndCouponId(USER_ID, COUPON_ID)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> couponService.claimCoupon(USER_ID, COUPON_CODE))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("이미 발급받은 쿠폰");
        }
    }

    @Nested
    @DisplayName("쿠폰 일괄 발급")
    class BulkGrantCouponTest {

        @Test
        @DisplayName("일괄 발급 성공")
        void bulkGrantCoupon_success() {
            Long userId2 = 101L;
            BulkGrantCouponRequest request = new BulkGrantCouponRequest(
                    COUPON_CODE, List.of(USER_ID, userId2));

            when(couponRepository.findByCodeAndIsActiveTrue(COUPON_CODE)).thenReturn(Optional.of(testCoupon));
            when(userCouponRepository.existsByUserIdAndCouponId(USER_ID, COUPON_ID)).thenReturn(false);
            when(userCouponRepository.existsByUserIdAndCouponId(userId2, COUPON_ID)).thenReturn(false);
            when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BulkGrantCouponResponse response = couponService.bulkGrantCoupon(request);

            assertThat(response.getCouponCode()).isEqualTo(COUPON_CODE);
            assertThat(response.getGrantedCount()).isEqualTo(2);
            assertThat(response.getSkippedCount()).isZero();
            assertThat(testCoupon.getIssuedQuantity()).isEqualTo(2);
            verify(userCouponRepository, times(2)).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("일괄 발급 - 이미 보유한 사용자는 건너뜀")
        void bulkGrantCoupon_skipsAlreadyGranted() {
            Long userId2 = 101L;
            BulkGrantCouponRequest request = new BulkGrantCouponRequest(
                    COUPON_CODE, List.of(USER_ID, userId2));

            when(couponRepository.findByCodeAndIsActiveTrue(COUPON_CODE)).thenReturn(Optional.of(testCoupon));
            when(userCouponRepository.existsByUserIdAndCouponId(USER_ID, COUPON_ID)).thenReturn(true);
            when(userCouponRepository.existsByUserIdAndCouponId(userId2, COUPON_ID)).thenReturn(false);
            when(userCouponRepository.save(any(UserCoupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BulkGrantCouponResponse response = couponService.bulkGrantCoupon(request);

            assertThat(response.getGrantedCount()).isEqualTo(1);
            assertThat(response.getSkippedCount()).isEqualTo(1);
            assertThat(testCoupon.getIssuedQuantity()).isEqualTo(1);
            verify(userCouponRepository, times(1)).save(any(UserCoupon.class));
        }

        @Test
        @DisplayName("일괄 발급 실패 - 쿠폰 없음")
        void bulkGrantCoupon_notFound() {
            BulkGrantCouponRequest request = new BulkGrantCouponRequest("NONE", List.of(USER_ID));
            when(couponRepository.findByCodeAndIsActiveTrue("NONE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> couponService.bulkGrantCoupon(request))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("쿠폰을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("사용자 쿠폰 조회")
    class GetUserCouponsTest {

        @Test
        @DisplayName("사용자 쿠폰 목록 조회")
        void getUserCoupons_success() {
            // given
            when(userCouponRepository.findByUserId(USER_ID)).thenReturn(List.of(testUserCoupon));

            // when
            List<UserCouponResponse> response = couponService.getUserCoupons(USER_ID);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("사용자 사용 가능 쿠폰 목록 조회")
        void getAvailableUserCoupons_success() {
            // given
            when(userCouponRepository.findByUserIdAndStatusWithCoupon(USER_ID, CouponStatus.AVAILABLE))
                    .thenReturn(List.of(testUserCoupon));

            // when
            List<UserCouponResponse> response = couponService.getAvailableUserCoupons(USER_ID);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).getStatus()).isEqualTo(CouponStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("쿠폰 사용")
    class UseCouponTest {

        @Test
        @DisplayName("쿠폰 사용 성공")
        void useCoupon_success() {
            // given
            Long userCouponId = 1L;
            Long orderId = 500L;

            when(userCouponRepository.findById(userCouponId)).thenReturn(Optional.of(testUserCoupon));

            // when
            couponService.useCoupon(userCouponId, orderId);

            // then
            assertThat(testUserCoupon.getStatus()).isEqualTo(CouponStatus.USED);
            assertThat(testUserCoupon.getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 존재하지 않음")
        void useCoupon_notFound_throwsException() {
            // given
            when(userCouponRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.useCoupon(999L, 500L))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("사용자 쿠폰을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 이미 사용됨")
        void useCoupon_alreadyUsed_throwsException() {
            // given
            testUserCoupon.use(100L);
            when(userCouponRepository.findById(1L)).thenReturn(Optional.of(testUserCoupon));

            // when & then
            assertThatThrownBy(() -> couponService.useCoupon(1L, 500L))
                    .isInstanceOf(DiscountDomainException.class)
                    .hasMessageContaining("사용 가능한 쿠폰이 아닙니다");
        }
    }

    @Nested
    @DisplayName("쿠폰 복원")
    class RestoreCouponTest {

        @Test
        @DisplayName("쿠폰 복원 성공")
        void restoreCoupon_success() {
            // given
            Long orderId = 500L;
            testUserCoupon.use(orderId);

            when(userCouponRepository.findByOrderId(orderId)).thenReturn(Optional.of(testUserCoupon));

            // when
            couponService.restoreCoupon(orderId);

            // then
            assertThat(testUserCoupon.getStatus()).isEqualTo(CouponStatus.AVAILABLE);
            assertThat(testUserCoupon.getOrderId()).isNull();
        }

        @Test
        @DisplayName("쿠폰 복원 - 해당 주문 쿠폰 없음 (무시)")
        void restoreCoupon_noUserCoupon_ignored() {
            // given
            when(userCouponRepository.findByOrderId(999L)).thenReturn(Optional.empty());

            // when & then (예외 없이 완료)
            couponService.restoreCoupon(999L);
            verify(userCouponRepository).findByOrderId(999L);
        }
    }

    private Coupon createTestCoupon(Long id, String code, String name, CouponType type,
                                     BigDecimal discountValue, Integer totalQuantity, boolean isActive) {
        Coupon coupon = Coupon.create(
                code, name, "테스트 쿠폰 설명", type, discountValue,
                new BigDecimal("10000"), new BigDecimal("5000"),
                totalQuantity, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30)
        );
        ReflectionTestUtils.setField(coupon, "id", id);
        ReflectionTestUtils.setField(coupon, "isActive", isActive);
        return coupon;
    }

    private UserCoupon createTestUserCoupon(Long id, Long userId, Coupon coupon) {
        UserCoupon userCoupon = UserCoupon.create(userId, coupon);
        ReflectionTestUtils.setField(userCoupon, "id", id);
        return userCoupon;
    }
}
