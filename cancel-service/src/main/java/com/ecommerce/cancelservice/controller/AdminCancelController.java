package com.ecommerce.cancelservice.controller;

import com.ecommerce.cancelservice.dto.request.RejectCancelRequest;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSummaryResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSyncResponse;
import com.ecommerce.cancelservice.dto.response.PageResponse;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import com.ecommerce.cancelservice.enums.CancelStatus;
import com.ecommerce.cancelservice.response.ApiResponse;
import com.ecommerce.cancelservice.service.CancelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Cancel", description = "관리자 취소 관리 API")
@RestController
@RequestMapping("/api/admin/cancels")
@RequiredArgsConstructor
public class AdminCancelController {

    private final CancelService cancelService;

    @Operation(summary = "취소·반품 목록 조회 (상태·요청 유형 필터)")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CancelResponse>>> getAllCancels(
            @RequestParam(required = false) CancelStatus status,
            @RequestParam(required = false) CancelRequestType requestType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<CancelResponse> response =
                cancelService.getAdminCancels(status, requestType, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "취소 상세 조회")
    @GetMapping("/{cancelId}")
    public ResponseEntity<ApiResponse<CancelResponse>> getCancel(@PathVariable Long cancelId) {
        CancelResponse response = cancelService.getCancelById(cancelId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문별 진행 중 취소 요약 (관리자·order-service 집계)")
    @GetMapping("/orders/{orderId}/active")
    public ResponseEntity<ApiResponse<OrderCancelSummaryResponse>> getActiveCancelForOrder(
            @PathVariable Long orderId) {
        return cancelService.getActiveCancelForOrderAdmin(orderId)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @Operation(summary = "주문별 취소 동기화 (진행 건 + 유형별 거절 이력, order-service 집계)")
    @GetMapping("/orders/{orderId}/sync")
    public ResponseEntity<ApiResponse<OrderCancelSyncResponse>> getCancelSyncForOrder(
            @PathVariable Long orderId) {
        OrderCancelSyncResponse sync = cancelService.getCancelSyncForOrderAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success(sync));
    }

    @Operation(summary = "취소 승인")
    @PutMapping("/{cancelId}/approve")
    public ResponseEntity<ApiResponse<CancelResponse>> approveCancel(@PathVariable Long cancelId) {
        CancelResponse response = cancelService.approveCancel(cancelId);
        return ResponseEntity.ok(ApiResponse.success(response, "취소가 승인되었습니다."));
    }

    @Operation(summary = "취소 거부")
    @PutMapping("/{cancelId}/reject")
    public ResponseEntity<ApiResponse<CancelResponse>> rejectCancel(
            @PathVariable Long cancelId,
            @Valid @RequestBody RejectCancelRequest request) {
        CancelResponse response = cancelService.rejectCancel(cancelId, request.getRejectedReason());
        return ResponseEntity.ok(ApiResponse.success(response, "취소가 거부되었습니다."));
    }
}
