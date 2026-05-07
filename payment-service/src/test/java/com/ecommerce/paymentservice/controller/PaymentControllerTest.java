package com.ecommerce.paymentservice.controller;

import com.ecommerce.common.response.PageResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.common.enums.UserRole;
import com.ecommerce.paymentservice.exception.PaymentDomainException;
import com.ecommerce.paymentservice.exception.PaymentDomainExceptionCode;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.common.security.JwtTokenProvider;
import com.ecommerce.paymentservice.service.PaymentService;
import com.ecommerce.common.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    private static final CustomUserDetails TEST_USER =
            new CustomUserDetails(200L, "user@test.com", UserRole.USER);

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
    @DisplayName("POST /api/payments -    ????  ")
    class CreatePaymentTest {

        @Test
        @DisplayName("   ????   ?   ")
        void createPayment_success() throws Exception {
            // given
            PaymentResponse response = createPaymentResponse(1L, 100L, PaymentStatus.PENDING);

            when(paymentService.createPayment(anyLong(), any())).thenReturn(response);

            String requestBody = """
                    {
                        "orderId": 100,
                        "orderNumber": "ORD-ABC123",
                        "paymentMethod": "CREDIT_CARD",
                        "amount": 50000
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/payments")
                            .with(mockUser(TEST_USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value(100))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("   ????   ??   - ?     ??   ")
        void createPayment_validation_fail() throws Exception {
            // given
            String requestBody = """
                    {
                        "orderId": 100
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/payments")
                            .with(mockUser(TEST_USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/payments - ??   ??    ?   ??)
    class GetMyPaymentsTest {

        @Test
        @DisplayName("??   ??    ?   ???   ")
        void getMyPayments_success() throws Exception {
            // given
            PaymentResponse payment = createPaymentResponse(1L, 100L, PaymentStatus.COMPLETED);

            PageResponse<PaymentResponse> pageResponse = PageResponse.<PaymentResponse>builder()
                    .content(List.of(payment))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(paymentService.getMyPayments(anyLong(), any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/payments").with(mockUser(TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/payments/{paymentId} -    ???       ??)
    class GetPaymentTest {

        @Test
        @DisplayName("   ???       ???   ")
        void getPayment_success() throws Exception {
            // given
            PaymentResponse response = createPaymentResponse(1L, 100L, PaymentStatus.COMPLETED);

            when(paymentService.getPayment(1L, 200L)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/payments/1").with(mockUser(TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("   ???       ????   -    ???? ??  ")
        void getPayment_notFound() throws Exception {
            // given
            when(paymentService.getPayment(999L, 200L))
                    .thenThrow(new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));

            // when & then
            mockMvc.perform(get("/api/payments/999").with(mockUser(TEST_USER)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/payments/order/{orderId} -      ?   ??   ??)
    class GetPaymentByOrderIdTest {

        @Test
        @DisplayName("     ?   ??   ???   ")
        void getPaymentByOrderId_success() throws Exception {
            // given
            PaymentResponse response = createPaymentResponse(1L, 100L, PaymentStatus.COMPLETED);

            when(paymentService.getPaymentByOrderId(100L, 200L)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/payments/order/100").with(mockUser(TEST_USER)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value(100));
        }

        @Test
        @DisplayName("     ?   ??   ????   - ??  ")
        void getPaymentByOrderId_notFound() throws Exception {
            // given
            when(paymentService.getPaymentByOrderId(999L, 200L))
                    .thenThrow(new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));

            // when & then
            mockMvc.perform(get("/api/payments/order/999").with(mockUser(TEST_USER)))
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
