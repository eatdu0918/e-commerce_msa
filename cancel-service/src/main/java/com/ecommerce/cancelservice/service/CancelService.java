package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.client.OrderServiceClient;
import com.ecommerce.cancelservice.client.dto.OrderPayload;
import com.ecommerce.cancelservice.dto.request.CancelItemRequest;
import com.ecommerce.cancelservice.dto.request.CreateCancelRequest;
import com.ecommerce.cancelservice.dto.response.CancelItemResponse;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSummaryResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSyncResponse;
import com.ecommerce.common.response.PageResponse;
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
import com.ecommerce.common.response.ApiResponse;
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
    private final com.ecommerce.cancelservice.client.PaymentServiceClient paymentServiceClient;

    @Transactional
    public CancelResponse createCancel(Long userId, CreateCancelRequest request) {
        log.info("?  ???    ??   ??  : userId={}, orderId={}", userId, request.getOrderId());

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
        log.info("?  ???    ??   ?   : cancelId={}, cancelNumber={}",
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
            /* UI      ??   ???   +??   ???     ?DELIVERED??????  ??DB???    PENDING ?    ????   */
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

    /**
     * payment-service에서 orderId로 paymentId를 조회합니다.
     * 조회 실패 시 null을 반환합니다 (환불 처리 흐름에서 별도 처리).
     */
    private Long fetchPaymentIdByOrderId(Long orderId) {
        try {
            com.ecommerce.common.response.ApiResponse<com.ecommerce.cancelservice.client.dto.PaymentPayload> res =
                    paymentServiceClient.getPaymentByOrderId(orderId);
            if (res != null && res.isSuccess() && res.getData() != null) {
                return res.getData().getId();
            }
        } catch (Exception e) {
            log.warn("payment-service에서 paymentId 조회 실패: orderId={}", orderId, e);
        }
        return null;
    }

    private static String normalizeStatus(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase();
    }

    /**
     * ?  ???    ??      ??  ??     ??  ?? Feign ??   ??null(?  ??????    ? ?.
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
            log.warn("     ??  ??   ????   ??  ????   ???    ????  : orderId={}, admin={}", orderId, admin, e);
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
     * ?? ??    ??    ?   ???  ???    ?  ?  ?       ??    ??   ??       ??      ?   ?? UI ??  ?   .
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
     * ?  ?     rder-service    ??? JWT ???? ?      ????? ? ??? ???  ?   )? ?  ??       ???  ??    ??   ??   ???  .
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

    /**      ?    UI  rder-service    ??     ? ? ?+ ?    ?    ?   ???? ??*/
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
     * ?  ?        ? ?    ?    ?   (?  ?????  ??/       ??  )       ?   .
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
        log.info("?  ???  ????  : cancelId={}", cancelId);

        Cancel cancel = cancelRepository.findByIdWithItems(cancelId)
                .orElseThrow(() -> new CancelDomainException(CancelDomainExceptionCode.CancelNotFoundException));

        if (!cancel.isRequested()) {
            throw new CancelDomainException(CancelDomainExceptionCode.CancelNotInRequestedStatusException);
        }

        OrderPayload orderSnapshot = fetchAdminOrderOrThrow(cancel.getOrderId());
        assertAdminMayProcessCancel(orderSnapshot);

        cancel.approve();

        BigDecimal refundAmount = RefundAmountCalculator.computeRefundAmount(cancel, orderSnapshot);

        Long paymentId = fetchPaymentIdByOrderId(cancel.getOrderId());

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
                .paymentId(paymentId)
                .refundAmount(refundAmount)
                .requestType(cancel.getRequestType())
                .items(items)
                .build();

        outboxEventPublisher.publishCancelApprovedEvent(event);
        log.info("?  ???  ???   : cancelId={}", cancelId);

        return CancelResponse.from(cancel, pricedItemResponses(cancel, orderSnapshot));
    }

    @Transactional
    public CancelResponse rejectCancel(Long cancelId, String rejectedReason) {
        log.info("?  ??   ? ??  : cancelId={}", cancelId);

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
        log.info("?  ??   ? ?   : cancelId={}", cancelId);

        return CancelResponse.from(cancel, pricedItemResponses(cancel, orderPayload));
    }
}
