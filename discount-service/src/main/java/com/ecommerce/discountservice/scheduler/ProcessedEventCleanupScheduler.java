package com.ecommerce.discountservice.scheduler;

import com.ecommerce.discountservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedEventCleanupScheduler {

    private final ProcessedEventRepository processedEventRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldProcessedEvents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        int deletedCount = processedEventRepository.deleteByProcessedAtBefore(cutoff);
        if (deletedCount > 0) {
            log.info("ProcessedEvent 정리 완료: {}건 삭제 (기준일: {})", deletedCount, cutoff);
        }
    }
}
