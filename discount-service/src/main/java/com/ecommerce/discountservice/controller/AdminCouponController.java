package com.ecommerce.discountservice.controller;

import com.ecommerce.discountservice.dto.request.BulkGrantCouponRequest;
import com.ecommerce.discountservice.dto.request.CreateCouponRequest;
import com.ecommerce.discountservice.dto.request.UpdateCouponRequest;
import com.ecommerce.discountservice.dto.response.BulkGrantCouponResponse;
import com.ecommerce.discountservice.dto.response.CouponResponse;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.discountservice.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Coupon", description = "?  ?    ?   ??  ??API")
@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @Operation(summary = "?   ???  ")
    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(response, "?   ????  ?? ???  ??"));
    }

    @Operation(summary = "?   ???      ???  ?   )")
    @PostMapping("/bulk-grant")
    public ResponseEntity<ApiResponse<BulkGrantCouponResponse>> bulkGrantCoupon(
            @Valid @RequestBody BulkGrantCouponRequest request) {
        BulkGrantCouponResponse response = couponService.bulkGrantCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(response, "?   ???      ???   ??? ???  ??"));
    }

    @Operation(summary = "?    ?   ?    ?   ??)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CouponResponse>>> getAllCoupons(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<CouponResponse> response = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "?   ??       ??)
    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCoupon(@PathVariable Long couponId) {
        CouponResponse response = couponService.getCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "?   ???  ")
    @PutMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable Long couponId,
            @Valid @RequestBody UpdateCouponRequest request) {
        CouponResponse response = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "?   ????  ?? ???  ??"));
    }

    @Operation(summary = "?   ??????? ??   )")
    @DeleteMapping("/{couponId}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.success(null, "?   ??????? ???  ??"));
    }
}
