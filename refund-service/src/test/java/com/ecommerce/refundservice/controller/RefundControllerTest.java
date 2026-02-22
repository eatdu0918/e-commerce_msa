package com.ecommerce.refundservice.controller;

import com.ecommerce.refundservice.config.SecurityConfig;
import com.ecommerce.refundservice.dto.response.PageResponse;
import com.ecommerce.refundservice.dto.response.RefundResponse;
import com.ecommerce.refundservice.enums.RefundReason;
import com.ecommerce.refundservice.enums.RefundStatus;
import com.ecommerce.refundservice.enums.UserRole;
import com.ecommerce.refundservice.exception.RefundDomainException;
import com.ecommerce.refundservice.exception.RefundDomainExceptionCode;
import com.ecommerce.refundservice.security.CustomUserDetails;
import com.ecommerce.refundservice.security.jwt.JwtAuthenticationFilter;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = RefundController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, SecurityConfig.class}))
@AutoConfigureMockMvc(addFilters = false)
class RefundControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RefundService refundService;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(400L, "test@test.com", UserRole.USER);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("GET /api/refunds - 내 환불 목록 조회")
    class GetMyRefundsTest {

        @Test
        @DisplayName("내 환불 목록 조회 성공")
        void getMyRefunds_success() throws Exception {
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

            when(refundService.getMyRefunds(anyLong(), any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/refunds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("내 환불 목록 조회 - 빈 목록")
        void getMyRefunds_empty() throws Exception {
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

            when(refundService.getMyRefunds(anyLong(), any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/refunds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/refunds/{refundId} - 환불 상세 조회")
    class GetRefundTest {

        @Test
        @DisplayName("환불 상세 조회 성공")
        void getRefund_success() throws Exception {
            // given
            RefundResponse response = createRefundResponse(1L, RefundStatus.COMPLETED);

            when(refundService.getRefund(anyLong(), anyLong())).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/refunds/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("환불 상세 조회 실패 - 존재하지 않음")
        void getRefund_notFound() throws Exception {
            // given
            when(refundService.getRefund(anyLong(), anyLong()))
                    .thenThrow(new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));

            // when & then
            mockMvc.perform(get("/api/refunds/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/refunds/cancel/{cancelId} - 취소별 환불 조회")
    class GetRefundByCancelIdTest {

        @Test
        @DisplayName("취소별 환불 조회 성공")
        void getRefundByCancelId_success() throws Exception {
            // given
            RefundResponse response = createRefundResponse(1L, RefundStatus.COMPLETED);

            when(refundService.getRefundByCancelId(anyLong(), anyLong())).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/refunds/cancel/200"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.cancelId").value(200));
        }

        @Test
        @DisplayName("취소별 환불 조회 실패 - 존재하지 않음")
        void getRefundByCancelId_notFound() throws Exception {
            // given
            when(refundService.getRefundByCancelId(anyLong(), anyLong()))
                    .thenThrow(new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));

            // when & then
            mockMvc.perform(get("/api/refunds/cancel/999"))
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
