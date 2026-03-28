package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PageResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.entity.Payment;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.paymentservice.exception.PaymentDomainException;
import com.ecommerce.paymentservice.exception.PaymentDomainExceptionCode;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentRequest request) {
        log.info("결제 생성 시도: userId={}, orderId={}", userId, request.getOrderId());

        Payment payment = Payment.create(
                request.getOrderId(),
                request.getOrderNumber(),
                userId,
                request.getPaymentMethod(),
                request.getAmount(),
                request.getPaymentDetails()
        );

        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 생성 완료: paymentId={}, paymentNumber={}",
                savedPayment.getId(), savedPayment.getPaymentNumber());

        return PaymentResponse.from(savedPayment);
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
        Payment payment = paymentRepository.findByOrderId(orderId)
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
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        log.info("결제 상태 변경 시도: paymentId={}, newStatus={}", paymentId, newStatus);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentDomainException(PaymentDomainExceptionCode.PaymentNotFoundException));

        payment.updateStatus(newStatus);
        log.info("결제 상태 변경 완료: paymentId={}, status={}", paymentId, newStatus);

        return PaymentResponse.from(payment);
    }
}
