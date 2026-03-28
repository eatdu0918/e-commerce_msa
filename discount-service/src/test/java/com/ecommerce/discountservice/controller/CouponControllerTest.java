package com.ecommerce.discountservice.controller;

import com.ecommerce.discountservice.dto.response.DiscountCalculationResponse;
import com.ecommerce.discountservice.dto.response.UserCouponResponse;
import com.ecommerce.discountservice.enums.CouponStatus;
import com.ecommerce.discountservice.enums.CouponType;
import com.ecommerce.discountservice.enums.UserRole;
import com.ecommerce.discountservice.security.CustomUserDetails;
import com.ecommerce.discountservice.exception.DiscountDomainException;
import com.ecommerce.discountservice.exception.DiscountDomainExceptionCode;
import com.ecommerce.discountservice.security.jwt.JwtTokenProvider;
import com.ecommerce.discountservice.service.CouponService;
import com.ecommerce.discountservice.service.DiscountCalculationService;
import com.ecommerce.discountservice.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponControllerTest {

    private static final CustomUserDetails TEST_USER =
            new CustomUserDetails(100L, "user@test.com", UserRole.USER);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CouponService couponService;

    @MockBean
    DiscountCalculationService discountCalculationService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    TokenService tokenService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static RequestPostProcessor mockUser(CustomUserDetails user) {
        return request -> {
            var authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return request;
        };
    }

    @Nested
    @DisplayName("GET /api/coupons - 내 쿠폰 목록 조회")
    class GetMyCouponsTest {

        @Test
        @DisplayName("내 쿠폰 목록 조회 성공")
        void getMyCoupons_success() throws Exception {
            // given
            UserCouponResponse coupon1 = createUserCouponResponse(1L, 100L, "SAVE10");
            UserCouponResponse coupon2 = createUserCouponResponse(2L, 100L, "SAVE20");

            when(couponService.getUserCoupons(anyLong())).thenReturn(List.of(coupon1, coupon2));

            // when & then
            mockMvc.perform(get("/api/coupons").with(mockUser(TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("내 쿠폰 목록 조회 - 빈 목록")
        void getMyCoupons_empty() throws Exception {
            // given
            when(couponService.getUserCoupons(anyLong())).thenReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/coupons").with(mockUser(TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/coupons/available - 사용 가능한 쿠폰 조회")
    class GetAvailableCouponsTest {

        @Test
        @DisplayName("사용 가능한 쿠폰 목록 조회 성공")
        void getAvailableCoupons_success() throws Exception {
            // given
            UserCouponResponse availableCoupon = createUserCouponResponse(1L, 100L, "SAVE10");

            when(couponService.getAvailableUserCoupons(anyLong())).thenReturn(List.of(availableCoupon));

            // when & then
            mockMvc.perform(get("/api/coupons/available").with(mockUser(TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }
    }

    @Nested
    @DisplayName("POST /api/coupons/claim/{code} - 쿠폰 발급")
    class ClaimCouponTest {

        @Test
        @DisplayName("쿠폰 발급 성공")
        void claimCoupon_success() throws Exception {
            // given
            UserCouponResponse response = createUserCouponResponse(1L, 100L, "SAVE10");

            when(couponService.claimCoupon(anyLong(), anyString())).thenReturn(response);

            // when & then
            mockMvc.perform(post("/api/coupons/claim/SAVE10")
                            .with(mockUser(TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.couponCode").value("SAVE10"));
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 쿠폰 없음")
        void claimCoupon_notFound() throws Exception {
            // given
            when(couponService.claimCoupon(anyLong(), anyString()))
                    .thenThrow(new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));

            // when & then
            mockMvc.perform(post("/api/coupons/claim/INVALID")
                            .with(mockUser(TEST_USER)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 이미 발급받음")
        void claimCoupon_alreadyClaimed() throws Exception {
            // given
            when(couponService.claimCoupon(anyLong(), anyString()))
                    .thenThrow(new DiscountDomainException(DiscountDomainExceptionCode.UserCouponAlreadyClaimedException));

            // when & then
            mockMvc.perform(post("/api/coupons/claim/SAVE10")
                            .with(mockUser(TEST_USER)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("쿠폰 발급 실패 - 수량 소진")
        void claimCoupon_outOfStock() throws Exception {
            // given
            when(couponService.claimCoupon(anyLong(), anyString()))
                    .thenThrow(new DiscountDomainException(DiscountDomainExceptionCode.CouponOutOfStockException));

            // when & then
            mockMvc.perform(post("/api/coupons/claim/SOLDOUT")
                            .with(mockUser(TEST_USER)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/coupons/calculate - 할인 금액 계산")
    class CalculateDiscountTest {

        @Test
        @DisplayName("할인 금액 계산 성공")
        void calculateDiscount_success() throws Exception {
            // given
            DiscountCalculationResponse response = DiscountCalculationResponse.applicable(
                    1L, 10L, "10% 할인", new BigDecimal("50000"), new BigDecimal("5000")
            );

            when(discountCalculationService.calculateDiscount(anyLong(), any())).thenReturn(response);

            String requestBody = """
                    {
                        "userCouponId": 1,
                        "orderAmount": 50000
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/coupons/calculate")
                            .with(mockUser(TEST_USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isApplicable").value(true))
                    .andExpect(jsonPath("$.data.discountAmount").value(5000));
        }

        @Test
        @DisplayName("할인 금액 계산 - 적용 불가")
        void calculateDiscount_notApplicable() throws Exception {
            // given
            DiscountCalculationResponse response = DiscountCalculationResponse.notApplicable(
                    1L, 10L, new BigDecimal("5000"), "최소 주문 금액(10000원) 이상이어야 합니다."
            );

            when(discountCalculationService.calculateDiscount(anyLong(), any())).thenReturn(response);

            String requestBody = """
                    {
                        "userCouponId": 1,
                        "orderAmount": 5000
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/coupons/calculate")
                            .with(mockUser(TEST_USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isApplicable").value(false))
                    .andExpect(jsonPath("$.data.discountAmount").value(0));
        }
    }

    private UserCouponResponse createUserCouponResponse(Long id, Long userId, String couponCode) {
        return UserCouponResponse.builder()
                .id(id)
                .userId(userId)
                .couponId(10L)
                .couponCode(couponCode)
                .couponName("테스트 쿠폰")
                .couponType(CouponType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .status(CouponStatus.AVAILABLE)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .isAvailable(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
