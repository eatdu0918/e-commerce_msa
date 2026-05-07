package com.ecommerce.orderservice.outbox;

import com.ecommerce.orderservice.entity.OutboxEvent;
import com.ecommerce.orderservice.enums.OutboxStatus;
import com.ecommerce.orderservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class OutboxProcessorTest {

    @Mock
    OutboxEventRepository outboxEventRepository;

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    OutboxProcessor outboxProcessor;

    @Test
    @DisplayName("PENDING ?   ????  ??   ???   ")
    void processOutboxEvents_success() {
        // given
        OutboxEvent event = createTestEvent(1L, OutboxStatus.PENDING);
        List<OutboxEvent> events = List.of(event);

        when(outboxEventRepository.findByStatusWithLimit(OutboxStatus.PENDING, 100))
                .thenReturn(events);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // when
        outboxProcessor.processOutboxEvents();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
        assertThat(event.getProcessedAt()).isNotNull();
        verify(kafkaTemplate).send(event.getTopic(), event.getAggregateId(), event.getPayload());
    }

    @Test
    @DisplayName("PENDING ??  ? ? ??   ?   ???? ??  ")
    void processOutboxEvents_noEvents() {
        // given
        when(outboxEventRepository.findByStatusWithLimit(OutboxStatus.PENDING, 100))
                .thenReturn(Collections.emptyList());

        // when
        outboxProcessor.processOutboxEvents();

        // then
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Kafka     ???       ?????  ??FAILED    ??)
    void processOutboxEvents_kafkaException_marksFailed() {
        // given
        OutboxEvent event = createTestEvent(1L, OutboxStatus.PENDING);
        List<OutboxEvent> events = List.of(event);

        when(outboxEventRepository.findByStatusWithLimit(OutboxStatus.PENDING, 100))
                .thenReturn(events);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka error"));

        // when
        outboxProcessor.processOutboxEvents();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("??????  ??   ??   ??)
    void processOutboxEvents_multipleEvents() {
        // given
        OutboxEvent event1 = createTestEvent(1L, OutboxStatus.PENDING);
        OutboxEvent event2 = createTestEvent(2L, OutboxStatus.PENDING);
        List<OutboxEvent> events = List.of(event1, event2);

        when(outboxEventRepository.findByStatusWithLimit(OutboxStatus.PENDING, 100))
                .thenReturn(events);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        // when
        outboxProcessor.processOutboxEvents();

        // then
        assertThat(event1.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
        assertThat(event2.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("   ?????  ???    - 7????   ??  ??????)
    void cleanupProcessedEvents_deletesOldEvents() {
        // given
        when(outboxEventRepository.deleteProcessedEventsBefore(eq(OutboxStatus.PROCESSED), any(LocalDateTime.class)))
                .thenReturn(5);

        // when
        outboxProcessor.cleanupProcessedEvents();

        // then
        verify(outboxEventRepository).deleteProcessedEventsBefore(eq(OutboxStatus.PROCESSED), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("??  ????  ???????- ?????   ?  ?   ??PENDING??       ?)
    void retryFailedEvents_retryableEvents() {
        // given
        OutboxEvent event = createTestEvent(1L, OutboxStatus.FAILED);
        // retryCount = 0 (?????   ??

        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED))
                .thenReturn(List.of(event));

        // when
        outboxProcessor.retryFailedEvents();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    @DisplayName("??  ????  ???????-    ? ???????   ?  ????FAILED ? ?")
    void retryFailedEvents_maxRetryExceeded() {
        // given
        OutboxEvent event = createTestEvent(1L, OutboxStatus.FAILED);
        //    ? ???????  (3)???   
        ReflectionTestUtils.setField(event, "retryCount", 3);

        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED))
                .thenReturn(List.of(event));

        // when
        outboxProcessor.retryFailedEvents();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    @DisplayName("??  ????  ? ? ??   ??????   ????  ")
    void retryFailedEvents_noFailedEvents() {
        // given
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED))
                .thenReturn(Collections.emptyList());

        // when
        outboxProcessor.retryFailedEvents();

        // then - no exception thrown
    }

    @Test
    @DisplayName("?  ????      ??- ??? ??  ?    ??  ")
    void processOutboxEvents_partialFailure() {
        // given
        OutboxEvent event1 = createTestEvent(1L, OutboxStatus.PENDING);
        OutboxEvent event2 = createTestEvent(2L, OutboxStatus.PENDING);
        List<OutboxEvent> events = List.of(event1, event2);

        when(outboxEventRepository.findByStatusWithLimit(OutboxStatus.PENDING, 100))
                .thenReturn(events);

        //  ?   ????   , ??   ?????  
        when(kafkaTemplate.send(eq("order-created"), eq("ORD-1"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        when(kafkaTemplate.send(eq("order-created"), eq("ORD-2"), anyString()))
                .thenThrow(new RuntimeException("Kafka error"));

        // when
        outboxProcessor.processOutboxEvents();

        // then
        assertThat(event1.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
        assertThat(event2.getStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    private OutboxEvent createTestEvent(Long id, OutboxStatus status) {
        OutboxEvent event = OutboxEvent.create(
                "Order",
                "ORD-" + id,
                "OrderCreatedEvent",
                "order-created",
                "{\"orderId\":" + id + "}"
        );
        ReflectionTestUtils.setField(event, "id", id);
        ReflectionTestUtils.setField(event, "status", status);
        ReflectionTestUtils.setField(event, "createdAt", LocalDateTime.now());
        return event;
    }
}
