package com.ecommerce.paymentservice.controller;

import com.ecommerce.common.response.PageResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.exception.PaymentDomainException;
import com.ecommerce.paymentservice.exception.PaymentDomainExceptionCode;
import com.ecommerce.common.security.JwtTokenProvider;
import com.ecommerce.paymentservice.service.PaymentService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminPaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PaymentService paymentService;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    TokenService tokenService;

    @Nested
    @DisplayName("GET /api/admin/payments - 전체 결제 목록 조회")
    class GetAllPaymentsTest {

        @Test
        @DisplayName("전체 결제 목록 조회 성공")
        void getAllPayments_success() throws Exception {
            // given
            PaymentResponse payment1 = createPaymentResponse(1L, 100L, PaymentStatus.COMPLETED);
            PaymentResponse payment2 = createPaymentResponse(2L, 101L, PaymentStatus.PENDING);

            PageResponse<PaymentResponse> pageResponse = PageResponse.<PaymentResponse>builder()
                    .content(List.of(payment1, payment2))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(2L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(paymentService.getAllPayments(any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/payments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("상태별 결제 목록 조회")
        void getPaymentsByStatus_success() throws Exception {
            // given
            PaymentResponse completedPayment = createPaymentResponse(1L, 100L, PaymentStatus.COMPLETED);

            PageResponse<PaymentResponse> pageResponse = PageResponse.<PaymentResponse>builder()
                    .content(List.of(completedPayment))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(paymentService.getPaymentsByStatus(any(PaymentStatus.class), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/payments")
                            .param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/payments/{paymentId} - 결제 상세 조회")
    class GetPaymentTest {

        @Test
        @DisplayName("결제 상세 조회 성공")
        void getPayment_success() throws Exception {
            // given
            PaymentResponse response = createPaymentResponse(1L, 100L, PaymentStatus.COMPLETED);

            when(paymentService.getPaymentById(1L)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/admin/payments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("결제 상세 조회 실패 - 존재하지 않음")
        void getPayment_notFound() throws Exception {
            // given
            when(paymentService.getPaymentById(999L))
                    .thenThrow(new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));

            // when & then
            mockMvc.perform(get("/api/admin/payments/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/payments/{paymentId}/status - 결제 상태 변경")
    class UpdatePaymentStatusTest {

        @Test
        @DisplayName("결제 상태 변경 성공 - COMPLETED")
        void updatePaymentStatus_toCompleted_success() throws Exception {
            // given
            PaymentResponse response = createPaymentResponse(1L, 100L, PaymentStatus.COMPLETED);

            when(paymentService.updatePaymentStatus(anyLong(), any(PaymentStatus.class))).thenReturn(response);

            String requestBody = """
                    {
                        "status": "COMPLETED"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/payments/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("결제 상태 변경 성공 - CANCELLED")
        void updatePaymentStatus_toCancelled_success() throws Exception {
            // given
            PaymentResponse response = createPaymentResponse(1L, 100L, PaymentStatus.CANCELLED);

            when(paymentService.updatePaymentStatus(anyLong(), any(PaymentStatus.class))).thenReturn(response);

            String requestBody = """
                    {
                        "status": "CANCELLED"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/payments/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("결제 상태 변경 실패 - 존재하지 않음")
        void updatePaymentStatus_notFound() throws Exception {
            // given
            when(paymentService.updatePaymentStatus(anyLong(), any(PaymentStatus.class)))
                    .thenThrow(new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));

            String requestBody = """
                    {
                        "status": "COMPLETED"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/payments/999/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }

    private PaymentResponse createPaymentResponse(Long id, Long orderId, PaymentStatus status) {
        return PaymentResponse.builder()
                .id(id)
                .orderId(orderId)
                .orderNumber("ORD-ABC123")
                .userId(200L)
                .paymentNumber("PAY-TEST123456")
                .status(status)
                .statusDescription(status.getDescription())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentMethodDescription(PaymentMethod.CREDIT_CARD.getDescription())
                .amount(new BigDecimal("50000"))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
