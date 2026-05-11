package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.CancelServiceClient;
import com.ecommerce.orderservice.client.PaymentServiceClient;
import com.ecommerce.orderservice.client.ProductServiceClient;
import com.ecommerce.orderservice.client.dto.OrderCancelSummaryResponse;
import com.ecommerce.orderservice.client.dto.OrderCancelSyncResponse;
import com.ecommerce.orderservice.client.dto.PaymentInfo;
import com.ecommerce.orderservice.client.dto.ProductInfo;
import com.ecommerce.orderservice.dto.OrderProgressStatusResolver;
import com.ecommerce.orderservice.dto.response.*;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAggregationService {

    private final OrderService orderService;
    private final ProductServiceClient productServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final CancelServiceClient cancelServiceClient;

    public PageResponse<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        PageResponse<OrderResponse> page = orderService.getMyOrders(userId, pageable);
        List<OrderResponse> enriched = page.getContent().stream()
                .map(this::withPaymentStatus)
                .toList();
        return PageResponse.<OrderResponse>builder()
                .content(enriched)
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * ??           ?  ?      ?   ?? ??  ??      ?  ??   ???  ?   ??progressStatus   ?  ? ???
     * (cancel-service ?   ??DB ?    ?  ???   ??    ??  ?? ??  ??      ??   ?)
     */
    public OrderResponse getMyOrderEnriched(Long orderId, Long userId) {
        OrderResponse order = orderService.getOrder(orderId, userId);
        return withPaymentStatus(order);
    }

    public OrderDetailResponse getOrderDetail(Long orderId, Long userId) {
        log.info("주문 상세 정보 조회 요청 orderId={}, userId={}", orderId, userId);

        OrderResponse order = orderService.getOrder(orderId, userId);
        PaymentInfo paymentInfo = fetchPaymentInfo(orderId);
        order = reconcileOrderFinancialsFromPayment(order, paymentInfo);
        List<OrderItemDetailResponse> enrichedItems = enrichOrderItems(order.getItems());
        OrderCancelSyncResponse cancelSync = fetchCancelSyncUser(orderId);
        OrderCancelSummaryResponse cancelSummary =
                cancelSync != null ? cancelSync.getActiveCancel() : null;

        return OrderDetailResponse.from(
                order,
                enrichedItems,
                paymentInfo,
                statusOf(cancelSummary),
                cancelIdOf(cancelSummary),
                requestTypeOf(cancelSummary),
                cancelSync != null && cancelSync.isHasRejectedOrderCancelRequest(),
                cancelSync != null && cancelSync.isHasRejectedReturnRefundRequest());
    }

    public OrderDetailResponse getOrderDetailAdmin(Long orderId) {
        log.info("관리자 주문 상세 정보 조회 요청 orderId={}", orderId);

        OrderResponse order = orderService.getOrderById(orderId);
        PaymentInfo paymentInfo = fetchPaymentInfo(orderId);
        order = reconcileOrderFinancialsFromPayment(order, paymentInfo);
        List<OrderItemDetailResponse> enrichedItems = enrichOrderItems(order.getItems());
        OrderCancelSyncResponse cancelSync = fetchCancelSyncAdmin(orderId);
        OrderCancelSummaryResponse cancelSummary =
                cancelSync != null ? cancelSync.getActiveCancel() : null;

        return OrderDetailResponse.from(
                order,
                enrichedItems,
                paymentInfo,
                statusOf(cancelSummary),
                cancelIdOf(cancelSummary),
                requestTypeOf(cancelSummary),
                cancelSync != null && cancelSync.isHasRejectedOrderCancelRequest(),
                cancelSync != null && cancelSync.isHasRejectedReturnRefundRequest());
    }

    public PageResponse<OrderResponse> getAllOrdersForAdmin(Pageable pageable) {
        PageResponse<OrderResponse> page = orderService.getAllOrders(pageable);
        return enrichAdminOrderPage(page);
    }

    public PageResponse<OrderResponse> getOrdersByStatusForAdmin(OrderStatus status, Pageable pageable) {
        PageResponse<OrderResponse> page = orderService.getOrdersByStatus(status, pageable);
        return enrichAdminOrderPage(page);
    }

    public OrderResponse getOrderByIdForAdmin(Long orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return withPaymentStatusForAdmin(order);
    }

    private PageResponse<OrderResponse> enrichAdminOrderPage(PageResponse<OrderResponse> page) {
        List<OrderResponse> enriched = page.getContent().stream()
                .map(this::withPaymentStatusForAdmin)
                .toList();
        return PageResponse.<OrderResponse>builder()
                .content(enriched)
                .pageNumber(page.getPageNumber())
                .pageSize(page.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private OrderResponse withPaymentStatusForAdmin(OrderResponse order) {
        PaymentInfo info = fetchPaymentInfo(order.getId());
        String paymentStatus = info != null ? info.getStatus() : null;
        OrderCancelSyncResponse cancelSync = fetchCancelSyncAdmin(order.getId());
        OrderCancelSummaryResponse cancelSummary =
                cancelSync != null ? cancelSync.getActiveCancel() : null;
        String activeCancel = statusOf(cancelSummary);
        OrderResponse built = order.toBuilder()
                .paymentStatus(paymentStatus)
                .paymentAmount(info != null ? info.getAmount() : null)
                .activeCancelStatus(activeCancel)
                .activeCancelId(cancelIdOf(cancelSummary))
                .activeCancelRequestType(requestTypeOf(cancelSummary))
                .progressStatus(OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                        order.getStatus(),
                        paymentStatus,
                        activeCancel,
                        order.isSkipConfirmAndPreparing(),
                        order.isSkipShippingAndDelivered()))
                .build();
        return reconcileOrderFinancialsFromPayment(built, info);
    }

    private List<OrderItemDetailResponse> enrichOrderItems(List<OrderItemResponse> items) {
        List<OrderItemDetailResponse> enrichedItems = new ArrayList<>();

        if (items == null) return enrichedItems;

        for (OrderItemResponse item : items) {
            ProductInfo productInfo = fetchProductInfo(item.getProductId());
            enrichedItems.add(OrderItemDetailResponse.from(item, productInfo));
        }

        return enrichedItems;
    }

    private ProductInfo fetchProductInfo(Long productId) {
        try {
            ApiResponse<ProductInfo> response = productServiceClient.getProduct(productId);
            return response != null && response.isSuccess() ? response.getData() : null;
        } catch (Exception e) {
            log.warn("?  ? ?       ????  : productId={}, error={}", productId, e.getMessage());
            return null;
        }
    }

    private PaymentInfo fetchPaymentInfo(Long orderId) {
        try {
            ApiResponse<PaymentInfo> response = paymentServiceClient.getPaymentByOrderId(orderId);
            return response != null && response.isSuccess() ? response.getData() : null;
        } catch (Exception e) {
            log.warn("   ???       ????  : orderId={}, error={}", orderId, e.getMessage());
            return null;
        }
    }

    private OrderCancelSyncResponse fetchCancelSyncUser(Long orderId) {
        try {
            ApiResponse<OrderCancelSyncResponse> response = cancelServiceClient.getCancelSyncForOrder(orderId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("?  ????  ??   ????  : orderId={}, error={}", orderId, e.getMessage());
        }
        return null;
    }

    private OrderCancelSyncResponse fetchCancelSyncAdmin(Long orderId) {
        try {
            ApiResponse<OrderCancelSyncResponse> response =
                    cancelServiceClient.getCancelSyncForOrderAdmin(orderId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
        } catch (Exception e) {
            log.warn("?  ?   ???  ????  ??   ????  : orderId={}, error={}", orderId, e.getMessage());
        }
        return null;
    }

    private static String statusOf(OrderCancelSummaryResponse summary) {
        return summary != null ? summary.getStatus() : null;
    }

    private static Long cancelIdOf(OrderCancelSummaryResponse summary) {
        return summary != null ? summary.getCancelId() : null;
    }

    private OrderResponse withPaymentStatus(OrderResponse order) {
        PaymentInfo info = fetchPaymentInfo(order.getId());
        String paymentStatus = info != null ? info.getStatus() : null;
        OrderCancelSyncResponse cancelSync = fetchCancelSyncUser(order.getId());
        OrderCancelSummaryResponse cancelSummary =
                cancelSync != null ? cancelSync.getActiveCancel() : null;
        String activeCancel = statusOf(cancelSummary);
        OrderResponse built = order.toBuilder()
                .paymentStatus(paymentStatus)
                .paymentAmount(info != null ? info.getAmount() : null)
                .activeCancelStatus(activeCancel)
                .activeCancelId(cancelIdOf(cancelSummary))
                .activeCancelRequestType(requestTypeOf(cancelSummary))
                .progressStatus(OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                        order.getStatus(),
                        paymentStatus,
                        activeCancel,
                        order.isSkipConfirmAndPreparing(),
                        order.isSkipShippingAndDelivered()))
                .build();
        return reconcileOrderFinancialsFromPayment(built, info);
    }

    /**
     * DB???   ????  ???  ????   ?  ??    ???  ? ??    ? ???    ? API ?   ???      ?      ??   ???   ??   ?      ?    ??.
     * ?  ????   ?   ???          ????  ????       ??       ? ???? ?COMPLETED???   ??CANCELLED  EFUNDED? ? ??   ???
     */
    private OrderResponse reconcileOrderFinancialsFromPayment(OrderResponse order, PaymentInfo payment) {
        if (payment == null || payment.getAmount() == null) {
            return order;
        }
        String payStatus = payment.getStatus();
        if (payStatus == null || !isPaymentAmountComparableToOrderTotal(payStatus)) {
            return order;
        }
        BigDecimal total = order.getTotalAmount();
        BigDecimal paid = payment.getAmount();
        BigDecimal disc = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        if (total == null || paid.compareTo(BigDecimal.ZERO) <= 0) {
            return order;
        }
        if (disc.compareTo(BigDecimal.ZERO) > 0) {
            return order;
        }
        if (paid.compareTo(total) >= 0) {
            return order;
        }
        BigDecimal implied = total.subtract(paid);
        if (implied.compareTo(BigDecimal.ZERO) <= 0) {
            return order;
        }
        return order.toBuilder()
                .discountAmount(implied)
                .finalAmount(paid)
                .build();
    }

    /**
     * {@link PaymentInfo#getAmount()}         ??  ??????    ?   ????????   ?   ? ? ? ? ?
     */
    private static boolean isPaymentAmountComparableToOrderTotal(String payStatus) {
        String s = payStatus.trim().toUpperCase(Locale.ROOT);
        return "COMPLETED".equals(s) || "CANCELLED".equals(s) || "REFUNDED".equals(s);
    }

    private static String requestTypeOf(OrderCancelSummaryResponse summary) {
        if (summary == null || summary.getRequestType() == null || summary.getRequestType().isBlank()) {
            return null;
        }
        return summary.getRequestType().trim();
    }
}
