package com.ecommerce.discountservice.controller;

import com.ecommerce.discountservice.dto.response.BulkGrantCouponResponse;
import com.ecommerce.discountservice.dto.response.CouponResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.discountservice.enums.CouponType;
import com.ecommerce.discountservice.exception.DiscountDomainException;
import com.ecommerce.discountservice.exception.DiscountDomainExceptionCode;
import com.ecommerce.common.security.JwtTokenProvider;
import com.ecommerce.discountservice.service.CouponService;
import com.ecommerce.common.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCouponControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CouponService couponService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    TokenService tokenService;

    @Nested
    @DisplayName("POST /api/admin/coupons - ?   ???  ")
    class CreateCouponTest {

        @Test
        @DisplayName("?   ???   ?   ")
        void createCoupon_success() throws Exception {
            // given
            CouponResponse response = createCouponResponse(1L, "SAVE10", "10% ?   ");

            when(couponService.createCoupon(any())).thenReturn(response);

            String requestBody = """
                    {
                        "code": "SAVE10",
                        "name": "10% ?   ",
                        "description": "???  ? 10% ?   ",
                        "couponType": "PERCENTAGE",
                        "discountValue": 10,
                        "minOrderAmount": 10000,
                        "maxDiscountAmount": 5000,
                        "totalQuantity": 100,
                        "validFrom": "2024-01-01T00:00:00",
                        "validUntil": "2024-12-31T23:59:59"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/admin/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("SAVE10"));
        }

        @Test
        @DisplayName("?   ???   ??   -    ???   ?)
        void createCoupon_duplicateCode() throws Exception {
            // given
            when(couponService.createCoupon(any()))
                    .thenThrow(new DiscountDomainException(DiscountDomainExceptionCode.CouponAlreadyExistsException));

            String requestBody = """
                    {
                        "code": "SAVE10",
                        "name": "10% ?   ",
                        "couponType": "PERCENTAGE",
                        "discountValue": 10,
                        "validFrom": "2024-01-01T00:00:00",
                        "validUntil": "2024-12-31T23:59:59"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/admin/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("?   ???   ??   - ?     ??   ")
        void createCoupon_validation_fail() throws Exception {
            // given
            String requestBody = """
                    {
                        "name": "10% ?   "
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/admin/coupons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/coupons/bulk-grant - ?   ???      ??)
    class BulkGrantCouponTest {

        @Test
        @DisplayName("??      ???   ")
        void bulkGrantCoupon_success() throws Exception {
            BulkGrantCouponResponse response = BulkGrantCouponResponse.builder()
                    .couponCode("WELCOME10")
                    .grantedCount(2)
                    .skippedCount(0)
                    .build();

            when(couponService.bulkGrantCoupon(any())).thenReturn(response);

            String requestBody = """
                    {
                        "couponCode": "WELCOME10",
                        "userIds": [1, 2]
                    }
                    """;

            mockMvc.perform(post("/api/admin/coupons/bulk-grant")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.couponCode").value("WELCOME10"))
                    .andExpect(jsonPath("$.data.grantedCount").value(2))
                    .andExpect(jsonPath("$.data.skippedCount").value(0));
        }

        @Test
        @DisplayName("??      ????   - userIds ??  ???  ")
        void bulkGrantCoupon_validation_emptyUserIds() throws Exception {
            String requestBody = """
                    {
                        "couponCode": "WELCOME10",
                        "userIds": []
                    }
                    """;

            mockMvc.perform(post("/api/admin/coupons/bulk-grant")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/coupons - ?    ?   ?    ?   ??)
    class GetAllCouponsTest {

        @Test
        @DisplayName("?    ?   ?    ?   ???   ")
        void getAllCoupons_success() throws Exception {
            // given
            CouponResponse coupon1 = createCouponResponse(1L, "SAVE10", "10% ?   ");
            CouponResponse coupon2 = createCouponResponse(2L, "SAVE3000", "3000???   ");

            PageResponse<CouponResponse> pageResponse = PageResponse.<CouponResponse>builder()
                    .content(List.of(coupon1, coupon2))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(2L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(couponService.getAllCoupons(any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/coupons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/coupons/{couponId} - ?   ??       ??)
    class GetCouponTest {

        @Test
        @DisplayName("?   ??       ???   ")
        void getCoupon_success() throws Exception {
            // given
            CouponResponse response = createCouponResponse(1L, "SAVE10", "10% ?   ");

            when(couponService.getCoupon(1L)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/admin/coupons/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.code").value("SAVE10"));
        }

        @Test
        @DisplayName("?   ??       ????   -    ???? ??  ")
        void getCoupon_notFound() throws Exception {
            // given
            when(couponService.getCoupon(999L))
                    .thenThrow(new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));

            // when & then
            mockMvc.perform(get("/api/admin/coupons/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/coupons/{couponId} - ?   ???  ")
    class UpdateCouponTest {

        @Test
        @DisplayName("?   ???   ?   ")
        void updateCoupon_success() throws Exception {
            // given
            CouponResponse response = createCouponResponse(1L, "SAVE10", "??  ??  ???   ?);

            when(couponService.updateCoupon(anyLong(), any())).thenReturn(response);

            String requestBody = """
                    {
                        "name": "??  ??  ???   ?,
                        "description": "??  ????  ",
                        "couponType": "PERCENTAGE",
                        "discountValue": 15,
                        "minOrderAmount": 15000,
                        "maxDiscountAmount": 7000,
                        "totalQuantity": 200,
                        "validFrom": "2024-01-01T00:00:00",
                        "validUntil": "2024-12-31T23:59:59",
                        "isActive": true
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/coupons/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("?   ???   ??   -    ???? ??  ")
        void updateCoupon_notFound() throws Exception {
            // given
            when(couponService.updateCoupon(anyLong(), any()))
                    .thenThrow(new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException));

            String requestBody = """
                    {
                        "name": "??  ??  ???   ?,
                        "couponType": "PERCENTAGE",
                        "discountValue": 15,
                        "validFrom": "2024-01-01T00:00:00",
                        "validUntil": "2024-12-31T23:59:59",
                        "isActive": true
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/coupons/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/coupons/{couponId} - ?   ?????)
    class DeleteCouponTest {

        @Test
        @DisplayName("?   ??????   ")
        void deleteCoupon_success() throws Exception {
            // given
            doNothing().when(couponService).deleteCoupon(1L);

            // when & then
            mockMvc.perform(delete("/api/admin/coupons/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(couponService).deleteCoupon(1L);
        }

        @Test
        @DisplayName("?   ???????   -    ???? ??  ")
        void deleteCoupon_notFound() throws Exception {
            // given
            doThrow(new DiscountDomainException(DiscountDomainExceptionCode.CouponNotFoundException))
                    .when(couponService).deleteCoupon(999L);

            // when & then
            mockMvc.perform(delete("/api/admin/coupons/999"))
                    .andExpect(status().isNotFound());
        }
    }

    private CouponResponse createCouponResponse(Long id, String code, String name) {
        return CouponResponse.builder()
                .id(id)
                .code(code)
                .name(name)
                .description("??? ???   ?)
                .couponType(CouponType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .minOrderAmount(new BigDecimal("10000"))
                .maxDiscountAmount(new BigDecimal("5000"))
                .totalQuantity(100)
                .issuedQuantity(0)
                .remainingQuantity(100)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .isActive(true)
                .isValid(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
