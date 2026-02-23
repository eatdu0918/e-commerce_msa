package com.ecommerce.refundservice.repository;

import com.ecommerce.refundservice.entity.OutboxEvent;
import com.ecommerce.refundservice.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC LIMIT :limit")
    List<OutboxEvent> findByStatusWithLimit(@Param("status") OutboxStatus status, @Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.status = :status AND o.processedAt < :before")
    int deleteProcessedEventsBefore(@Param("status") OutboxStatus status, @Param("before") LocalDateTime before);
}
