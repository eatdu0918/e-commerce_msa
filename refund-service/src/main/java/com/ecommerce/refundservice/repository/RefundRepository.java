package com.ecommerce.refundservice.repository;

import com.ecommerce.refundservice.entity.Refund;
import com.ecommerce.refundservice.enums.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Page<Refund> findByUserId(Long userId, Pageable pageable);

    Optional<Refund> findByIdAndUserId(Long id, Long userId);

    Optional<Refund> findByCancelId(Long cancelId);

    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);

    Optional<Refund> findByRefundNumber(String refundNumber);
}
