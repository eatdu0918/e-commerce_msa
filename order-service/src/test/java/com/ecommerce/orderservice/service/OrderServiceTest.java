package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.dto.response.PageResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.orderservice.exception.OrderDomainException;
import com.ecommerce.orderservice.kafka.OrderEventProducer;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderEventProducer orderEventProducer;

    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {
        // given
        Long userId = 1L;
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(100L)
                .productName("테스트 상품")
                .unitPrice(new BigDecimal("10000"))
                .quantity(2)
                .build();
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(itemRequest), null, "서울시 강남구", "홍길동", "010-1234-5678"
        );

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        // when
        OrderResponse response = orderService.createOrder(userId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer).sendOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("주문 생성 실패 - 빈 상품 목록")
    void createOrder_emptyItems_throwsException() {
        // given
        Long userId = 1L;
        CreateOrderRequest request = new CreateOrderRequest(
                Collections.emptyList(), null, "서울시", "홍길동", "010-0000-0000"
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("주문 상품이 비어있습니다");
    }

    @Test
    @DisplayName("주문 생성 실패 - null 상품 목록")
    void createOrder_nullItems_throwsException() {
        // given
        Long userId = 1L;
        CreateOrderRequest request = new CreateOrderRequest(
                null, null, "서울시", "홍길동", "010-0000-0000"
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(OrderDomainException.class);
    }

    @Test
    @DisplayName("주문 조회 성공")
    void getOrder_success() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        Order order = createTestOrder(orderId, userId, OrderStatus.PENDING);

        when(orderRepository.findByIdAndUserIdWithItems(orderId, userId)).thenReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.getOrder(orderId, userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("주문 조회 실패 - 존재하지 않는 주문")
    void getOrder_notFound_throwsException() {
        // given
        when(orderRepository.findByIdAndUserIdWithItems(999L, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(999L, 1L))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문 취소 성공 - PENDING 상태")
    void cancelOrder_pending_success() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        Order order = createTestOrder(orderId, userId, OrderStatus.PENDING);

        when(orderRepository.findByIdAndUserIdWithItems(orderId, userId)).thenReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.cancelOrder(orderId, userId);

        // then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderEventProducer).sendOrderCancelledEvent(any());
    }

    @Test
    @DisplayName("주문 취소 실패 - DELIVERED 상태")
    void cancelOrder_delivered_throwsException() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        Order order = createTestOrder(orderId, userId, OrderStatus.DELIVERED);

        when(orderRepository.findByIdAndUserIdWithItems(orderId, userId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("취소할 수 없는 주문 상태");
    }

    @Test
    @DisplayName("주문 취소 실패 - CANCELLED 상태")
    void cancelOrder_alreadyCancelled_throwsException() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        Order order = createTestOrder(orderId, userId, OrderStatus.CANCELLED);

        when(orderRepository.findByIdAndUserIdWithItems(orderId, userId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(OrderDomainException.class);
    }

    @Test
    @DisplayName("내 주문 목록 조회")
    void getMyOrders_returnsPaginated() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Order order = createTestOrder(1L, userId, OrderStatus.PENDING);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findByUserId(userId, pageable)).thenReturn(page);

        // when
        PageResponse<OrderResponse> response = orderService.getMyOrders(userId, pageable);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("주문 상태 변경 성공")
    void updateOrderStatus_success() {
        // given
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CONFIRMED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.PREPARING);

        // then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    @DisplayName("주문 상태 변경 실패 - CANCELLED 상태")
    void updateOrderStatus_cancelled_throwsException() {
        // given
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CANCELLED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("상태를 변경할 수 없는");
    }

    private Order createTestOrder(Long id, Long userId, OrderStatus status) {
        Order order = Order.create(userId, new BigDecimal("20000"), null, "서울시", "홍길동", "010-1234-5678");
        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "status", status);
        return order;
    }
}
