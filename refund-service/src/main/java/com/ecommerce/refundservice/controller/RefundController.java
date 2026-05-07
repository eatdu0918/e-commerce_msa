package com.ecommerce.refundservice.controller;

import com.ecommerce.common.response.PageResponse;
import com.ecommerce.refundservice.dto.response.RefundResponse;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.refundservice.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Refund", description = "??   API")
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @Operation(summary = "????       ?   ??)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RefundResponse>>> getMyRefunds(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<RefundResponse> response = refundService.getMyRefunds(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "??   ?       ??)
    @GetMapping("/{refundId}")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefund(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long refundId) {
        RefundResponse response = refundService.getRefund(refundId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "?  ?  ???      ??)
    @GetMapping("/cancel/{cancelId}")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefundByCancelId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cancelId) {
        RefundResponse response = refundService.getRefundByCancelId(cancelId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
