package com.ecommerce.refundservice.controller;

import com.ecommerce.refundservice.dto.request.UpdateRefundStatusRequest;
import com.ecommerce.refundservice.dto.response.PageResponse;
import com.ecommerce.refundservice.dto.response.RefundResponse;
import com.ecommerce.refundservice.enums.RefundStatus;
import com.ecommerce.refundservice.response.ApiResponse;
import com.ecommerce.refundservice.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Refund", description = "관리자 환불 관리 API")
@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    @Operation(summary = "전체 환불 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RefundResponse>>> getAllRefunds(
            @RequestParam(required = false) RefundStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<RefundResponse> response;
        if (status != null) {
            response = refundService.getRefundsByStatus(status, pageable);
        } else {
            response = refundService.getAllRefunds(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "환불 상세 조회")
    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefund(@PathVariable Long refundId) {
        RefundResponse response = refundService.getRefundById(refundId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "환불 재시도")
    @PutMapping("/{refundId}/retry")
    public ResponseEntity<ApiResponse<RefundResponse>> retryRefund(@PathVariable Long refundId) {
        RefundResponse response = refundService.retryRefund(refundId);
        return ResponseEntity.ok(ApiResponse.success(response, "환불 재시도가 요청되었습니다."));
    }

    @Operation(summary = "환불 상태 변경")
    @PutMapping("/{refundId}/status")
    public ResponseEntity<ApiResponse<RefundResponse>> updateRefundStatus(
            @PathVariable Long refundId,
            @Valid @RequestBody UpdateRefundStatusRequest request) {
        RefundResponse response = refundService.updateRefundStatus(refundId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "환불 상태가 변경되었습니다."));
    }
}
