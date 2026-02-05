package com.ecommerce.refundservice.service;

import com.ecommerce.refundservice.dto.response.PageResponse;
import com.ecommerce.refundservice.dto.response.RefundResponse;
import com.ecommerce.refundservice.entity.Refund;
import com.ecommerce.refundservice.enums.RefundStatus;
import com.ecommerce.refundservice.event.RefundCompletedEvent;
import com.ecommerce.refundservice.event.RefundFailedEvent;
import com.ecommerce.refundservice.exception.RefundDomainException;
import com.ecommerce.refundservice.exception.RefundDomainExceptionCode;
import com.ecommerce.refundservice.kafka.RefundEventProducer;
import com.ecommerce.refundservice.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final RefundEventProducer refundEventProducer;

    @Transactional(readOnly = true)
    public RefundResponse getRefund(Long refundId, Long userId) {
        Refund refund = refundRepository.findByIdAndUserId(refundId, userId)
                .orElseThrow(() -> new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));
        return RefundResponse.from(refund);
    }

    @Transactional(readOnly = true)
    public PageResponse<RefundResponse> getMyRefunds(Long userId, Pageable pageable) {
        Page<Refund> refunds = refundRepository.findByUserId(userId, pageable);
        Page<RefundResponse> responsePage = refunds.map(RefundResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefundByCancelId(Long cancelId, Long userId) {
        Refund refund = refundRepository.findByCancelId(cancelId)
                .orElseThrow(() -> new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));
        return RefundResponse.from(refund);
    }

    @Transactional(readOnly = true)
    public PageResponse<RefundResponse> getAllRefunds(Pageable pageable) {
        Page<Refund> refunds = refundRepository.findAll(pageable);
        Page<RefundResponse> responsePage = refunds.map(RefundResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<RefundResponse> getRefundsByStatus(RefundStatus status, Pageable pageable) {
        Page<Refund> refunds = refundRepository.findByStatus(status, pageable);
        Page<RefundResponse> responsePage = refunds.map(RefundResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefundById(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));
        return RefundResponse.from(refund);
    }

    @Transactional
    public RefundResponse retryRefund(Long refundId) {
        log.info("환불 재시도: refundId={}", refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));

        if (!refund.canRetry()) {
            throw new RefundDomainException(RefundDomainExceptionCode.InvalidRefundStatusException);
        }

        try {
            refund.process();
            refund.complete();

            RefundCompletedEvent completedEvent = RefundCompletedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .refundId(refund.getId())
                    .refundNumber(refund.getRefundNumber())
                    .orderId(refund.getOrderId())
                    .cancelId(refund.getCancelId())
                    .paymentId(refund.getPaymentId())
                    .userId(refund.getUserId())
                    .amount(refund.getAmount())
                    .build();

            refundEventProducer.sendRefundCompletedEvent(completedEvent);
            log.info("환불 재시도 성공: refundId={}", refundId);

        } catch (Exception e) {
            refund.fail(e.getMessage());

            RefundFailedEvent failedEvent = RefundFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .refundId(refund.getId())
                    .refundNumber(refund.getRefundNumber())
                    .orderId(refund.getOrderId())
                    .cancelId(refund.getCancelId())
                    .paymentId(refund.getPaymentId())
                    .userId(refund.getUserId())
                    .amount(refund.getAmount())
                    .reason(e.getMessage())
                    .build();

            refundEventProducer.sendRefundFailedEvent(failedEvent);
            log.error("환불 재시도 실패: refundId={}", refundId, e);
        }

        return RefundResponse.from(refund);
    }

    @Transactional
    public RefundResponse updateRefundStatus(Long refundId, RefundStatus newStatus) {
        log.info("환불 상태 변경 시도: refundId={}, newStatus={}", refundId, newStatus);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RefundDomainException(RefundDomainExceptionCode.RefundNotFoundException));

        refund.updateStatus(newStatus);
        log.info("환불 상태 변경 완료: refundId={}, status={}", refundId, newStatus);

        return RefundResponse.from(refund);
    }
}
