package com.ecommerce.orderservice.controller;

import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.orderservice.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.orderservice.dto.response.OrderDetailResponse;
import com.ecommerce.orderservice.dto.response.OrderResponse;
import com.ecommerce.orderservice.enums.OrderStatus;
import com.ecommerce.orderservice.service.OrderAggregationService;
import com.ecommerce.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Order", description = "관리자 주문 관리 API")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderAggregationService orderAggregationService;

    @Operation(summary = "전체 주문 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<OrderResponse> response;
        if (status != null) {
            response = orderAggregationService.getOrdersByStatusForAdmin(status, pageable);
        } else {
            response = orderAggregationService.getAllOrdersForAdmin(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문 상세 조회")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        OrderResponse response = orderAggregationService.getOrderByIdForAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문 상세 정보 조회 (상품/배송 정보 포함)")
    @GetMapping("/{orderId}/detail")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable Long orderId) {
        OrderDetailResponse response = orderAggregationService.getOrderDetailAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문 상태 변경")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "주문 상태가 성공적으로 변경되었습니다."));
    }
}
