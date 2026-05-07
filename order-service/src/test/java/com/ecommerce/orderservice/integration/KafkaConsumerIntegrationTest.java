package com.ecommerce.orderservice.integration;

import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.ProcessedEvent;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.orderservice.event.CancelRejectedEvent;
import com.ecommerce.orderservice.event.CancelRequestedEvent;
import com.ecommerce.orderservice.event.CouponUsedEvent;
import com.ecommerce.orderservice.event.PaymentCompletedEvent;
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
 * Kafka Consumer ???? ??? ??
 * Saga ??? ? ?  ??  ????   ??     ?          ??    ?
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
    @DisplayName("coupon-used ??  ????   ??     ?       CONFIRMED ?    ?)
    void handleCouponUsed_confirmsOrder() throws Exception {
        // given -      ??  
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
                .couponName("?   ???   ")
                .couponCode("SPRING")
                .couponType("PERCENTAGE")
                .couponRuleValue(new BigDecimal("10"))
                .build();

        // when - coupon-used ??  ??    ?(discount-service ?? ??????  ??
        kafkaTemplate.send("coupon-used", event.getOrderNumber(), event).get();

        // then -      ?        ??    (Consumer       ??????     ?? ?
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(updatedOrder.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(updatedOrder.getFinalAmount()).isEqualByComparingTo(new BigDecimal("25000")); // 30000 - 5000
            assertThat(updatedOrder.getAppliedCouponName()).isEqualTo("?   ???   ");
            assertThat(updatedOrder.getAppliedCouponCode()).isEqualTo("SPRING");
            assertThat(updatedOrder.getAppliedCouponType()).isEqualTo("PERCENTAGE");
            assertThat(updatedOrder.getAppliedCouponRuleValue()).isEqualByComparingTo(new BigDecimal("10"));
        });

        //     ??    ?- ProcessedEvent??    ??
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(processedEventRepository.existsByEventId(event.getEventId())).isTrue();
        });
    }

    @Test
    @DisplayName("stock-decrease-failed ??  ????   ??     ?  ??)
    void handleStockDecreaseFailed_cancelsOrder() throws Exception {
        // given -      ??  
        OrderResponse order = createTestOrder(2L);
        Long orderId = order.getId();

        StockDecreaseFailedEvent event = StockDecreaseFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .reason("?????   ?)
                .build();

        // when - stock-decrease-failed ??  ??    ?
        kafkaTemplate.send("stock-decrease-failed", event.getOrderNumber(), event).get();

        // then -      ?       CANCELLED ?    ?
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        });
    }

    @Test
    @DisplayName("payment-completed ??  ????   ??PENDING     ??CONFIRMED ?    ?)
    void handlePaymentCompleted_confirmsPendingOrder() throws Exception {
        OrderResponse order = createTestOrder(5L);
        Long orderId = order.getId();

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(99L)
                .paymentNumber("PAY-TEST")
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .userId(5L)
                .amount(new BigDecimal("30000"))
                .paymentMethod("CREDIT_CARD")
                .build();

        kafkaTemplate.send("payment-completed", event.getOrderNumber(), event).get();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        });

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(processedEventRepository.existsByEventId(event.getEventId())).isTrue();
        });
    }

    @Test
    @DisplayName("payment-completed: ??  ??  ???  ???  ???    ??      ?      ?   ??coupon-used    ??????")
    void handlePaymentCompleted_reconcilesDiscountWhenPaidLessThanTotal() throws Exception {
        OrderResponse order = createTestOrder(8L);
        Long orderId = order.getId();

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(101L)
                .paymentNumber("PAY-DISC")
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .userId(8L)
                .amount(new BigDecimal("25000"))
                .paymentMethod("TOSSPAYMENTS")
                .build();

        kafkaTemplate.send("payment-completed", event.getOrderNumber(), event).get();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updated = orderRepository.findById(orderId).orElseThrow();
            assertThat(updated.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5000"));
            assertThat(updated.getFinalAmount()).isEqualByComparingTo(new BigDecimal("25000"));
            assertThat(updated.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        });
    }

    @Test
    @DisplayName("payment-failed ??  ????   ??     ?  ??)
    void handlePaymentFailed_cancelsOrder() throws Exception {
        // given -      ??  
        OrderResponse order = createTestOrder(3L);
        Long orderId = order.getId();

        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .reason("   ????  ")
                .build();

        // when - payment-failed ??  ??    ?
        kafkaTemplate.send("payment-failed", event.getOrderNumber(), event).get();

        // then -      ?       CANCELLED ?    ?
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        });
    }

    @Test
    @DisplayName("cancel-requested ??  ????   ??    ???  ???     ??    ?    ?)
    void handleCancelRequested_setsCancelRequested() throws Exception {
        OrderResponse order = createTestOrder(6L);
        Long orderId = order.getId();

        CancelRequestedEvent event = CancelRequestedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .cancelId(1L)
                .cancelNumber("CAN-TEST01")
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .userId(6L)
                .cancelReason("CHANGE_OF_MIND")
                .items(List.of())
                .build();

        kafkaTemplate.send("cancel-requested", event.getOrderNumber(), event).get();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CANCEL_REQUESTED);
        });
    }

    @Test
    @DisplayName("cancel-requested ??  ?       ?? ?    ??   ???? ??  ")
    void handleCancelRequested_ignoredWhenShipping() throws Exception {
        OrderResponse order = createTestOrder(8L);
        Long orderId = order.getId();
        Order entity = orderRepository.findById(orderId).orElseThrow();
        entity.updateStatus(OrderStatus.SHIPPING);
        orderRepository.save(entity);

        CancelRequestedEvent event = CancelRequestedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .cancelId(3L)
                .cancelNumber("CAN-SHIP01")
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .userId(8L)
                .cancelReason("CHANGE_OF_MIND")
                .requestType("ORDER_CANCEL")
                .items(List.of())
                .build();

        kafkaTemplate.send("cancel-requested", event.getOrderNumber(), event).get();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(orderId).orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.SHIPPING);
        });
    }

    @Test
    @DisplayName("cancel-rejected ??  ????   ???  ???       ??     ?    ?   ?")
    void handleCancelRejected_restoresPreviousStatus() throws Exception {
        OrderResponse order = createTestOrder(7L);
        Long orderId = order.getId();

        CancelRequestedEvent requested = CancelRequestedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .cancelId(2L)
                .cancelNumber("CAN-TEST02")
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .userId(7L)
                .cancelReason("CHANGE_OF_MIND")
                .items(List.of())
                .build();
        kafkaTemplate.send("cancel-requested", requested.getOrderNumber(), requested).get();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(orderRepository.findById(orderId).orElseThrow().getStatus())
                    .isEqualTo(OrderStatus.CANCEL_REQUESTED);
        });

        CancelRejectedEvent rejected = CancelRejectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .cancelId(2L)
                .cancelNumber("CAN-TEST02")
                .orderId(orderId)
                .orderNumber(order.getOrderNumber())
                .userId(7L)
                .rejectedReason("?  ???  ?")
                .build();
        kafkaTemplate.send("cancel-rejected", rejected.getOrderNumber(), rejected).get();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(orderRepository.findById(orderId).orElseThrow().getStatus())
                    .isEqualTo(OrderStatus.PENDING);
        });
    }

    @Test
    @DisplayName("   ????  ??   ??   ? (    ??")
    void handleDuplicateEvent_ignoresSecondEvent() throws Exception {
        // given -      ??  
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

        // when -    ? ??  ? ? ?? ?    ?
        kafkaTemplate.send("coupon-used", event.getOrderNumber(), event).get();

        //  ?   ????  ??   ???? ?
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(processedEventRepository.existsByEventId(eventId)).isTrue();
        });

        // ??   ????   ??  ??    ?
        kafkaTemplate.send("coupon-used", event.getOrderNumber(), event).get();
        Thread.sleep(2000); //    ????   ?? ?

        // then - ProcessedEvent????   ?   ??
        List<ProcessedEvent> processedEvents = processedEventRepository.findAll();
        long count = processedEvents.stream()
                .filter(e -> e.getEventId().equals(eventId))
                .count();
        assertThat(count).isEqualTo(1);
    }

    private OrderResponse createTestOrder(Long userId) {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId((long) (Math.random() * 1000))
                .productName("??? ???  ?")
                .unitPrice(new BigDecimal("30000"))
                .quantity(1)
                .build();
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(itemRequest),
                null,
                "??? ??   ??,
                "??? ????  ??,
                "010-0000-0000",
                null,
                null
        );
        return orderService.createOrder(userId, request);
    }
}
