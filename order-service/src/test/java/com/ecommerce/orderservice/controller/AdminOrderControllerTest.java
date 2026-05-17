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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminOrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, SecurityConfig.class}))
@AutoConfigureMockMvc(addFilters = false)
class AdminOrderControllerTest {

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
        CustomUserDetails userDetails = new CustomUserDetails(1L, "admin@test.com", "", UserRole.ADMIN);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("GET /api/admin/orders - 전체 주문 목록 조회")
    class GetAllOrdersTest {

        @Test
        @DisplayName("전체 주문 목록 조회 성공")
        void getAllOrders_success() throws Exception {
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

            when(orderAggregationService.getAllOrdersForAdmin(any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("상태별 주문 목록 조회 성공")
        void getAllOrders_withStatus() throws Exception {
            // given
            OrderResponse order = createOrderResponse(1L, OrderStatus.PENDING);

            PageResponse<OrderResponse> pageResponse = PageResponse.<OrderResponse>builder()
                    .content(List.of(order))
                    .pageNumber(0)
                    .pageSize(20)
                    .totalElements(1L)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .build();

            when(orderAggregationService.getOrdersByStatusForAdmin(eq(OrderStatus.PENDING), any(Pageable.class)))
                    .thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/orders")
                            .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("전체 주문 목록 조회 - 빈 목록")
        void getAllOrders_empty() throws Exception {
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

            when(orderAggregationService.getAllOrdersForAdmin(any(Pageable.class))).thenReturn(pageResponse);

            // when & then
            mockMvc.perform(get("/api/admin/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isEmpty())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/orders/{orderId} - 주문 상세 조회")
    class GetOrderTest {

        @Test
        @DisplayName("주문 상세 조회 성공")
        void getOrder_success() throws Exception {
            // given
            OrderResponse response = createOrderResponse(1L, OrderStatus.CONFIRMED);

            when(orderAggregationService.getOrderByIdForAdmin(1L)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/admin/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("주문 상세 조회 실패 - 존재하지 않음")
        void getOrder_notFound() throws Exception {
            // given
            when(orderAggregationService.getOrderByIdForAdmin(999L))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

            // when & then
            mockMvc.perform(get("/api/admin/orders/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/orders/{orderId}/detail - 주문 상세 통합 조회")
    class GetOrderDetailTest {

        @Test
        @DisplayName("주문 상세 통합 조회 성공")
        void getOrderDetail_success() throws Exception {
            // given
            OrderDetailResponse response = createOrderDetailResponse(1L, OrderStatus.CONFIRMED);

            when(orderAggregationService.getOrderDetailAdmin(1L)).thenReturn(response);

            // when & then
            mockMvc.perform(get("/api/admin/orders/1/detail"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.orderNumber").value("ORD-TEST123"));
        }

        @Test
        @DisplayName("주문 상세 통합 조회 실패 - 존재하지 않음")
        void getOrderDetail_notFound() throws Exception {
            // given
            when(orderAggregationService.getOrderDetailAdmin(999L))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

            // when & then
            mockMvc.perform(get("/api/admin/orders/999/detail"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/orders/{orderId}/status - 주문 상태 변경")
    class UpdateOrderStatusTest {

        @Test
        @DisplayName("주문 상태 변경 성공")
        void updateOrderStatus_success() throws Exception {
            // given
            OrderResponse response = createOrderResponse(1L, OrderStatus.SHIPPING);

            when(orderService.updateOrderStatus(1L, OrderStatus.SHIPPING)).thenReturn(response);

            String requestBody = """
                    {
                        "status": "SHIPPING"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/orders/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("주문 상태가 변경되었습니다."))
                    .andExpect(jsonPath("$.data.status").value("SHIPPING"));
        }

        @Test
        @DisplayName("주문 상태 변경 실패 - 필수 값 누락")
        void updateOrderStatus_validation_fail() throws Exception {
            // given
            String requestBody = """
                    {
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/orders/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("주문 상태 변경 실패 - 존재하지 않음")
        void updateOrderStatus_notFound() throws Exception {
            // given
            when(orderService.updateOrderStatus(999L, OrderStatus.SHIPPING))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

            String requestBody = """
                    {
                        "status": "SHIPPING"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/orders/999/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("주문 상태 변경 실패 - 변경 불가 상태")
        void updateOrderStatus_cannotUpdate() throws Exception {
            // given
            when(orderService.updateOrderStatus(1L, OrderStatus.SHIPPING))
                    .thenThrow(new OrderDomainException(OrderDomainExceptionCode.OrderCannotBeUpdatedException));

            String requestBody = """
                    {
                        "status": "SHIPPING"
                    }
                    """;

            // when & then
            mockMvc.perform(put("/api/admin/orders/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    private OrderResponse createOrderResponse(Long id, OrderStatus status) {
        OrderItemResponse item = OrderItemResponse.builder()
                .id(1L)
                .productId(1L)
                .productName("테스트 상품")
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
                .shippingAddress("서울시 강남구")
                .recipientName("홍길동")
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
                .shippingAddress("서울시 강남구")
                .recipientName("홍길동")
                .recipientPhone("010-1234-5678")
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .progressStatus(status)
                .build();
    }
}
