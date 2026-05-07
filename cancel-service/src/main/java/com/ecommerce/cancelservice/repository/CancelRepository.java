package com.ecommerce.cancelservice.repository;

import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import com.ecommerce.cancelservice.enums.CancelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface CancelRepository extends JpaRepository<Cancel, Long> {

    Page<Cancel> findByUserId(Long userId, Pageable pageable);

    Optional<Cancel> findByIdAndUserId(Long id, Long userId);

    Page<Cancel> findByStatus(CancelStatus status, Pageable pageable);

    Page<Cancel> findByRequestType(CancelRequestType requestType, Pageable pageable);

    Page<Cancel> findByStatusAndRequestType(
            CancelStatus status, CancelRequestType requestType, Pageable pageable);

    @Query("SELECT c FROM Cancel c LEFT JOIN FETCH c.cancelItems WHERE c.id = :id")
    Optional<Cancel> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT c FROM Cancel c LEFT JOIN FETCH c.cancelItems WHERE c.id = :id AND c.userId = :userId")
    Optional<Cancel> findByIdAndUserIdWithItems(@Param("id") Long id, @Param("userId") Long userId);

    Optional<Cancel> findByOrderId(Long orderId);

    boolean existsByOrderIdAndUserIdAndStatusIn(Long orderId, Long userId, Collection<CancelStatus> statuses);

    boolean existsByOrderIdAndUserIdAndStatusAndRequestType(
            Long orderId, Long userId, CancelStatus status, CancelRequestType requestType);

    boolean existsByOrderIdAndStatusAndRequestType(
            Long orderId, CancelStatus status, CancelRequestType requestType);

    /**
     *     ?   ???  ???    ?  ?  ?       ??  ?   ??   ?? ???     ?   ? ?     ???    ??? ? ? ??
     */
    Optional<Cancel> findFirstByOrderIdAndUserIdAndStatusInOrderByIdDesc(
            Long orderId, Long userId, Collection<CancelStatus> statuses);

    /** ?  ?     rder-service    ??      ????? ? ?  ???        ID   ?  ?    ? ??  ??   ??*/
    Optional<Cancel> findFirstByOrderIdAndStatusInOrderByIdDesc(
            Long orderId, Collection<CancelStatus> statuses);
}
