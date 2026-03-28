package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    Optional<Payment> findByIdAndUserId(Long id, Long userId);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderIdAndUserId(Long orderId, Long userId);

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
