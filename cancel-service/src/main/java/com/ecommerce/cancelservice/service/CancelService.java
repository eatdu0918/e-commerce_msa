package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.client.OrderServiceClient;
import com.ecommerce.cancelservice.client.dto.OrderPayload;
import com.ecommerce.cancelservice.dto.request.CancelItemRequest;
import com.ecommerce.cancelservice.dto.request.CreateCancelRequest;
import com.ecommerce.cancelservice.dto.response.CancelItemResponse;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSummaryResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSyncResponse;
import com.ecommerce.cancelservice.dto.response.PageResponse;
import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.entity.CancelItem;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import com.ecommerce.cancelservice.enums.CancelStatus;
import com.ecommerce.cancelservice.event.CancelApprovedEvent;
import com.ecommerce.cancelservice.event.CancelRejectedEvent;
import com.ecommerce.cancelservice.event.CancelRequestedEvent;
import com.ecommerce.cancelservice.exception.CancelDomainException;
import com.ecommerce.cancelservice.exception.CancelDomainExceptionCode;
import com.ecommerce.cancelservice.outbox.OutboxEventPublisher;
import com.ecommerce.cancelservice.repository.CancelRepository;
import com.ecommerce.cancelservice.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private static final Set<String> PRE_SHIP_ORDER_CANCEL_STATUSES =
            Set.of("PENDING", "CONFIRMED", "PREPARING");

    private final CancelRepository cancelRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final OrderServiceClient orderServiceClient;

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

        CancelRequestType resolvedType =
                request.getRequestType() != null ? request.getRequestType() : CancelRequestType.ORDER_CANCEL;

        if (cancelRepository.existsByOrderIdAndUserIdAndStatusAndRequestType(
                request.getOrderId(), userId, CancelStatus.REJECTED, resolvedType)) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelRequestBlockedAfterRejectionException);
        }

        OrderPayload order = fetchUserOrderOrThrow(request.getOrderId());
        assertUserCancelAllowed(order, resolvedType);

        Cancel cancel = Cancel.create(
                request.getOrderId(),
                request.getOrderNumber(),
                userId,
                request.getCancelReason(),
                request.getCancelDetail(),
                request.getRequestType()
        );

        for (CancelItemRequest itemRequest : request.getItems()) {
            BigDecimal unitPrice = OrderDiscountPricing.discountedUnitPriceForCancelQuantity(
                    order,
                    itemRequest.getProductId(),
                    itemRequest.getQuantity(),
                    itemRequest.getUnitPrice());
            CancelItem cancelItem = CancelItem.create(
                    itemRequest.getProductId(),
                    itemRequest.getProductName(),
                    itemRequest.getQuantity(),
                    unitPrice
            );
            cancel.addCancelItem(cancelItem);
        }

        Cancel savedCancel = cancelRepository.save(cancel);
        log.info("취소 요청 생성 완료: cancelId={}, cancelNumber={}",
                savedCancel.getId(), savedCancel.getCancelNumber());

        CancelRequestedEvent event = createCancelRequestedEvent(savedCancel);
        outboxEventPublisher.publishCancelRequestedEvent(event);

        return CancelResponse.from(savedCancel, pricedItemResponses(savedCancel, order));
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
                .requestType(cancel.getRequestType().name())
                .items(items)
                .build();
    }

    private OrderPayload fetchUserOrderOrThrow(Long orderId) {
        ApiResponse<OrderPayload> res = orderServiceClient.getMyOrder(orderId);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new CancelDomainException(CancelDomainExceptionCode.OrderInfoUnavailableException);
        }
        return res.getData();
    }

    private void assertUserCancelAllowed(OrderPayload payload, CancelRequestType type) {
        String st = normalizeStatus(payload.getStatus());
        String progress = normalizeStatus(payload.getProgressStatus());
        if ("SHIPPING".equals(st)) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelBlockedWhileShippingException);
        }
        if (type == CancelRequestType.RETURN_REFUND) {
            /* UI·집계는 결제 완료+스킵 플래그로 DELIVERED일 수 있는데 DB는 아직 PENDING 등일 수 있음 */
            if (!"DELIVERED".equals(st) && !"DELIVERED".equals(progress)) {
                throw new CancelDomainException(CancelDomainExceptionCode.ReturnRefundOnlyAfterDeliveredException);
            }
        } else {
            if (!PRE_SHIP_ORDER_CANCEL_STATUSES.contains(st)) {
                throw new CancelDomainException(CancelDomainExceptionCode.OrderCancelOnlyBeforeShippingException);
            }
        }
    }

    private OrderPayload fetchAdminOrderOrThrow(Long orderId) {
        ApiResponse<OrderPayload> res = orderServiceClient.getAdminOrder(orderId);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new CancelDomainException(CancelDomainExceptionCode.OrderInfoUnavailableException);
        }
        return res.getData();
    }

    private void assertAdminMayProcessCancel(OrderPayload p) {
        String st = normalizeStatus(p.getStatus());
        String before = normalizeStatus(p.getStatusBeforeCancelRequest());
        if ("SHIPPING".equals(st)) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelAdminActionBlockedException);
        }
        if ("CANCEL_REQUESTED".equals(st) && "SHIPPING".equals(before)) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelAdminActionBlockedException);
        }
    }

    private static String normalizeStatus(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase();
    }

    /**
     * 취소 상세 품목가 표시용 주문 스냅샷. Feign 실패 시 null(엔티티 단가 그대로).
     */
    private static List<CancelItemResponse> pricedItemResponses(Cancel cancel, OrderPayload order) {
        if (cancel.getCancelItems() == null) {
            return null;
        }
        if (order == null) {
            return null;
        }
        return cancel.getCancelItems().stream()
                .map(ci -> {
                    BigDecimal unit = OrderDiscountPricing.discountedUnitPriceForCancelQuantity(
                            order,
                            ci.getProductId(),
                            ci.getQuantity(),
                            ci.getUnitPrice());
                    return CancelItemResponse.fromWithUnitPrice(ci, unit);
                })
                .toList();
    }

    private OrderPayload tryFetchOrderForCancelDetail(Long orderId, boolean admin) {
        try {
            ApiResponse<OrderPayload> res = admin
                    ? orderServiceClient.getAdminOrder(orderId)
                    : orderServiceClient.getMyOrder(orderId);
            if (res != null && res.isSuccess() && res.getData() != null) {
                return res.getData();
            }
        } catch (Exception e) {
            log.warn("주문 스냅샷 조회 실패로 취소 품목 단가 보정 생략: orderId={}, admin={}", orderId, admin, e);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public CancelResponse getCancel(Long cancelId, Long userId) {
        Cancel cancel = cancelRepository.findByIdAndUserIdWithItems(cancelId, userId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));
        OrderPayload order = tryFetchOrderForCancelDetail(cancel.getOrderId(), false);
        return CancelResponse.from(cancel, pricedItemResponses(cancel, order));
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
                        .requestType(c.getRequestType())
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
                        .requestType(c.getRequestType())
                        .build());
    }

    /** 주문 상세 UI·order-service 집계: 진행 중 건 + 요청 유형별 거절 이력 */
    @Transactional(readOnly = true)
    public OrderCancelSyncResponse getCancelSyncForOrder(Long orderId, Long userId) {
        Optional<OrderCancelSummaryResponse> active = getActiveCancelForOrder(orderId, userId);
        boolean rejOrderCancel = cancelRepository.existsByOrderIdAndUserIdAndStatusAndRequestType(
                orderId, userId, CancelStatus.REJECTED, CancelRequestType.ORDER_CANCEL);
        boolean rejReturn = cancelRepository.existsByOrderIdAndUserIdAndStatusAndRequestType(
                orderId, userId, CancelStatus.REJECTED, CancelRequestType.RETURN_REFUND);
        return OrderCancelSyncResponse.builder()
                .activeCancel(active.orElse(null))
                .hasRejectedOrderCancelRequest(rejOrderCancel)
                .hasRejectedReturnRefundRequest(rejReturn)
                .build();
    }

    @Transactional(readOnly = true)
    public OrderCancelSyncResponse getCancelSyncForOrderAdmin(Long orderId) {
        Optional<OrderCancelSummaryResponse> active = getActiveCancelForOrderAdmin(orderId);
        boolean rejOrderCancel = cancelRepository.existsByOrderIdAndStatusAndRequestType(
                orderId, CancelStatus.REJECTED, CancelRequestType.ORDER_CANCEL);
        boolean rejReturn = cancelRepository.existsByOrderIdAndStatusAndRequestType(
                orderId, CancelStatus.REJECTED, CancelRequestType.RETURN_REFUND);
        return OrderCancelSyncResponse.builder()
                .activeCancel(active.orElse(null))
                .hasRejectedOrderCancelRequest(rejOrderCancel)
                .hasRejectedReturnRefundRequest(rejReturn)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<CancelResponse> getMyCancels(Long userId, Pageable pageable) {
        Page<Cancel> cancels = cancelRepository.findByUserId(userId, pageable);
        Page<CancelResponse> responsePage = cancels.map(CancelResponse::fromWithoutItems);
        return PageResponse.from(responsePage);
    }

    /**
     * 관리자 목록: 상태·요청 유형(출고 전 취소 / 반품·환불) 조합 필터.
     */
    @Transactional(readOnly = true)
    public PageResponse<CancelResponse> getAdminCancels(
            CancelStatus status, CancelRequestType requestType, Pageable pageable) {
        Page<Cancel> cancels;
        if (status != null && requestType != null) {
            cancels = cancelRepository.findByStatusAndRequestType(status, requestType, pageable);
        } else if (status != null) {
            cancels = cancelRepository.findByStatus(status, pageable);
        } else if (requestType != null) {
            cancels = cancelRepository.findByRequestType(requestType, pageable);
        } else {
            cancels = cancelRepository.findAll(pageable);
        }
        Page<CancelResponse> responsePage = cancels.map(CancelResponse::fromWithoutItems);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<CancelResponse> getAllCancels(Pageable pageable) {
        return getAdminCancels(null, null, pageable);
    }

    @Transactional(readOnly = true)
    public PageResponse<CancelResponse> getCancelsByStatus(CancelStatus status, Pageable pageable) {
        return getAdminCancels(status, null, pageable);
    }

    @Transactional(readOnly = true)
    public CancelResponse getCancelById(Long cancelId) {
        Cancel cancel = cancelRepository.findByIdWithItems(cancelId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));
        OrderPayload order = tryFetchOrderForCancelDetail(cancel.getOrderId(), true);
        return CancelResponse.from(cancel, pricedItemResponses(cancel, order));
    }

    @Transactional
    public CancelResponse approveCancel(Long cancelId) {
        log.info("취소 승인 시도: cancelId={}", cancelId);

        Cancel cancel = cancelRepository.findByIdWithItems(cancelId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

        if (!cancel.isRequested()) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelNotInRequestedStatusException);
        }

        OrderPayload orderSnapshot = fetchAdminOrderOrThrow(cancel.getOrderId());
        assertAdminMayProcessCancel(orderSnapshot);

        cancel.approve();

        BigDecimal refundAmount = RefundAmountCalculator.computeRefundAmount(cancel, orderSnapshot);

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
                .refundAmount(refundAmount)
                .requestType(cancel.getRequestType())
                .items(items)
                .build();

        outboxEventPublisher.publishCancelApprovedEvent(event);
        log.info("취소 승인 완료: cancelId={}", cancelId);

        return CancelResponse.from(cancel, pricedItemResponses(cancel, orderSnapshot));
    }

    @Transactional
    public CancelResponse rejectCancel(Long cancelId, String rejectedReason) {
        log.info("취소 거부 시도: cancelId={}", cancelId);

        Cancel cancel = cancelRepository.findByIdWithItems(cancelId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

        if (!cancel.isRequested()) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelNotInRequestedStatusException);
        }

        OrderPayload orderPayload = fetchAdminOrderOrThrow(cancel.getOrderId());
        assertAdminMayProcessCancel(orderPayload);

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

        return CancelResponse.from(cancel, pricedItemResponses(cancel, orderPayload));
    }
}
