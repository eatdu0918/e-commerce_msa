package com.ecommerce.cancelservice.controller;

import com.ecommerce.cancelservice.dto.request.CreateCancelRequest;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSummaryResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSyncResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.cancelservice.service.CancelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cancel", description = "주문 취소 API")
@RestController
@RequestMapping("/api/cancels")
@RequiredArgsConstructor
public class CancelController {

    private final CancelService cancelService;

    @Operation(summary = "취소 요청 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CancelResponse>> createCancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCancelRequest request) {
        CancelResponse response = cancelService.createCancel(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "취소 요청이 성공적으로 접수되었습니다."));
    }

    @Operation(summary = "특정 주문의 활성 취소 요청 조회")
    @GetMapping("/by-order/{orderId}/active")
    public ResponseEntity<ApiResponse<OrderCancelSummaryResponse>> getActiveCancelForOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        return cancelService.getActiveCancelForOrder(orderId, userDetails.getUserId())
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @Operation(summary = "특정 주문의 취소 데이터 동기화 조회")
    @GetMapping("/by-order/{orderId}/sync")
    public ResponseEntity<ApiResponse<OrderCancelSyncResponse>> getCancelSyncForOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        OrderCancelSyncResponse sync =
                cancelService.getCancelSyncForOrder(orderId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(sync));
    }

    @Operation(summary = "내 취소 요청 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CancelResponse>>> getMyCancels(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<CancelResponse> response = cancelService.getMyCancels(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "취소 요청 상세 조회")
    @GetMapping("/{cancelId}")
    public ResponseEntity<ApiResponse<CancelResponse>> getCancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cancelId) {
        CancelResponse response = cancelService.getCancel(cancelId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
