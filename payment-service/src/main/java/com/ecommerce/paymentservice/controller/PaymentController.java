package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.dto.request.CreatePaymentRequest;
import com.ecommerce.paymentservice.dto.response.PageResponse;
import com.ecommerce.paymentservice.dto.response.PaymentResponse;
import com.ecommerce.paymentservice.response.ApiResponse;
import com.ecommerce.paymentservice.security.CustomUserDetails;
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

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "결제가 생성되었습니다."));
    }

    @Operation(summary = "내 결제 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PaymentResponse> response = paymentService.getMyPayments(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "결제 상세 조회")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문별 결제 조회")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
