package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.client.CancelServiceClient;
import com.ecommerce.orderservice.client.PaymentServiceClient;
import com.ecommerce.orderservice.client.dto.OrderCancelSummaryResponse;
import com.ecommerce.orderservice.client.dto.PaymentInfo;
import com.ecommerce.orderservice.dto.OrderProgressStatusResolver;
import com.ecommerce.orderservice.dto.request.CreateOrderRequest;
import com.ecommerce.orderservice.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.orderservice.entity.Order;
import com.ecommerce.orderservice.entity.OrderItem;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.orderservice.event.OrderCancelledEvent;
import com.ecommerce.orderservice.event.OrderCreatedEvent;
import com.ecommerce.orderservice.exception.OrderDomainException;
import com.ecommerce.orderservice.exception.OrderDomainExceptionCode;
import com.ecommerce.orderservice.outbox.OutboxEventPublisher;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.common.response.ApiResponse;
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
        log.info("     ??   ??  : userId={}", userId);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new OrderDomainException(OrderDomainExceptionCode.EmptyOrderItemsException);
        }

        BigDecimal totalAmount = calculateTotalAmount(request.getItems());

        boolean skipConfirmAndPreparing = Boolean.TRUE.equals(request.getSkipConfirmAndPreparing());
        boolean skipShippingAndDelivered = Boolean.TRUE.equals(request.getSkipShippingAndDelivered());

        Order order = Order.create(
                userId,
                totalAmount,
                request.getUserCouponId(),
                request.getShippingAddress(),
                request.getRecipientName(),
                request.getRecipientPhone(),
                skipConfirmAndPreparing,
                skipShippingAndDelivered
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
        log.info("     ??   ?    (PENDING): orderId={}, orderNumber={}",
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
        log.info("     ?  ????  : orderId={}, userId={}", orderId, userId);

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
        log.info("     ?  ???   : orderId={}", orderId);

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
        log.info("     ?        ???  : orderId={}, newStatus={}", orderId, newStatus);

        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderDomainException(OrderDomainExceptionCode.OrderNotFoundException));

        if (!order.canUpdateStatus()) {
            throw new OrderDomainException(OrderDomainExceptionCode.OrderCannotBeUpdatedException);
        }

        if (order.getStatus() != newStatus) {
            assertAdminFulfillmentNotBlockedByCancelOrRefund(orderId);
        }

        validateAdminFulfillmentTransition(order, newStatus);

        order.updateStatus(newStatus);
        log.info("     ?        ??   : orderId={}, status={}", orderId, newStatus);

        return OrderResponse.from(order);
    }

    /**
     * ?  ?       ?     ????  ?    ??? ????  ???   .
     *     ?   ? ?     ? ??    ?  ?    ????   ??        ?? ?  ?       ?  ?    ?? ?   ? ?   ?   ??   ?  ?   ??????  .
     *        ??    ?? ??   ??        ??     ??   ? ?     ???    ???    ?   ??????  .
     */
    private void validateAdminFulfillmentTransition(Order order, OrderStatus next) {
        OrderStatus current = order.getStatus();
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
        if (allowedNext == next) {
            return;
        }
        if (order.isSkipConfirmAndPreparing() && next == OrderStatus.SHIPPING) {
            if (current == OrderStatus.PENDING || current == OrderStatus.CONFIRMED) {
                return;
            }
        }
        if (order.isSkipShippingAndDelivered() && order.isSkipConfirmAndPreparing()) {
            if ((current == OrderStatus.CONFIRMED || current == OrderStatus.PREPARING)
                    && next == OrderStatus.DELIVERED) {
                return;
            }
        }
        /*
         * ?  ?       ???    ?   : ?  ????   ??  ?CONFIRMED~)? ?  ??    ?DELIVERED ?    ??   ??
         * PENDING(        ????? ??  .
         */
        if (next == OrderStatus.DELIVERED
                && (current == OrderStatus.CONFIRMED
                    || current == OrderStatus.PREPARING
                    || current == OrderStatus.SHIPPING)) {
            return;
        }
        throw new OrderDomainException(OrderDomainExceptionCode.OrderStatusTransitionNotAllowedException);
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
            log.warn("?  ?       ????  ?      ??   ??   ????  : orderId={}, error={}", orderId, e.getMessage());
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
            log.warn("?  ?       ????  ?      ???  ???       ????  : orderId={}, error={}", orderId, e.getMessage());
        }
        return null;
    }
}
