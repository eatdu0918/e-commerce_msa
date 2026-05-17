package com.ecommerce.cancelservice.controller;

import com.ecommerce.cancelservice.config.SecurityConfig;
import com.ecommerce.cancelservice.dto.response.CancelItemResponse;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSummaryResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSyncResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.cancelservice.enums.CancelReason;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import com.ecommerce.cancelservice.enums.CancelStatus;
import com.ecommerce.common.enums.UserRole;
import com.ecommerce.cancelservice.exception.CancelDomainException;
import com.ecommerce.cancelservice.exception.CancelDomainExceptionCode;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.common.security.JwtAuthenticationFilter;
import com.ecommerce.cancelservice.service.CancelService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminCancelController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, SecurityConfig.class}))
@AutoConfigureMockMvc(addFilters = false)
class AdminCancelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CancelService cancelService;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(1L, "admin@test.com", "", UserRole.ADMIN);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("GET /api/admin/cancels - 전체 취소 목록 조회")
    class GetAllCancelsTest {

        @Test
        @DisplayName("전체 취소 목록 조회 성공")
        void getAllCancels_success() throws Exception {
            // given
            CancelResponse cancel = createCancelResponse(1L, CancelStatus.REQUESTED);

            PageResponse<CancelResponse> pageResponse = PageResponse.<CancelResponse>builder()
                    .content(List.of(cancel))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(cancelService.getAdminCancels(isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/cancels"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("상태별 취소 목록 조회 성공")
        void getAllCancels_withStatus() throws Exception {
            // given
            CancelResponse cancel = createCancelResponse(1L, CancelStatus.REQUESTED);

            PageResponse<CancelResponse> pageResponse = PageResponse.<CancelResponse>builder()
                    .content(List.of(cancel))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(cancelService.getAdminCancels(eq(CancelStatus.REQUESTED), isNull(), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/cancels")
                            .param("status", "REQUESTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].status").value("REQUESTED"));
        }

        @Test
        @DisplayName("요청 유형별 목록 조회 성공 (반품·환불)")
        void getAllCancels_withRequestType() throws Exception {
            CancelResponse cancel = createCancelResponse(1L, CancelStatus.REQUESTED, CancelRequestType.RETURN_REFUND);

            PageResponse<CancelResponse> pageResponse = PageResponse.<CancelResponse>builder()
                    .content(List.of(cancel))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(cancelService.getAdminCancels(isNull(), eq(CancelRequestType.RETURN_REFUND), any(Pageable.class)))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/admin/cancels")
                            .param("requestType", "RETURN_REFUND"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].requestType").value("RETURN_REFUND"));
        }

        @Test
        @DisplayName("전체 취소 목록 조회 - 빈 목록")
        void getAllCancels_empty() throws Exception {
            // given
            PageResponse<CancelResponse> pageResponse = PageResponse.<CancelResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(cancelService.getAdminCancels(isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/cancels"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/cancels/orders/{orderId}/active - 주문별 진행 중 취소 요약")
    class GetActiveCancelForOrderTest {

        @Test
        @DisplayName("진행 중 취소가 있으면 요약 반환")
        void getActiveCancel_found() throws Exception {
            OrderCancelSummaryResponse summary = OrderCancelSummaryResponse.builder()
                    .cancelId(1L)
                    .cancelNumber("CN-1")
                    .status(CancelStatus.APPROVED)
                    .requestType(CancelRequestType.RETURN_REFUND)
                    .build();
            when(cancelService.getActiveCancelForOrderAdmin(10L)).thenReturn(Optional.of(summary));

            mockMvc.perform(get("/api/admin/cancels/orders/10/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"))
                    .andExpect(jsonPath("$.data.requestType").value("RETURN_REFUND"));
        }

        @Test
        @DisplayName("진행 중 취소가 없으면 data 없음")
        void getActiveCancel_empty() throws Exception {
            when(cancelService.getActiveCancelForOrderAdmin(10L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/admin/cancels/orders/10/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("주문별 취소 동기화 (관리자)")
        void getCancelSync_ok() throws Exception {
            OrderCancelSyncResponse sync = OrderCancelSyncResponse.builder()
                    .activeCancel(null)
                    .hasRejectedOrderCancelRequest(true)
                    .hasRejectedReturnRefundRequest(false)
                    .build();
            when(cancelService.getCancelSyncForOrderAdmin(10L)).thenReturn(sync);

            mockMvc.perform(get("/api/admin/cancels/orders/10/sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.hasRejectedOrderCancelRequest").value(true))
                    .andExpect(jsonPath("$.data.hasRejectedReturnRefundRequest").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/cancels/{cancelId} - 취소 상세 조회")
    class GetCancelTest {

        @Test
        @DisplayName("취소 상세 조회 성공")
        void getCancel_success() throws Exception {
            // given
            CancelResponse response = createCancelResponse(1L, CancelStatus.REQUESTED);

            when(cancelService.getCancelById(1L)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/admin/cancels/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("REQUESTED"));
        }

        @Test
        @DisplayName("취소 상세 조회 실패 - 존재하지 않음")
        void getCancel_notFound() throws Exception {
            // given
            when(cancelService.getCancelById(999L))
                    .thenThrow(new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

            // when & then
            mockMvc.perform(get("/api/admin/cancels/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/cancels/{cancelId}/approve - 취소 승인")
    class ApproveCancelTest {

        @Test
        @DisplayName("취소 승인 성공")
        void approveCancel_success() throws Exception {
            // given
            CancelResponse response = createCancelResponse(1L, CancelStatus.APPROVED);

            when(cancelService.approveCancel(1L)).thenReturn(response);

            // when & then
            mockMvc.perform(put("/api/admin/cancels/1/approve"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("취소가 승인되었습니다."))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));
        }

        @Test
        @DisplayName("취소 승인 실패 - 존재하지 않음")
        void approveCancel_notFound() throws Exception {
            // given
            when(cancelService.approveCancel(999L))
                    .thenThrow(new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

            // when & then
            mockMvc.perform(put("/api/admin/cancels/999/approve"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("취소 승인 실패 - 요청 상태 아님")
        void approveCancel_notInRequestedStatus() throws Exception {
            // given
            when(cancelService.approveCancel(1L))
                    .thenThrow(new CancelDomainException(CancelDomainExceptionCode.CancelNotInRequestedStatusException));

            // when & then
            mockMvc.perform(put("/api/admin/cancels/1/approve"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/cancels/{cancelId}/reject - 취소 거부")
    class RejectCancelTest {

        @Test
        @DisplayName("취소 거부 성공")
        void rejectCancel_success() throws Exception {
            // given
            CancelResponse response = createCancelResponse(1L, CancelStatus.REJECTED);

            when(cancelService.rejectCancel(1L, "재고 부족")).thenReturn(response);

            String requestBody = """
                    {
                        "rejectedReason": "재고 부족"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/cancels/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("취소가 거부되었습니다."))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));
        }

        @Test
        @DisplayName("취소 거부 실패 - 필수 값 누락")
        void rejectCancel_validation_fail() throws Exception {
            // given
            String requestBody = """
                    {
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/cancels/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("취소 거부 실패 - 존재하지 않음")
        void rejectCancel_notFound() throws Exception {
            // given
            when(cancelService.rejectCancel(999L, "재고 부족"))
                    .thenThrow(new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

            String requestBody = """
                    {
                        "rejectedReason": "재고 부족"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/cancels/999/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("취소 거부 실패 - 요청 상태 아님")
        void rejectCancel_notInRequestedStatus() throws Exception {
            // given
            when(cancelService.rejectCancel(1L, "재고 부족"))
                    .thenThrow(new CancelDomainException(CancelDomainExceptionCode.CancelNotInRequestedStatusException));

            String requestBody = """
                    {
                        "rejectedReason": "재고 부족"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/cancels/1/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    private CancelResponse createCancelResponse(Long id, CancelStatus status) {
        return createCancelResponse(id, status, CancelRequestType.ORDER_CANCEL);
    }

    private CancelResponse createCancelResponse(Long id, CancelStatus status, CancelRequestType requestType) {
        CancelItemResponse item = CancelItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("테스트 상품")
                .quantity(2)
                .unitPrice(new BigDecimal("10000"))
                .totalPrice(new BigDecimal("20000"))
                .build();

        return CancelResponse.builder()
                .id(id)
                .orderId(100L)
                .orderNumber("ORD-TEST123")
                .userId(200L)
                .cancelNumber("CAN-ABC123")
                .status(status)
                .statusDescription(status.getDescription())
                .requestType(requestType)
                .requestTypeDescription(requestType.getDescription())
                .cancelReason(CancelReason.CHANGE_OF_MIND)
                .cancelReasonDescription(CancelReason.CHANGE_OF_MIND.getDescription())
                .cancelDetail("단순 변심으로 취소합니다.")
                .items(List.of(item))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
