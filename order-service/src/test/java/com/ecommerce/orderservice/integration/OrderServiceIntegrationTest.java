package com.ecommerce.orderservice.integration;

import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OutboxEvent;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.orderservice.enums.OutboxStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.OutboxEventRepository;
import com.ecommerce.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OrderService 통합 테스트
 * H2 인메모리 DB + EmbeddedKafka를 사용하여 테스트
 */
class OrderServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("주문 생성 - 실제 DB 저장 검증")
    void createOrder_savesToDatabase() {
        // given
        Long userId = 1L;
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(1L)
                .productName("테스트 상품")
                .unitPrice(new BigDecimal("10000"))
                .quantity(2)
                .build();
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(itemRequest),
                null,
                "서울시 강남구 테헤란로 123",
                "홍길동",
                "010-1234-5678"
        );

        // when
        OrderResponse response = orderService.createOrder(userId, request);

        // then - DB 저장 검증
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("20000"));

        // DB에서 직접 조회하여 검증 (fetch join으로 orderItems 포함)
        Order savedOrder = orderRepository.findByIdWithItems(response.getId()).orElseThrow();
        assertThat(savedOrder.getOrderNumber()).isNotNull();
        assertThat(savedOrder.getShippingAddress()).isEqualTo("서울시 강남구 테헤란로 123");
        assertThat(savedOrder.getOrderItems()).hasSize(1);
    }

    @Test
    @DisplayName("주문 생성 - Outbox 이벤트 저장 검증 (Transactional Outbox Pattern)")
    void createOrder_savesOutboxEvent() {
        // given
        Long userId = 2L;
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(200L)
                .productName("Outbox 테스트 상품")
                .unitPrice(new BigDecimal("25000"))
                .quantity(1)
                .build();
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(itemRequest),
                null,
                "부산시 해운대구",
                "김철수",
                "010-9876-5432"
        );

        // when
        OrderResponse response = orderService.createOrder(userId, request);

        // then - Outbox 테이블에 이벤트 저장 검증
        List<OutboxEvent> outboxEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        assertThat(outboxEvents).isNotEmpty();

        OutboxEvent event = outboxEvents.stream()
                .filter(e -> e.getAggregateId().equals(response.getOrderNumber()))
                .findFirst()
                .orElseThrow();

        assertThat(event.getAggregateType()).isEqualTo("Order");
        assertThat(event.getEventType()).isEqualTo("OrderCreatedEvent");
        assertThat(event.getTopic()).isEqualTo("order-created");
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getPayload()).contains(response.getId().toString());
    }

    @Test
    @DisplayName("주문 조회 - 사용자의 주문 목록 조회")
    void getMyOrders_returnsUserOrders() {
        // given
        Long userId = 3L;

        // 주문 2개 생성
        createTestOrder(userId, "상품1", new BigDecimal("10000"));
        createTestOrder(userId, "상품2", new BigDecimal("20000"));

        // 다른 사용자 주문
        createTestOrder(999L, "다른사용자상품", new BigDecimal("5000"));

        // when
        var result = orderService.getMyOrders(userId, org.springframework.data.domain.PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(order -> order.getUserId().equals(userId));
    }

    @Test
    @DisplayName("주문 상세 조회 - 주문 아이템 포함")
    void getOrder_returnsOrderWithItems() {
        // given
        Long userId = 4L;
        OrderResponse created = createTestOrder(userId, "상세조회테스트", new BigDecimal("30000"));

        // when
        OrderResponse result = orderService.getOrder(created.getId(), userId);

        // then
        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getItems()).isNotEmpty();
        assertThat(result.getItems().get(0).getProductName()).isEqualTo("상세조회테스트");
    }

    private OrderResponse createTestOrder(Long userId, String productName, BigDecimal price) {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId((long) (Math.random() * 1000))
                .productName(productName)
                .unitPrice(price)
                .quantity(1)
                .build();
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(itemRequest),
                null,
                "테스트 주소",
                "테스트 수령인",
                "010-0000-0000"
        );
        return orderService.createOrder(userId, request);
    }
}
