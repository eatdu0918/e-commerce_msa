package com.ecommerce.orderservice.integration;

import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.ProcessedEvent;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.orderservice.event.CouponUsedEvent;
import com.ecommerce.orderservice.event.PaymentFailedEvent;
import com.ecommerce.orderservice.event.StockDecreaseFailedEvent;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.ProcessedEventRepository;
import com.ecommerce.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Kafka Consumer 통합 테스트
 * Saga 패턴에서 이벤트 수신 시 주문 상태 변경을 검증
 */
class KafkaConsumerIntegrationTest extends IntegrationTestBase {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProcessedEventRepository processedEventRepository;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("coupon-used 이벤트 수신 시 주문 상태가 CONFIRMED로 변경")
    void handleCouponUsed_confirmsOrder() throws Exception {
        // given - 주문 생성
        OrderResponse order = createTestOrder(1L);
        Long orderId = order.getId();

        CouponUsedEvent event = CouponUsedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .userId(1L)
                .userCouponId(100L)
                .couponId(10L)
                .discountAmount(new BigDecimal("5000"))
                .build();

        // when - coupon-used 이벤트 발행 (discount-service 역할 시뮬레이션)
        kafkaTemplate.send("coupon-used", event.getOrderNumber(), event).get();

        // then - 주문 상태 변경 확인 (Consumer가 처리할 때까지 대기)
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(updatedOrder.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(updatedOrder.getFinalAmount()).isEqualByComparingTo(new BigDecimal("25000")); // 30000 - 5000
        });

        // 멱등성 검증 - ProcessedEvent에 기록됨
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(processedEventRepository.existsByEventId(event.getEventId())).isTrue();
        });
    }

    @Test
    @DisplayName("stock-decrease-failed 이벤트 수신 시 주문 취소")
    void handleStockDecreaseFailed_cancelsOrder() throws Exception {
        // given - 주문 생성
        OrderResponse order = createTestOrder(2L);
        Long orderId = order.getId();

        StockDecreaseFailedEvent event = StockDecreaseFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .reason("재고 부족")
                .build();

        // when - stock-decrease-failed 이벤트 발행
        kafkaTemplate.send("stock-decrease-failed", event.getOrderNumber(), event).get();

        // then - 주문 상태가 CANCELLED로 변경
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        });
    }

    @Test
    @DisplayName("payment-failed 이벤트 수신 시 주문 취소")
    void handlePaymentFailed_cancelsOrder() throws Exception {
        // given - 주문 생성
        OrderResponse order = createTestOrder(3L);
        Long orderId = order.getId();

        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .reason("결제 실패")
                .build();

        // when - payment-failed 이벤트 발행
        kafkaTemplate.send("payment-failed", event.getOrderNumber(), event).get();

        // then - 주문 상태가 CANCELLED로 변경
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        });
    }

    @Test
    @DisplayName("중복 이벤트 처리 방지 (멱등성)")
    void handleDuplicateEvent_ignoresSecondEvent() throws Exception {
        // given - 주문 생성
        OrderResponse order = createTestOrder(4L);
        Long orderId = order.getId();
        String eventId = UUID.randomUUID().toString();

        CouponUsedEvent event = CouponUsedEvent.builder()
                .eventId(eventId)
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .userId(4L)
                .userCouponId(100L)
                .couponId(10L)
                .discountAmount(new BigDecimal("3000"))
                .build();

        // when - 같은 이벤트를 두 번 발행
        kafkaTemplate.send("coupon-used", event.getOrderNumber(), event).get();

        // 첫 번째 이벤트 처리 대기
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(processedEventRepository.existsByEventId(eventId)).isTrue();
        });

        // 두 번째 동일 이벤트 발행
        kafkaTemplate.send("coupon-used", event.getOrderNumber(), event).get();
        Thread.sleep(2000); // 처리 시간 대기

        // then - ProcessedEvent는 하나만 존재
        List<ProcessedEvent> processedEvents = processedEventRepository.findAll();
        long count = processedEvents.stream()
                .filter(e -> e.getEventId().equals(eventId))
                .count();
        assertThat(count).isEqualTo(1);
    }

    private OrderResponse createTestOrder(Long userId) {
        OrderItemRequest itemRequest = new OrderItemRequest(
                (long) (Math.random() * 1000),
                "테스트 상품",
                new BigDecimal("30000"),
                1
        );
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
