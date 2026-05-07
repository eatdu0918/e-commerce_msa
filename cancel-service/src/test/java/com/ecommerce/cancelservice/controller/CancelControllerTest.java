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
    @DisplayName("POST /api/cancels - ?  ???    ??  ")
    class CreateCancelTest {

        @Test
        @DisplayName("?  ???    ??   ?   ")
        void createCancel_success() throws Exception {
            // given
            CancelResponse response = createCancelResponse(1L, CancelStatus.REQUESTED);

            when(cancelService.createCancel(anyLong(), any())).thenReturn(response);

            String requestBody = """
                    {
                        "orderId": 100,
                        "orderNumber": "ORD-TEST123",
                        "cancelReason": "CHANGE_OF_MIND",
                        "cancelDetail": "??      ???  ??  ???  ??",
                        "items": [
                            {
                                "productId": 1,
                                "productName": "??? ???  ?",
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
                    .andExpect(jsonPath("$.message").value("?  ???   ????  ?? ???  ??"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("REQUESTED"));
        }

        @Test
        @DisplayName("     ?    ? ??  ???    - ??   ??)
        void getActiveCancelForOrder_found() throws Exception {
            OrderCancelSummaryResponse summary = OrderCancelSummaryResponse.builder()
                    .cancelId(10L)
                    .cancelNumber("CAN-X")
                    .status(CancelStatus.REQUESTED)
                    .requestType(CancelRequestType.ORDER_CANCEL)
                    .build();
            when(cancelService.getActiveCancelForOrder(100L, 200L)).thenReturn(Optional.of(summary));

            mockMvc.perform(get("/api/cancels/by-order/100/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.cancelId").value(10))
                    .andExpect(jsonPath("$.data.status").value("REQUESTED"))
                    .andExpect(jsonPath("$.data.requestType").value("ORDER_CANCEL"));
        }

        @Test
        @DisplayName("     ?    ? ??  ???    - ??   ??)
        void getActiveCancelForOrder_empty() throws Exception {
            when(cancelService.getActiveCancelForOrder(100L, 200L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/cancels/by-order/100/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("     ??  ????  ??)
        void getCancelSyncForOrder_ok() throws Exception {
            OrderCancelSummaryResponse active = OrderCancelSummaryResponse.builder()
                    .cancelId(10L)
                    .cancelNumber("CAN-X")
                    .status(CancelStatus.REQUESTED)
                    .requestType(CancelRequestType.ORDER_CANCEL)
                    .build();
            OrderCancelSyncResponse sync = OrderCancelSyncResponse.builder()
                    .activeCancel(active)
                    .hasRejectedOrderCancelRequest(false)
                    .hasRejectedReturnRefundRequest(true)
                    .build();
            when(cancelService.getCancelSyncForOrder(100L, 200L)).thenReturn(sync);

            mockMvc.perform(get("/api/cancels/by-order/100/sync"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.activeCancel.cancelId").value(10))
                    .andExpect(jsonPath("$.data.hasRejectedOrderCancelRequest").value(false))
                    .andExpect(jsonPath("$.data.hasRejectedReturnRefundRequest").value(true));
        }

        @Test
        @DisplayName("?  ???    ??   ??   - ?     ??    (orderId)")
        void createCancel_validation_fail_noOrderId() throws Exception {
            // given
            String requestBody = """
                    {
                        "orderNumber": "ORD-TEST123",
                        "cancelReason": "CHANGE_OF_MIND",
                        "items": [
                            {
                                "productId": 1,
                                "productName": "??? ???  ?",
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
        @DisplayName("?  ???    ??   ??   - ???  ?     ?)
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
        @DisplayName("?  ???    ??   ??   - ?  ? ?   ??    ???  ")
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
    @DisplayName("GET /api/cancels - ???  ??    ?   ??)
    class GetMyCancelsTest {

        @Test
        @DisplayName("???  ??    ?   ???   ")
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
        @DisplayName("???  ??    ?   ??- ??    ?)
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
    @DisplayName("GET /api/cancels/{cancelId} - ?  ???       ??)
    class GetCancelTest {

        @Test
        @DisplayName("?  ???       ???   ")
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
        @DisplayName("?  ???       ????   -    ???? ??  ")
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
                .productName("??? ???  ?")
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
                .requestType(CancelRequestType.ORDER_CANCEL)
                .requestTypeDescription(CancelRequestType.ORDER_CANCEL.getDescription())
                .cancelReason(CancelReason.CHANGE_OF_MIND)
                .cancelReasonDescription(CancelReason.CHANGE_OF_MIND.getDescription())
                .cancelDetail("??      ???  ??  ???  ??")
                .items(List.of(item))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
