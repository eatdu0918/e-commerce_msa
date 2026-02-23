package com.ecommerce.orderservice.outbox;

import com.ecommerce.orderservice.config.KafkaConfig;
import com.ecommerce.orderservice.entity.OutboxEvent;
import com.ecommerce.orderservice.enums.OutboxStatus;
import com.ecommerce.orderservice.event.OrderCancelledEvent;
import com.ecommerce.orderservice.event.OrderCreatedEvent;
import com.ecommerce.orderservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherTest {

    @Mock
    OutboxEventRepository outboxEventRepository;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    OutboxEventPublisher outboxEventPublisher;

    @Test
    @DisplayName("OrderCreatedEvent 발행 성공")
    void publishOrderCreatedEvent_success() {
        // given
        OrderCreatedEvent event = createOrderCreatedEvent();

        when(outboxEventRepository.save(any(OutboxEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        outboxEventPublisher.publishOrderCreatedEvent(event);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent savedEvent = captor.getValue();
        assertThat(savedEvent.getAggregateType()).isEqualTo("Order");
        assertThat(savedEvent.getAggregateId()).isEqualTo(event.getOrderNumber());
        assertThat(savedEvent.getEventType()).isEqualTo("OrderCreatedEvent");
        assertThat(savedEvent.getTopic()).isEqualTo(KafkaConfig.TOPIC_ORDER_CREATED);
        assertThat(savedEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(savedEvent.getPayload()).contains("\"orderId\":1");
    }

    @Test
    @DisplayName("OrderCancelledEvent 발행 성공")
    void publishOrderCancelledEvent_success() {
        // given
        OrderCancelledEvent event = createOrderCancelledEvent();

        when(outboxEventRepository.save(any(OutboxEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        outboxEventPublisher.publishOrderCancelledEvent(event);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent savedEvent = captor.getValue();
        assertThat(savedEvent.getAggregateType()).isEqualTo("Order");
        assertThat(savedEvent.getAggregateId()).isEqualTo(event.getOrderNumber());
        assertThat(savedEvent.getEventType()).isEqualTo("OrderCancelledEvent");
        assertThat(savedEvent.getTopic()).isEqualTo(KafkaConfig.TOPIC_ORDER_CANCELLED);
        assertThat(savedEvent.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("페이로드가 JSON 형식으로 저장됨")
    void publishEvent_payloadIsValidJson() throws JsonProcessingException {
        // given
        OrderCreatedEvent event = createOrderCreatedEvent();

        when(outboxEventRepository.save(any(OutboxEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        outboxEventPublisher.publishOrderCreatedEvent(event);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        String payload = captor.getValue().getPayload();
        OrderCreatedEvent deserializedEvent = objectMapper.readValue(payload, OrderCreatedEvent.class);

        assertThat(deserializedEvent.getOrderId()).isEqualTo(event.getOrderId());
        assertThat(deserializedEvent.getOrderNumber()).isEqualTo(event.getOrderNumber());
        assertThat(deserializedEvent.getUserId()).isEqualTo(event.getUserId());
    }

    @Test
    @DisplayName("직렬화 실패 시 RuntimeException 발생")
    void publishEvent_serializationFails_throwsException() throws JsonProcessingException {
        // given
        OrderCreatedEvent event = createOrderCreatedEvent();

        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("Serialization error") {});

        OutboxEventPublisher publisherWithMockMapper = new OutboxEventPublisher(
                outboxEventRepository, mockObjectMapper
        );

        // when & then
        assertThatThrownBy(() -> publisherWithMockMapper.publishOrderCreatedEvent(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이벤트 직렬화 실패");
    }

    private OrderCreatedEvent createOrderCreatedEvent() {
        return OrderCreatedEvent.builder()
                .eventId("evt-001")
                .orderId(1L)
                .orderNumber("ORD-20240101-001")
                .userId(100L)
                .totalAmount(new BigDecimal("50000"))
                .userCouponId(null)
                .items(List.of(
                        OrderCreatedEvent.OrderItemEvent.builder()
                                .productId(10L)
                                .productName("테스트 상품")
                                .quantity(2)
                                .unitPrice(new BigDecimal("25000"))
                                .build()
                ))
                .build();
    }

    private OrderCancelledEvent createOrderCancelledEvent() {
        return OrderCancelledEvent.builder()
                .eventId("evt-002")
                .orderId(1L)
                .orderNumber("ORD-20240101-001")
                .userId(100L)
                .userCouponId(null)
                .items(List.of(
                        OrderCancelledEvent.OrderItemEvent.builder()
                                .productId(10L)
                                .quantity(2)
                                .build()
                ))
                .build();
    }
}
