package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.dto.request.CancelItemRequest;
import com.ecommerce.cancelservice.dto.request.CreateCancelRequest;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSummaryResponse;
import com.ecommerce.cancelservice.dto.response.PageResponse;
import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.entity.CancelItem;
import com.ecommerce.cancelservice.enums.CancelStatus;
import com.ecommerce.cancelservice.event.CancelApprovedEvent;
import com.ecommerce.cancelservice.event.CancelRejectedEvent;
import com.ecommerce.cancelservice.event.CancelRequestedEvent;
import com.ecommerce.cancelservice.exception.CancelDomainException;
import com.ecommerce.cancelservice.exception.CancelDomainExceptionCode;
import com.ecommerce.cancelservice.outbox.OutboxEventPublisher;
import com.ecommerce.cancelservice.repository.CancelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelService {

    private static final Set<CancelStatus> BLOCK_DUPLICATE_CREATE_STATUSES =
            EnumSet.of(CancelStatus.REQUESTED, CancelStatus.APPROVED, CancelStatus.COMPLETED);

    private final CancelRepository cancelRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public CancelResponse createCancel(Long userId, CreateCancelRequest request) {
        log.info("취소 요청 생성 시도: userId={}, orderId={}", userId, request.getOrderId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new CancelDomainException(CancelDomainExceptionCode.EmptyCancelItemsException);
        }

        if (cancelRepository.existsByOrderIdAndUserIdAndStatusIn(
                request.getOrderId(), userId, BLOCK_DUPLICATE_CREATE_STATUSES)) {
            throw new CancelDomainException(CancelDomainExceptionCode.DuplicateCancelRequestException);
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
        outboxEventPublisher.publishCancelRequestedEvent(event);

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

    /**
     * 해당 주문에 진행 중인 취소(요청·승인·완료 처리)가 있으면 요약 반환. 주문 상세와 UI 동기화용.
     */
    @Transactional(readOnly = true)
    public Optional<OrderCancelSummaryResponse> getActiveCancelForOrder(Long orderId, Long userId) {
        return cancelRepository.findFirstByOrderIdAndUserIdAndStatusInOrderByIdDesc(
                        orderId, userId, BLOCK_DUPLICATE_CREATE_STATUSES)
                .map(c -> OrderCancelSummaryResponse.builder()
                        .cancelId(c.getId())
                        .cancelNumber(c.getCancelNumber())
                        .status(c.getStatus())
                        .build());
    }

    /**
     * 관리자·order-service 집계용. JWT 사용자와 주문 소유자가 다를 때(관리자)에도 동일 주문의 취소 진행 상태를 조회한다.
     */
    @Transactional(readOnly = true)
    public Optional<OrderCancelSummaryResponse> getActiveCancelForOrderAdmin(Long orderId) {
        return cancelRepository.findFirstByOrderIdAndStatusInOrderByIdDesc(
                        orderId, BLOCK_DUPLICATE_CREATE_STATUSES)
                .map(c -> OrderCancelSummaryResponse.builder()
                        .cancelId(c.getId())
                        .cancelNumber(c.getCancelNumber())
                        .status(c.getStatus())
                        .build());
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

        outboxEventPublisher.publishCancelApprovedEvent(event);
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

        outboxEventPublisher.publishCancelRejectedEvent(event);
        log.info("취소 거부 완료: cancelId={}", cancelId);

        return CancelResponse.from(cancel);
    }
}
