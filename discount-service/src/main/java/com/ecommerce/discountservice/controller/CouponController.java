package com.ecommerce.discountservice.controller;

import com.ecommerce.discountservice.dto.request.CalculateDiscountRequest;
import com.ecommerce.discountservice.dto.response.DiscountCalculationResponse;
import com.ecommerce.discountservice.dto.response.UserCouponResponse;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.discountservice.service.CouponService;
import com.ecommerce.discountservice.service.DiscountCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coupon", description = "?   ?API")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final DiscountCalculationService discountCalculationService;

    @Operation(summary = "???   ?    ?   ??)
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getMyCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<UserCouponResponse> coupons = couponService.getUserCoupons(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @Operation(summary = "????   ?  ??   ?    ?   ??)
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<UserCouponResponse>>> getAvailableCoupons(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<UserCouponResponse> coupons = couponService.getAvailableUserCoupons(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @Operation(summary = "?   ?   ?  ?  ")
    @PostMapping("/claim/{code}")
    public ResponseEntity<ApiResponse<UserCouponResponse>> claimCoupon(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String code) {
        UserCouponResponse response = couponService.claimCoupon(userDetails.getUserId(), code);
        return ResponseEntity.ok(ApiResponse.success(response, "?   ??   ??? ???  ??"));
    }

    @Operation(summary = "?        ??   ?)
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<DiscountCalculationResponse>> calculateDiscount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CalculateDiscountRequest request) {
        DiscountCalculationResponse response = discountCalculationService.calculateDiscount(
                userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
