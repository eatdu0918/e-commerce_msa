package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.config.SecurityConfig;
import com.ecommerce.orderservice.dto.response.OrderDetailResponse;
import com.ecommerce.orderservice.dto.response.OrderItemResponse;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.common.enums.UserRole;
import com.ecommerce.orderservice.exception.OrderDomainException;
import com.ecommerce.orderservice.exception.OrderDomainExceptionCode;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.common.security.JwtAuthenticationFilter;
import com.ecommerce.orderservice.service.OrderAggregationService;
import com.ecommerce.orderservice.service.OrderService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, SecurityConfig.class}))
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderService orderService;

    @MockBean
    OrderAggregationService orderAggregationService;

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(100L, "test@test.com", UserRole.USER);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("POST /api/orders -      ??  ")
    class CreateOrderTest {

        @Test
        @DisplayName("     ??   ?   ")
        void createOrder_success() throws Exception {
            // given
            OrderResponse response = createOrderResponse(1L, OrderStatus.PENDING);

            when(orderService.createOrder(anyLong(), any())).thenReturn(response);

            String requestBody = """
                    {
                        "items": [
                            {
                                "productId": 1,
                                "productName": "??? ???  ?",
                                "unitPrice": 10000,
                                "quantity": 2
                            }
                        ],
                        "shippingAddress": "??  ??      ?,
                        "recipientName": "??  ??,
                        "recipientPhone": "010-1234-5678"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("    ????  ?? ???  ??"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("     ??   ??   - ?     ??    (items)")
        void createOrder_validation_fail_emptyItems() throws Exception {
            // given
            String requestBody = """
                    {
                        "items": [],
                        "shippingAddress": "??  ??      ?,
                        "recipientName": "??  ??,
                        "recipientPhone": "010-1234-5678"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("     ??   ??   - ?     ??    (   ?  ?)")
        void createOrder_validation_fail_noAddress() throws Exception {
            // given
            String requestBody = """
                    {
                        "items": [
                            {
                                "productId": 1,
                                "productName": "??? ???  ?",
                                "unitPrice": 10000,
                                "quantity": 2
                            }
                        ],
                        "recipientName": "??  ??,
                        "recipientPhone": "010-1234-5678"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("     ??   ??   - ?  ? ?   ??    ???  ")
        void createOrder_validation_fail_invalidItem() throws Exception {
            // given
            String requestBody = """
                    {
                        "items": [
                            {
                                "productId": 1,
                                "productName": "",
                                "unitPrice": -100,
                                "quantity": 0
                            }
                        ],
                        "shippingAddress": "??  ??      ?,
                        "recipientName": "??  ??,
                        "recipientPhone": "010-1234-5678"
                    }
                    """;

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("     ??   ??   -    ????  ???   ???   ?   ")
        void createOrder_validation_fail_skipShippingWithoutSkipConfirm() throws Exception {
            String requestBody = """
                    {
                        "items": [
                            {
                                "productId": 1,
                                "productName": "??? ???  ?",
                                "unitPrice": 10000,
                                "quantity": 1
                            }
                        ],
                        "shippingAddress": "??  ??      ?,
                        "recipientName": "??  ??,
                        "recipientPhone": "010-1234-5678",
                        "skipConfirmAndPreparing": false,
                        "skipShippingAndDelivered": true
                    }
                    """;

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/orders - ??         ?   ??)
    class GetMyOrdersTest {

        @Test
        @DisplayName("??         ?   ???   ")
        void getMyOrders_success() throws Exception {
            // given
            OrderResponse order = createOrderResponse(1L, OrderStatus.CONFIRMED);

            PageResponse<OrderResponse> pageResponse = PageResponse.<OrderResponse>builder()
                    .content(List.of(order))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(orderAggregationService.getMyOrders(anyLong(), any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("??         ?   ???    - ??    ?)
        void getMyOrders_empty() throws Exception {
            // given
            PageResponse<OrderResponse> pageResponse = PageResponse.<OrderResponse>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(0L)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();

            when(orderAggregationService.getMyOrders(anyLong(), any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{orderId} -      ?       ??)
    class GetOrderTest {

        @Test
        @DisplayName("     ?       ???   ")
        void getOrder_success() throws Exception {
            // given
            OrderResponse response = createOrderResponse(1L, OrderStatus.CONFIRMED);

            when(orderAggregationService.getMyOrderEnriched(anyLong(), anyLong())).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("     ?       ????   -    ???? ??  ")
        void getOrder_notFound() throws Exception {
            // given
            when(orderAggregationService.getMyOrderEnriched(anyLong(), anyLong()))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

            // when & then
            mockMvc.perform(get("/api/orders/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("     ?       ????   - ??   ???? ?      ")
        void getOrder_accessDenied() throws Exception {
            // given
            when(orderAggregationService.getMyOrderEnriched(anyLong(), anyLong()))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.AccessDeniedException));

            // when & then
            mockMvc.perform(get("/api/orders/1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{orderId}/detail -      ?    ????    ??)
    class GetOrderDetailTest {

        @Test
        @DisplayName("     ?    ????    ???   ")
        void getOrderDetail_success() throws Exception {
            // given
            OrderDetailResponse response = createOrderDetailResponse(1L, OrderStatus.CONFIRMED);

            when(orderAggregationService.getOrderDetail(anyLong(), anyLong())).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/orders/1/detail"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.orderNumber").value("ORD-TEST123"));
        }

        @Test
        @DisplayName("     ?    ????    ????   -    ???? ??  ")
        void getOrderDetail_notFound() throws Exception {
            // given
            when(orderAggregationService.getOrderDetail(anyLong(), anyLong()))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

            // when & then
            mockMvc.perform(get("/api/orders/999/detail"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/orders/{orderId}/cancel -      ?  ??)
    class CancelOrderTest {

        @Test
        @DisplayName("     ?  ???   ")
        void cancelOrder_success() throws Exception {
            // given
            OrderResponse response = createOrderResponse(1L, OrderStatus.CANCELLED);

            when(orderService.cancelOrder(anyLong(), anyLong())).thenReturn(response);

            // when & then
            mockMvc.perform(put("/api/orders/1/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("    ???  ??? ???  ??"))
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("     ?  ????   -    ???? ??  ")
        void cancelOrder_notFound() throws Exception {
            // given
            when(orderService.cancelOrder(anyLong(), anyLong()))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

            // when & then
            mockMvc.perform(put("/api/orders/999/cancel"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("     ?  ????   - ?  ???  ? ?   ")
        void cancelOrder_cannotCancel() throws Exception {
            // given
            when(orderService.cancelOrder(anyLong(), anyLong()))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderCannotBeCancelledException));

            // when & then
            mockMvc.perform(put("/api/orders/1/cancel"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("     ?  ????   - ?? ? ?  ???)
        void cancelOrder_alreadyCancelled() throws Exception {
            // given
            when(orderService.cancelOrder(anyLong(), anyLong()))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderAlreadyCancelledException));

            // when & then
            mockMvc.perform(put("/api/orders/1/cancel"))
                    .andExpect(status().isBadRequest());
        }
    }

    private OrderResponse createOrderResponse(Long id, OrderStatus status) {
        OrderItemResponse item = OrderItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("??? ???  ?")
                .unitPrice(new BigDecimal("10000"))
                .quantity(2)
                .totalPrice(new BigDecimal("20000"))
                .build();

        return OrderResponse.builder()
                .id(id)
                .userId(100L)
                .orderNumber("ORD-TEST123")
                .status(status)
                .statusDescription(status.getDescription())
                .totalAmount(new BigDecimal("20000"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("20000"))
                .shippingAddress("??  ??      ?)
                .recipientName("??  ??)
                .recipientPhone("010-1234-5678")
                .items(List.of(item))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .progressStatus(status)
                .build();
    }

    private OrderDetailResponse createOrderDetailResponse(Long id, OrderStatus status) {
        return OrderDetailResponse.builder()
                .id(id)
                .userId(100L)
                .orderNumber("ORD-TEST123")
                .status(status)
                .statusDescription(status.getDescription())
                .totalAmount(new BigDecimal("20000"))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("20000"))
                .shippingAddress("??  ??      ?)
                .recipientName("??  ??)
                .recipientPhone("010-1234-5678")
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .progressStatus(status)
                .build();
    }
}
