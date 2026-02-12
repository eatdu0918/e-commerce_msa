package com.ecommerce.cancelservice.repository;

import com.ecommerce.cancelservice.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventId(String eventId);

    @Modifying
    @Query("DELETE FROM ProcessedEvent e WHERE e.processedAt < :cutoff")
    int deleteByProcessedAtBefore(LocalDateTime cutoff);
}
