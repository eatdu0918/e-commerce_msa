package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.UpdatePaymentStatusRequest;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Payment", description = "?  ?       ???  ??API")
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "?       ??    ?   ??)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PaymentResponse> response;
        if (status != null) {
            response = paymentService.getPaymentsByStatus(status, pageable);
        } else {
            response = paymentService.getAllPayments(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "   ???       ??)
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "   ???        ?)
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "   ???              ??  ??  ."));
    }
}
