package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.dto.request.CancelItemRequest;
import com.ecommerce.cancelservice.dto.request.CreateCancelRequest;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.PageResponse;
import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.entity.CancelItem;
import com.ecommerce.cancelservice.enums.CancelStatus;
import com.ecommerce.cancelservice.event.CancelApprovedEvent;
import com.ecommerce.cancelservice.event.CancelRejectedEvent;
import com.ecommerce.cancelservice.event.CancelRequestedEvent;
import com.ecommerce.cancelservice.exception.CancelDomainException;
import com.ecommerce.cancelservice.exception.CancelDomainExceptionCode;
import com.ecommerce.cancelservice.kafka.CancelEventProducer;
import com.ecommerce.cancelservice.repository.CancelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelService {

    private final CancelRepository cancelRepository;
    private final CancelEventProducer cancelEventProducer;

    @Transactional
    public CancelResponse createCancel(Long userId, CreateCancelRequest request) {
        log.info("취소 요청 생성 시도: userId={}, orderId={}", userId, request.getOrderId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new CancelDomainException(CancelDomainExceptionCode.EmptyCancelItemsException);
        }

        Cancel cancel = Cancel.create(
                request.getOrderId(),
                request.getOrderNumber(),
                userId,
                request.getCancelReason(),
                request.getCancelDetail()
        );

        for (CancelItemRequest itemRequest : request.getItems()) {
            CancelItem cancelItem = CancelItem.create(
                    itemRequest.getProductId(),
                    itemRequest.getProductName(),
                    itemRequest.getQuantity(),
                    itemRequest.getUnitPrice()
            );
            cancel.addCancelItem(cancelItem);
        }

        Cancel savedCancel = cancelRepository.save(cancel);
        log.info("취소 요청 생성 완료: cancelId={}, cancelNumber={}",
                savedCancel.getId(), savedCancel.getCancelNumber());

        CancelRequestedEvent event = createCancelRequestedEvent(savedCancel);
        cancelEventProducer.sendCancelRequestedEvent(event);

        return CancelResponse.from(savedCancel);
    }

    private CancelRequestedEvent createCancelRequestedEvent(Cancel cancel) {
        List<CancelRequestedEvent.CancelItemEvent> items = cancel.getCancelItems().stream()
                .map(item -> CancelRequestedEvent.CancelItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        return CancelRequestedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .cancelId(cancel.getId())
                .cancelNumber(cancel.getCancelNumber())
                .orderId(cancel.getOrderId())
                .orderNumber(cancel.getOrderNumber())
                .userId(cancel.getUserId())
                .cancelReason(cancel.getCancelReason().name())
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public CancelResponse getCancel(Long cancelId, Long userId) {
        Cancel cancel = cancelRepository.findByIdAndUserIdWithItems(cancelId, userId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));
        return CancelResponse.from(cancel);
    }

    @Transactional(readOnly = true)
    public PageResponse<CancelResponse> getMyCancels(Long userId, Pageable pageable) {
        Page<Cancel> cancels = cancelRepository.findByUserId(userId, pageable);
        Page<CancelResponse> responsePage = cancels.map(CancelResponse::fromWithoutItems);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CancelResponse> getAllCancels(Pageable pageable) {
        Page<Cancel> cancels = cancelRepository.findAll(pageable);
        Page<CancelResponse> responsePage = cancels.map(CancelResponse::fromWithoutItems);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CancelResponse> getCancelsByStatus(CancelStatus status, Pageable pageable) {
        Page<Cancel> cancels = cancelRepository.findByStatus(status, pageable);
        Page<CancelResponse> responsePage = cancels.map(CancelResponse::fromWithoutItems);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public CancelResponse getCancelById(Long cancelId) {
        Cancel cancel = cancelRepository.findByIdWithItems(cancelId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));
        return CancelResponse.from(cancel);
    }

    @Transactional
    public CancelResponse approveCancel(Long cancelId) {
        log.info("취소 승인 시도: cancelId={}", cancelId);

        Cancel cancel = cancelRepository.findByIdWithItems(cancelId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

        if (!cancel.isRequested()) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelNotInRequestedStatusException);
        }

        cancel.approve();

        List<CancelApprovedEvent.CancelItemEvent> items = cancel.getCancelItems().stream()
                .map(item -> CancelApprovedEvent.CancelItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        CancelApprovedEvent event = CancelApprovedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .cancelId(cancel.getId())
                .cancelNumber(cancel.getCancelNumber())
                .orderId(cancel.getOrderId())
                .orderNumber(cancel.getOrderNumber())
                .userId(cancel.getUserId())
                .items(items)
                .build();

        cancelEventProducer.sendCancelApprovedEvent(event);
        log.info("취소 승인 완료: cancelId={}", cancelId);

        return CancelResponse.from(cancel);
    }

    @Transactional
    public CancelResponse rejectCancel(Long cancelId, String rejectedReason) {
        log.info("취소 거부 시도: cancelId={}", cancelId);

        Cancel cancel = cancelRepository.findByIdWithItems(cancelId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

        if (!cancel.isRequested()) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelNotInRequestedStatusException);
        }

        cancel.reject(rejectedReason);

        CancelRejectedEvent event = CancelRejectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .cancelId(cancel.getId())
                .cancelNumber(cancel.getCancelNumber())
                .orderId(cancel.getOrderId())
                .orderNumber(cancel.getOrderNumber())
                .userId(cancel.getUserId())
                .rejectedReason(rejectedReason)
                .build();

        cancelEventProducer.sendCancelRejectedEvent(event);
        log.info("취소 거부 완료: cancelId={}", cancelId);

        return CancelResponse.from(cancel);
    }
}
