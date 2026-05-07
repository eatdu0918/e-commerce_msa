package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.CancelServiceClient;
import com.ecommerce.orderservice.client.PaymentServiceClient;
import com.ecommerce.orderservice.client.dto.OrderCancelSummaryResponse;
import com.ecommerce.orderservice.client.dto.PaymentInfo;
import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.orderservice.exception.OrderDomainException;
import com.ecommerce.orderservice.outbox.OutboxEventPublisher;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OutboxEventPublisher outboxEventPublisher;

    @Mock
    PaymentServiceClient paymentServiceClient;

    @Mock
    CancelServiceClient cancelServiceClient;

    @InjectMocks
    OrderService orderService;

    @BeforeEach
    void setUp() {
        lenient().when(paymentServiceClient.getPaymentByOrderId(anyLong())).thenReturn(ApiResponse.success(null));
        lenient().when(cancelServiceClient.getActiveCancelForOrderAdmin(anyLong())).thenReturn(ApiResponse.success(null));
    }

    @Test
    @DisplayName("     ??   ?   ")
    void createOrder_success() {
        // given
        Long userId = 1L;
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(100L)
                .productName("??? ???  ?")
                .unitPrice(new BigDecimal("10000"))
                .quantity(2)
                .build();
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(itemRequest), null, "??  ??      ?, "??  ??, "010-1234-5678", null, null
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
        verify(outboxEventPublisher).publishOrderCreatedEvent(any());
    }

    @Test
    @DisplayName("     ??   ??   - ???  ?     ?)
    void createOrder_emptyItems_throwsException() {
        // given
        Long userId = 1L;
        CreateOrderRequest request = new CreateOrderRequest(
                Collections.emptyList(), null, "??  ??, "??  ??, "010-0000-0000", null, null
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("     ?  ?????  ??  ??  ");
    }

    @Test
    @DisplayName("     ??   ??   - null ?  ?     ?)
    void createOrder_nullItems_throwsException() {
        // given
        Long userId = 1L;
        CreateOrderRequest request = new CreateOrderRequest(
                null, null, "??  ??, "??  ??, "010-0000-0000", null, null
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(OrderDomainException.class);
    }

    @Test
    @DisplayName("        ???   ")
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
    @DisplayName("        ????   -    ???? ??       ")
    void getOrder_notFound_throwsException() {
        // given
        when(orderRepository.findByIdAndUserIdWithItems(999L, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(999L, 1L))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("    ??   ??????  ??  ");
    }

    @Test
    @DisplayName("     ?  ???    - PENDING ?   ")
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
        verify(outboxEventPublisher).publishOrderCancelledEvent(any());
    }

    @Test
    @DisplayName("     ?  ????   - DELIVERED ?   ")
    void cancelOrder_delivered_throwsException() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        Order order = createTestOrder(orderId, userId, OrderStatus.DELIVERED);

        when(orderRepository.findByIdAndUserIdWithItems(orderId, userId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderId, userId))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("?  ???????        ?   ");
    }

    @Test
    @DisplayName("     ?  ????   - CANCELLED ?   ")
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
    @DisplayName("??         ?   ??)
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
    @DisplayName("     ?        ??   ")
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
    @DisplayName("     ?        ???   - CANCELLED ?   ")
    void updateOrderStatus_cancelled_throwsException() {
        // given
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CANCELLED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("?   ??     ? ?????  ");
    }

    @Test
    @DisplayName("     ?        ???   - ?  ???     ?)
    void updateOrderStatus_cancelRequested_throwsException() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CANCEL_REQUESTED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.PREPARING))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("?   ??     ? ?????  ");
    }

    @Test
    @DisplayName("     ?        ???   - PENDING? ?     ??    ??   ")
    void updateOrderStatus_pendingToDelivered_throwsException() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.PENDING);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("??  ??? ??  ");
    }

    @Test
    @DisplayName("     ?        ??    - ?  ?    CONFIRMED ??DELIVERED (   ???    ?   )")
    void updateOrderStatus_admin_confirmedToDelivered() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CONFIRMED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("     ?        ??    -     ?    ?  ?   ????       : CONFIRMED ??SHIPPING")
    void updateOrderStatus_skipConfirm_confirmedToShipping() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CONFIRMED);
        ReflectionTestUtils.setField(order, "skipConfirmAndPreparing", true);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    @DisplayName("     ?        ??    -     ?    ?  ?   ????       : PENDING ??SHIPPING (??         ???      ????  ??")
    void updateOrderStatus_skipConfirm_pendingToShipping() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.PENDING);
        ReflectionTestUtils.setField(order, "skipConfirmAndPreparing", true);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    @DisplayName("     ?        ???   - PENDING ??SHIPPING (??   ??? ???  )")
    void updateOrderStatus_pendingToShipping_withoutFlag_throws() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.PENDING);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("??  ??? ??  ");
    }

    @Test
    @DisplayName("     ?        ???   - CONFIRMED ??SHIPPING (??   ??? ???  )")
    void updateOrderStatus_confirmedToShipping_withoutFlag_throws() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CONFIRMED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("??  ??? ??  ");
    }

    @Test
    @DisplayName("     ?        ??    -     ?       ?  ?? ??  : PREPARING ??DELIVERED")
    void updateOrderStatus_skipBoth_preparingToDelivered() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.PREPARING);
        ReflectionTestUtils.setField(order, "skipConfirmAndPreparing", true);
        ReflectionTestUtils.setField(order, "skipShippingAndDelivered", true);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("     ?        ??    - ??   ?   (    ?")
    void updateOrderStatus_sameStatus_noop() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.PREPARING);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.updateOrderStatus(orderId, OrderStatus.PREPARING);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.PREPARING);
        verifyNoInteractions(paymentServiceClient);
        verifyNoInteractions(cancelServiceClient);
    }

    @Test
    @DisplayName("     ?        ???   -    ????     ?  ??)
    void updateOrderStatus_refundedPayment_throwsException() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CONFIRMED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        PaymentInfo payment = new PaymentInfo(
                1L, orderId, "PN", "REFUNDED", new BigDecimal("10000"), "CARD", null, null);
        when(paymentServiceClient.getPaymentByOrderId(orderId)).thenReturn(ApiResponse.success(payment));

        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.PREPARING))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("?  ??? ?  ??  ");
    }

    @Test
    @DisplayName("     ?        ???   -     ? ??  ??)
    void updateOrderStatus_activeCancel_throwsException() {
        Long orderId = 1L;
        Order order = createTestOrder(orderId, 1L, OrderStatus.CONFIRMED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        OrderCancelSummaryResponse summary = new OrderCancelSummaryResponse();
        ReflectionTestUtils.setField(summary, "status", "REQUESTED");
        when(cancelServiceClient.getActiveCancelForOrderAdmin(orderId)).thenReturn(ApiResponse.success(summary));

        assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, OrderStatus.PREPARING))
                .isInstanceOf(OrderDomainException.class)
                .hasMessageContaining("?  ??? ?  ??  ");
    }

    private Order createTestOrder(Long id, Long userId, OrderStatus status) {
        Order order = Order.create(userId, new BigDecimal("20000"), null, "??  ??, "??  ??, "010-1234-5678",
                false, false);
        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "status", status);
        return order;
    }
}
