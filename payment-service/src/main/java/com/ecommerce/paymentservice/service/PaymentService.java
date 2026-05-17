package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.event.PaymentCompletedEvent;
import com.ecommerce.paymentservice.exception.PaymentDomainException;
import com.ecommerce.paymentservice.exception.PaymentDomainExceptionCode;
import com.ecommerce.paymentservice.outbox.OutboxEventPublisher;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        log.info("   ????   ??  : userId={}, orderId={}", userId, request.getOrderId());

        return paymentRepository.findByOrderIdAndUserId(request.getOrderId(), userId)
                .map(existing -> {
                    log.info("??           ?  ? ?? ?    ???      ?? ?   ?? paymentId={}, orderId={}",
                            existing.getId(), request.getOrderId());
                    return PaymentResponse.from(existing);
                })
                .orElseGet(() -> persistNewPayment(userId, request));
    }

    private PaymentResponse persistNewPayment(Long userId, CreatePaymentRequest request) {
        Payment payment = Payment.create(
                request.getOrderId(),
                request.getOrderNumber(),
                userId,
                request.getPaymentMethod(),
                request.getAmount(),
                request.getPaymentDetails()
        );
        payment.complete();

        try {
            Payment saved = paymentRepository.save(payment);
            log.info("   ????   ?   : paymentId={}, paymentNumber={}",
                    saved.getId(), saved.getPaymentNumber());
            publishPaymentCompletedOutbox(saved);
            return PaymentResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("   ???   ???   ???   ?       ??: orderId={}, message={}",
                    request.getOrderId(), e.getMessage());
            return paymentRepository.findByOrderIdAndUserId(request.getOrderId(), userId)
                    .map(PaymentResponse::from)
                    .orElseThrow(() -> e);
        }
    }

    private void publishPaymentCompletedOutbox(Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .paymentId(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .build();
        outboxEventPublisher.publishPaymentCompletedEvent(event);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getMyPayments(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        Page<PaymentResponse> responsePage = payments.map(PaymentResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        Page<PaymentResponse> responsePage = payments.map(PaymentResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByStatus(status, pageable);
        Page<PaymentResponse> responsePage = payments.map(PaymentResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderIdAdmin(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        log.info("   ???        ???  : paymentId={}, newStatus={}", paymentId, newStatus);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));

        payment.updateStatus(newStatus);
        log.info("   ???        ??   : paymentId={}, status={}", paymentId, newStatus);

        return PaymentResponse.from(payment);
    }
}
