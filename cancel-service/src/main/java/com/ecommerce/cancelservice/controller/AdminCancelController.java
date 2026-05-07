package com.ecommerce.cancelservice.controller;

import com.ecommerce.cancelservice.dto.request.RejectCancelRequest;
import com.ecommerce.cancelservice.dto.response.CancelResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSummaryResponse;
import com.ecommerce.cancelservice.dto.response.OrderCancelSyncResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import com.ecommerce.cancelservice.enums.CancelStatus;
import com.ecommerce.common.response.ApiResponse;
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

@Tag(name = "Admin Cancel", description = "?  ?    ?  ???  ??API")
@RestController
@RequestMapping("/api/admin/cancels")
@RequiredArgsConstructor
public class AdminCancelController {

    private final CancelService cancelService;

    @Operation(summary = "?  ?      ?    ?   ??(?    ?    ?    ?   )")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CancelResponse>>> getAllCancels(
            @RequestParam(required = false) CancelStatus status,
            @RequestParam(required = false) CancelRequestType requestType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<CancelResponse> response =
                cancelService.getAdminCancels(status, requestType, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "?  ???       ??)
    @GetMapping("/{cancelId}")
    public ResponseEntity<ApiResponse<CancelResponse>> getCancel(@PathVariable Long cancelId) {
        CancelResponse response = cancelService.getCancelById(cancelId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "     ?    ? ??  ???    (?  ?     rder-service    ??")
    @GetMapping("/orders/{orderId}/active")
    public ResponseEntity<ApiResponse<OrderCancelSummaryResponse>> getActiveCancelForOrder(
            @PathVariable Long orderId) {
        return cancelService.getActiveCancelForOrderAdmin(orderId)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @Operation(summary = "     ??  ????  ??(    ? ?+ ?    ?   ???? ?? order-service    ??")
    @GetMapping("/orders/{orderId}/sync")
    public ResponseEntity<ApiResponse<OrderCancelSyncResponse>> getCancelSyncForOrder(
            @PathVariable Long orderId) {
        OrderCancelSyncResponse sync = cancelService.getCancelSyncForOrderAdmin(orderId);
        return ResponseEntity.ok(ApiResponse.success(sync));
    }

    @Operation(summary = "?  ???  ??)
    @PutMapping("/{cancelId}/approve")
    public ResponseEntity<ApiResponse<CancelResponse>> approveCancel(@PathVariable Long cancelId) {
        CancelResponse response = cancelService.approveCancel(cancelId);
        return ResponseEntity.ok(ApiResponse.success(response, "?  ?  ? ?  ??? ???  ??"));
    }

    @Operation(summary = "?  ??   ?")
    @PutMapping("/{cancelId}/reject")
    public ResponseEntity<ApiResponse<CancelResponse>> rejectCancel(
            @PathVariable Long cancelId,
            @Valid @RequestBody RejectCancelRequest request) {
        CancelResponse response = cancelService.rejectCancel(cancelId, request.getRejectedReason());
        return ResponseEntity.ok(ApiResponse.success(response, "?  ?  ?    ??? ???  ??"));
    }
}
