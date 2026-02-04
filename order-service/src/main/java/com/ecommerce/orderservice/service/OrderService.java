package com.ecommerce.orderservice.service;

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
import com.ecommerce.orderservice.kafka.OrderEventProducer;
import com.ecommerce.orderservice.repository.OrderRepository;
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
    private final OrderEventProducer orderEventProducer;

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
                    itemRequest.getUnitPrice(),
                    itemRequest.getQuantity()
            );
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료 (PENDING): orderId={}, orderNumber={}",
                savedOrder.getId(), savedOrder.getOrderNumber());

        OrderCreatedEvent event = createOrderCreatedEvent(savedOrder);
        orderEventProducer.sendOrderCreatedEvent(event);

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
        Page<OrderResponse> responsePage = orders.map(OrderResponse::fromWithoutItems);
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

        orderEventProducer.sendOrderCancelledEvent(event);
        log.info("주문 취소 완료: orderId={}", orderId);

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderResponse> responsePage = orders.map(OrderResponse::fromWithoutItems);
        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        Page<OrderResponse> responsePage = orders.map(OrderResponse::fromWithoutItems);
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

        order.updateStatus(newStatus);
        log.info("주문 상태 변경 완료: orderId={}, status={}", orderId, newStatus);

        return OrderResponse.from(order);
    }
}
