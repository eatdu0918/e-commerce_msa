package com.ecommerce.refundservice.controller;

import com.ecommerce.refundservice.config.SecurityConfig;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.refundservice.dto.response.RefundResponse;
import com.ecommerce.refundservice.enums.RefundReason;
import com.ecommerce.refundservice.enums.RefundStatus;
import com.ecommerce.common.enums.UserRole;
import com.ecommerce.refundservice.exception.RefundDomainException;
import com.ecommerce.refundservice.exception.RefundDomainExceptionCode;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.common.security.JwtAuthenticationFilter;
import com.ecommerce.refundservice.service.RefundService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminRefundController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, SecurityConfig.class}))
@AutoConfigureMockMvc(addFilters = false)
class AdminRefundControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RefundService refundService;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "admin@test.com", "", UserRole.ADMIN);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("GET /api/admin/refunds - 전체 환불 목록 조회")
    class GetAllRefundsTest {

        @Test
        @DisplayName("전체 환불 목록 조회 성공")
        void getAllRefunds_success() throws Exception {
            // given
            RefundResponse refund = createRefundResponse(1L, RefundStatus.COMPLETED);

            PageResponse<RefundResponse> pageResponse = PageResponse.<RefundResponse>builder()
                    .content(List.of(refund))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(refundService.getAllRefunds(any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/refunds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("상태별 환불 목록 조회 성공")
        void getAllRefunds_withStatus() throws Exception {
            // given
            RefundResponse refund = createRefundResponse(1L, RefundStatus.PENDING);

            PageResponse<RefundResponse> pageResponse = PageResponse.<RefundResponse>builder()
                    .content(List.of(refund))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(refundService.getRefundsByStatus(eq(RefundStatus.PENDING), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/refunds")
                            .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("전체 환불 목록 조회 - 빈 목록")
        void getAllRefunds_empty() throws Exception {
            // given
            PageResponse<RefundResponse> pageResponse = PageResponse.<RefundResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(refundService.getAllRefunds(any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/refunds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/refunds/{refundId} - 환불 상세 조회")
    class GetRefundTest {

        @Test
        @DisplayName("환불 상세 조회 성공")
        void getRefund_success() throws Exception {
            // given
            RefundResponse response = createRefundResponse(1L, RefundStatus.COMPLETED);

            when(refundService.getRefundById(1L)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/admin/refunds/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("환불 상세 조회 실패 - 존재하지 않음")
        void getRefund_notFound() throws Exception {
            // given
            when(refundService.getRefundById(999L))
                    .thenThrow(new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));

            // when & then
            mockMvc.perform(get("/api/admin/refunds/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/refunds/{refundId}/retry - 환불 재시도")
    class RetryRefundTest {

        @Test
        @DisplayName("환불 재시도 성공")
        void retryRefund_success() throws Exception {
            // given
            RefundResponse response = createRefundResponse(1L, RefundStatus.COMPLETED);

            when(refundService.retryRefund(1L)).thenReturn(response);

            // when & then
            mockMvc.perform(put("/api/admin/refunds/1/retry"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("환불 재시도가 요청되었습니다."))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("환불 재시도 실패 - 존재하지 않음")
        void retryRefund_notFound() throws Exception {
            // given
            when(refundService.retryRefund(999L))
                    .thenThrow(new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));

            // when & then
            mockMvc.perform(put("/api/admin/refunds/999/retry"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("환불 재시도 실패 - 유효하지 않은 상태")
        void retryRefund_invalidStatus() throws Exception {
            // given
            when(refundService.retryRefund(1L))
                    .thenThrow(new RefundDomainException(RefundDomainExceptionCode.InvalidRefundStatusException));

            // when & then
            mockMvc.perform(put("/api/admin/refunds/1/retry"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/refunds/{refundId}/status - 환불 상태 변경")
    class UpdateRefundStatusTest {

        @Test
        @DisplayName("환불 상태 변경 성공")
        void updateRefundStatus_success() throws Exception {
            // given
            RefundResponse response = createRefundResponse(1L, RefundStatus.PROCESSING);

            when(refundService.updateRefundStatus(1L, RefundStatus.PROCESSING)).thenReturn(response);

            String requestBody = """
                    {
                        "status": "PROCESSING"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/refunds/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("환불 상태가 변경되었습니다."))
                    .andExpect(jsonPath("$.data.status").value("PROCESSING"));
        }

        @Test
        @DisplayName("환불 상태 변경 실패 - 필수 값 누락")
        void updateRefundStatus_validation_fail() throws Exception {
            // given
            String requestBody = """
                    {
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/refunds/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("환불 상태 변경 실패 - 존재하지 않음")
        void updateRefundStatus_notFound() throws Exception {
            // given
            when(refundService.updateRefundStatus(999L, RefundStatus.PROCESSING))
                    .thenThrow(new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));

            String requestBody = """
                    {
                        "status": "PROCESSING"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/refunds/999/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    private RefundResponse createRefundResponse(Long id, RefundStatus status) {
        return RefundResponse.builder()
                .id(id)
                .orderId(100L)
                .cancelId(200L)
                .paymentId(300L)
                .userId(400L)
                .refundNumber("REF-ABC123")
                .status(status)
                .statusDescription(status.getDescription())
                .refundReason(RefundReason.ORDER_CANCEL)
                .refundReasonDescription(RefundReason.ORDER_CANCEL.getDescription())
                .refundDetail("주문 취소로 인한 환불")
                .amount(new BigDecimal("50000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
