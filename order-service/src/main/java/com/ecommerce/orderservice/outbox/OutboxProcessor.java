package com.ecommerce.orderservice.outbox;

import com.ecommerce.orderservice.entity.OutboxEvent;
import com.ecommerce.orderservice.enums.OutboxStatus;
import com.ecommerce.orderservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_RETRY_COUNT = 3;
    private static final int CLEANUP_DAYS = 7;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatusWithLimit(OutboxStatus.PENDING, BATCH_SIZE);

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Kafka     ???  : eventId={}, topic={}", event.getId(), event.getTopic(), ex);
                            }
                        });

                event.markAsProcessed();
                log.debug("Outbox ??  ??   ???   : eventId={}, eventType={}", event.getId(), event.getEventType());

            } catch (Exception e) {
                log.error("Outbox ??  ??   ????  : eventId={}, eventType={}", event.getId(), event.getEventType(), e);
                event.markAsFailed();

                if (!event.canRetry(MAX_RETRY_COUNT)) {
                    log.warn("Outbox ??  ??   ? ??????  ?? eventId={}, retryCount={}", event.getId(), event.getRetryCount());
                }
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupProcessedEvents() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(CLEANUP_DAYS);
        int deletedCount = outboxEventRepository.deleteProcessedEventsBefore(OutboxStatus.PROCESSED, cutoffDate);
        log.info("Outbox ??  ???    ?   : ???????  ????{}", deletedCount);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED);

        for (OutboxEvent event : failedEvents) {
            if (event.canRetry(MAX_RETRY_COUNT)) {
                event.retry();
                log.info("Outbox ??  ?????????    ??  ?: eventId={}, retryCount={}", event.getId(), event.getRetryCount());
            }
        }
    }
}
