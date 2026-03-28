package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.CancelServiceClient;
import com.ecommerce.orderservice.client.PaymentServiceClient;
import com.ecommerce.orderservice.client.dto.OrderCancelSummaryResponse;
import com.ecommerce.orderservice.client.dto.PaymentInfo;
import com.ecommerce.orderservice.dto.OrderProgressStatusResolver;
import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.dto.response.PageResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.orderservice.event.OrderCancelledEvent;
import com.ecommerce.orderservice.event.OrderCreatedEvent;
import com.ecommerce.orderservice.exception.OrderDomainException;
import com.ecommerce.orderservice.exception.OrderDomainExceptionCode;
import com.ecommerce.orderservice.outbox.OutboxEventPublisher;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final PaymentServiceClient paymentServiceClient;
    private final CancelServiceClient cancelServiceClient;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.info("주문 생성 시도: userId={}", userId);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new OrderDomainException(OrderDomainExceptionCode.EmptyOrderItemsException);
        }

        BigDecimal totalAmount = calculateTotalAmount(request.getItems());

        Order order = Order.create(
                userId,
                totalAmount,
                request.getUserCouponId(),
                request.getShippingAddress(),
                request.getRecipientName(),
                request.getRecipientPhone()
        );

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem orderItem = OrderItem.create(
                    itemRequest.getProductId(),
                    itemRequest.getProductName(),
                    itemRequest.getImageUrl(),
                    itemRequest.getUnitPrice(),
                    itemRequest.getQuantity()
            );
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료 (PENDING): orderId={}, orderNumber={}",
                savedOrder.getId(), savedOrder.getOrderNumber());

        OrderCreatedEvent event = createOrderCreatedEvent(savedOrder);
        outboxEventPublisher.publishOrderCreatedEvent(event);

        return OrderResponse.from(savedOrder);
    }

    private BigDecimal calculateTotalAmount(List<OrderItemRequest> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderCreatedEvent createOrderCreatedEvent(Order order) {
        List<OrderCreatedEvent.OrderItemEvent> items = order.getOrderItems().stream()
                .map(item -> OrderCreatedEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .build())
                .toList();

        return OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .userCouponId(order.getUserCouponId())
                .items(items)
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        Page<OrderResponse> responsePage = orders.map(OrderResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        log.info("주문 취소 시도: orderId={}, userId={}", orderId, userId);

        Order order = orderRepository.findByIdAndUserIdWithItems(orderId, userId)
                .orElseThrow(() -> new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

        if (!order.canCancel()) {
            throw new OrderDomainException(OrderDomainExceptionCode.OrderCannotBeCancelledException);
        }

        order.cancel();

        List<OrderCancelledEvent.OrderItemEvent> items = order.getOrderItems().stream()
                .map(item -> OrderCancelledEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userCouponId(order.getUserCouponId())
                .items(items)
                .build();

        outboxEventPublisher.publishOrderCancelledEvent(event);
        log.info("주문 취소 완료: orderId={}", orderId);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderResponse> responsePage = orders.map(OrderResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        Page<OrderResponse> responsePage = orders.map(OrderResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("주문 상태 변경 시도: orderId={}, newStatus={}", orderId, newStatus);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

        if (!order.canUpdateStatus()) {
            throw new OrderDomainException(OrderDomainExceptionCode.OrderCannotBeUpdatedException);
        }

        if (order.getStatus() != newStatus) {
            assertAdminFulfillmentNotBlockedByCancelOrRefund(orderId);
        }

        validateAdminFulfillmentTransition(order.getStatus(), newStatus);

        order.updateStatus(newStatus);
        log.info("주문 상태 변경 완료: orderId={}, status={}", orderId, newStatus);

        return OrderResponse.from(order);
    }

    /**
     * 관리자 배송·준비 단계: 한 단계씩만 전환 (PENDING→CONFIRMED→PREPARING→SHIPPING→DELIVERED).
     */
    private void validateAdminFulfillmentTransition(OrderStatus current, OrderStatus next) {
        if (current == next) {
            return;
        }
        OrderStatus allowedNext = switch (current) {
            case PENDING -> OrderStatus.CONFIRMED;
            case CONFIRMED -> OrderStatus.PREPARING;
            case PREPARING -> OrderStatus.SHIPPING;
            case SHIPPING -> OrderStatus.DELIVERED;
            default -> null;
        };
        if (allowedNext != next) {
            throw new OrderDomainException(OrderDomainExceptionCode.OrderStatusTransitionNotAllowedException);
        }
    }

    private void assertAdminFulfillmentNotBlockedByCancelOrRefund(Long orderId) {
        String paymentStatus = fetchPaymentStatusForAdminGuard(orderId);
        String activeCancel = fetchActiveCancelStatusForAdminGuard(orderId);
        if (OrderProgressStatusResolver.blocksAdminFulfillmentAdvance(paymentStatus, activeCancel)) {
            throw new OrderDomainException(OrderDomainExceptionCode.OrderFulfillmentBlockedByCancelOrRefundException);
        }
    }

    private String fetchPaymentStatusForAdminGuard(Long orderId) {
        try {
            ApiResponse<PaymentInfo> response = paymentServiceClient.getPaymentByOrderId(orderId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getStatus();
            }
        } catch (Exception e) {
            log.warn("관리자 배송 단계 검증용 결제 조회 실패: orderId={}, error={}", orderId, e.getMessage());
        }
        return null;
    }

    private String fetchActiveCancelStatusForAdminGuard(Long orderId) {
        try {
            ApiResponse<OrderCancelSummaryResponse> response =
                    cancelServiceClient.getActiveCancelForOrderAdmin(orderId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData().getStatus();
            }
        } catch (Exception e) {
            log.warn("관리자 배송 단계 검증용 취소 요약 조회 실패: orderId={}, error={}", orderId, e.getMessage());
        }
        return null;
    }
}
