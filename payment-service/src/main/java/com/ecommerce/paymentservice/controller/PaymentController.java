package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.paymentservice.service.PaymentService;
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

@Tag(name = "Payment", description = "   ??API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "   ????  ")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "   ?  ? ??  ?? ???  ??"));
    }

    @Operation(summary = "??   ??    ?   ??)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PaymentResponse> response = paymentService.getMyPayments(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "   ???       ??)
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "     ?   ??   ??)
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
