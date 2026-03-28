package com.ecommerce.cancelservice.controller;

import com.ecommerce.cancelservice.config.SecurityConfig;
import com.ecommerce.cancelservice.dto.response.CancelItemResponse;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSummaryResponse;
import com.ecommerce.cancelservice.dto.response.PageResponse;
import com.ecommerce.cancelservice.enums.CancelReason;
import com.ecommerce.cancelservice.enums.CancelStatus;
import com.ecommerce.cancelservice.enums.UserRole;
import com.ecommerce.cancelservice.exception.CancelDomainException;
import com.ecommerce.cancelservice.exception.CancelDomainExceptionCode;
import com.ecommerce.cancelservice.security.CustomUserDetails;
import com.ecommerce.cancelservice.security.jwt.JwtAuthenticationFilter;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CancelController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, SecurityConfig.class}))
@AutoConfigureMockMvc(addFilters = false)
class CancelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CancelService cancelService;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(200L, "test@test.com", UserRole.USER);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("POST /api/cancels - 취소 요청 생성")
    class CreateCancelTest {

        @Test
        @DisplayName("취소 요청 생성 성공")
        void createCancel_success() throws Exception {
            // given
            CancelResponse response = createCancelResponse(1L, CancelStatus.REQUESTED);

            when(cancelService.createCancel(anyLong(), any())).thenReturn(response);

            String requestBody = """
                    {
                        "orderId": 100,
                        "orderNumber": "ORD-TEST123",
                        "cancelReason": "CHANGE_OF_MIND",
                        "cancelDetail": "단순 변심으로 취소합니다.",
                        "items": [
                            {
                                "productId": 1,
                                "productName": "테스트 상품",
                                "quantity": 2,
                                "unitPrice": 10000
                            }
                        ]
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/cancels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("취소 요청이 생성되었습니다."))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("REQUESTED"));
        }

        @Test
        @DisplayName("주문별 진행 중 취소 요약 - 있을 때")
        void getActiveCancelForOrder_found() throws Exception {
            OrderCancelSummaryResponse summary = OrderCancelSummaryResponse.builder()
                    .cancelId(10L)
                    .cancelNumber("CAN-X")
                    .status(CancelStatus.REQUESTED)
                    .build();
            when(cancelService.getActiveCancelForOrder(100L, 200L)).thenReturn(Optional.of(summary));

            mockMvc.perform(get("/api/cancels/by-order/100/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.cancelId").value(10))
                    .andExpect(jsonPath("$.data.status").value("REQUESTED"));
        }

        @Test
        @DisplayName("주문별 진행 중 취소 요약 - 없을 때")
        void getActiveCancelForOrder_empty() throws Exception {
            when(cancelService.getActiveCancelForOrder(100L, 200L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/cancels/by-order/100/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("취소 요청 생성 실패 - 필수 값 누락 (orderId)")
        void createCancel_validation_fail_noOrderId() throws Exception {
            // given
            String requestBody = """
                    {
                        "orderNumber": "ORD-TEST123",
                        "cancelReason": "CHANGE_OF_MIND",
                        "items": [
                            {
                                "productId": 1,
                                "productName": "테스트 상품",
                                "quantity": 2,
                                "unitPrice": 10000
                            }
                        ]
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/cancels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("취소 요청 생성 실패 - 빈 상품 목록")
        void createCancel_validation_fail_emptyItems() throws Exception {
            // given
            String requestBody = """
                    {
                        "orderId": 100,
                        "orderNumber": "ORD-TEST123",
                        "cancelReason": "CHANGE_OF_MIND",
                        "items": []
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/cancels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("취소 요청 생성 실패 - 상품 유효성 검증 실패")
        void createCancel_validation_fail_invalidItem() throws Exception {
            // given
            String requestBody = """
                    {
                        "orderId": 100,
                        "orderNumber": "ORD-TEST123",
                        "cancelReason": "CHANGE_OF_MIND",
                        "items": [
                            {
                                "productId": 1,
                                "productName": "",
                                "quantity": 0,
                                "unitPrice": -100
                            }
                        ]
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/cancels")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/cancels - 내 취소 목록 조회")
    class GetMyCancelsTest {

        @Test
        @DisplayName("내 취소 목록 조회 성공")
        void getMyCancels_success() throws Exception {
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

            when(cancelService.getMyCancels(anyLong(), any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/cancels"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("내 취소 목록 조회 - 빈 목록")
        void getMyCancels_empty() throws Exception {
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

            when(cancelService.getMyCancels(anyLong(), any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/cancels"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/cancels/{cancelId} - 취소 상세 조회")
    class GetCancelTest {

        @Test
        @DisplayName("취소 상세 조회 성공")
        void getCancel_success() throws Exception {
            // given
            CancelResponse response = createCancelResponse(1L, CancelStatus.REQUESTED);

            when(cancelService.getCancel(anyLong(), anyLong())).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/cancels/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("REQUESTED"));
        }

        @Test
        @DisplayName("취소 상세 조회 실패 - 존재하지 않음")
        void getCancel_notFound() throws Exception {
            // given
            when(cancelService.getCancel(anyLong(), anyLong()))
                    .thenThrow(new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

            // when & then
            mockMvc.perform(get("/api/cancels/999"))
                    .andExpect(status().isNotFound());
        }
    }

    private CancelResponse createCancelResponse(Long id, CancelStatus status) {
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
                .cancelReason(CancelReason.CHANGE_OF_MIND)
                .cancelReasonDescription(CancelReason.CHANGE_OF_MIND.getDescription())
                .cancelDetail("단순 변심으로 취소합니다.")
                .items(List.of(item))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
