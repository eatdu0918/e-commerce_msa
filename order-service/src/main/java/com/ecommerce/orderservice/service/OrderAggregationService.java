package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.CancelServiceClient;
import com.ecommerce.orderservice.client.PaymentServiceClient;
import com.ecommerce.orderservice.client.ProductServiceClient;
import com.ecommerce.orderservice.client.dto.OrderCancelSummaryResponse;
import com.ecommerce.orderservice.client.dto.PaymentInfo;
import com.ecommerce.orderservice.client.dto.ProductInfo;
import com.ecommerce.orderservice.dto.OrderProgressStatusResolver;
import com.ecommerce.orderservice.dto.response.*;
import com.ecommerce.orderservice.response.ApiResponse;
import com.ecommerce.orderservice.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public OrderDetailResponse getOrderDetail(Long orderId, Long userId) {
        log.info("주문 상세 통합 조회: orderId={}, userId={}", orderId, userId);

        OrderResponse order = orderService.getOrder(orderId, userId);
        List<OrderItemDetailResponse> enrichedItems = enrichOrderItems(order.getItems());
        PaymentInfo paymentInfo = fetchPaymentInfo(orderId);
        String activeCancel = fetchActiveCancelStatus(orderId);

        return OrderDetailResponse.from(order, enrichedItems, paymentInfo, activeCancel);
    }

    public OrderDetailResponse getOrderDetailAdmin(Long orderId) {
        log.info("관리자 주문 상세 통합 조회: orderId={}", orderId);

        OrderResponse order = orderService.getOrderById(orderId);
        List<OrderItemDetailResponse> enrichedItems = enrichOrderItems(order.getItems());
        PaymentInfo paymentInfo = fetchPaymentInfo(orderId);
        String activeCancel = fetchActiveCancelStatusAdmin(orderId);

        return OrderDetailResponse.from(order, enrichedItems, paymentInfo, activeCancel);
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
        String activeCancel = fetchActiveCancelStatusAdmin(order.getId());
        return order.toBuilder()
                .paymentStatus(paymentStatus)
                .activeCancelStatus(activeCancel)
                .progressStatus(OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                        order.getStatus(), paymentStatus, activeCancel))
                .build();
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
            log.warn("상품 정보 조회 실패: productId={}, error={}", productId, e.getMessage());
            return null;
        }
    }

    private PaymentInfo fetchPaymentInfo(Long orderId) {
        try {
            ApiResponse<PaymentInfo> response = paymentServiceClient.getPaymentByOrderId(orderId);
            return response != null && response.isSuccess() ? response.getData() : null;
        } catch (Exception e) {
            log.warn("결제 정보 조회 실패: orderId={}, error={}", orderId, e.getMessage());
            return null;
        }
    }

    private String fetchActiveCancelStatus(Long orderId) {
        try {
            ApiResponse<OrderCancelSummaryResponse> response =
                    cancelServiceClient.getActiveCancelForOrder(orderId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getStatus();
            }
        } catch (Exception e) {
            log.warn("진행 중 취소 요약 조회 실패: orderId={}, error={}", orderId, e.getMessage());
        }
        return null;
    }

    private String fetchActiveCancelStatusAdmin(Long orderId) {
        try {
            ApiResponse<OrderCancelSummaryResponse> response =
                    cancelServiceClient.getActiveCancelForOrderAdmin(orderId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getStatus();
            }
        } catch (Exception e) {
            log.warn("관리자용 진행 중 취소 요약 조회 실패: orderId={}, error={}", orderId, e.getMessage());
        }
        return null;
    }

    private OrderResponse withPaymentStatus(OrderResponse order) {
        PaymentInfo info = fetchPaymentInfo(order.getId());
        String paymentStatus = info != null ? info.getStatus() : null;
        String activeCancel = fetchActiveCancelStatus(order.getId());
        return order.toBuilder()
                .paymentStatus(paymentStatus)
                .activeCancelStatus(activeCancel)
                .progressStatus(OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                        order.getStatus(), paymentStatus, activeCancel))
                .build();
    }
}
