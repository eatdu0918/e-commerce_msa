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

@Tag(name = "Cancel", description = "?  ??API")
@RestController
@RequestMapping("/api/cancels")
@RequiredArgsConstructor
public class CancelController {

    private final CancelService cancelService;

    @Operation(summary = "?  ???   ")
    @PostMapping
    public ResponseEntity<ApiResponse<CancelResponse>> createCancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCancelRequest request) {
        CancelResponse response = cancelService.createCancel(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "?  ???   ????  ?? ???  ??"));
    }

    @Operation(summary = "     ?    ? ??  ???    (     ?    ??  ??")
    @GetMapping("/by-order/{orderId}/active")
    public ResponseEntity<ApiResponse<OrderCancelSummaryResponse>> getActiveCancelForOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        return cancelService.getActiveCancelForOrder(orderId, userDetails.getUserId())
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @Operation(summary = "     ??  ????  ??(    ? ?+ ?    ?   ???? ??")
    @GetMapping("/by-order/{orderId}/sync")
    public ResponseEntity<ApiResponse<OrderCancelSyncResponse>> getCancelSyncForOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        OrderCancelSyncResponse sync =
                cancelService.getCancelSyncForOrder(orderId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(sync));
    }

    @Operation(summary = "???  ??    ?   ??)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CancelResponse>>> getMyCancels(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<CancelResponse> response = cancelService.getMyCancels(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "?  ???       ??)
    @GetMapping("/{cancelId}")
    public ResponseEntity<ApiResponse<CancelResponse>> getCancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cancelId) {
        CancelResponse response = cancelService.getCancel(cancelId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
