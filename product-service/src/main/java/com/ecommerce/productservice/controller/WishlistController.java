package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.AddWishlistRequest;
import com.ecommerce.productservice.dto.response.PageResponse;
import com.ecommerce.productservice.dto.response.WishlistItemResponse;
import com.ecommerce.productservice.response.ApiResponse;
import com.ecommerce.productservice.security.CustomUserDetails;
import com.ecommerce.productservice.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageResponse<WishlistItemResponse>> getWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/wishlist - 찜 목록 조회: userId={}", userDetails.getUserId());
        PageResponse<WishlistItemResponse> response = wishlistService.getWishlist(userDetails.getUserId(), pageable);
        return ApiResponse.success(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WishlistItemResponse> addToWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddWishlistRequest request) {
        log.info("POST /api/wishlist - 찜 추가: userId={}", userDetails.getUserId());
        WishlistItemResponse response = wishlistService.addToWishlist(userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{wishlistItemId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long wishlistItemId) {
        log.info("DELETE /api/wishlist/{} - 찜 삭제: userId={}", wishlistItemId, userDetails.getUserId());
        wishlistService.removeFromWishlist(userDetails.getUserId(), wishlistItemId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/product/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> removeFromWishlistByProductId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {
        log.info("DELETE /api/wishlist/product/{} - 찜 삭제 (상품ID): userId={}", productId, userDetails.getUserId());
        wishlistService.removeFromWishlistByProductId(userDetails.getUserId(), productId);
        return ApiResponse.ok();
    }

    @GetMapping("/check/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> isInWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {
        log.info("GET /api/wishlist/check/{} - 찜 여부 확인: userId={}", productId, userDetails.getUserId());
        boolean isInWishlist = wishlistService.isInWishlist(userDetails.getUserId(), productId);
        return ApiResponse.success(isInWishlist);
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Integer> getWishlistCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/wishlist/count - 찜 개수 조회: userId={}", userDetails.getUserId());
        int count = wishlistService.getWishlistCount(userDetails.getUserId());
        return ApiResponse.success(count);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> clearWishlist(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("DELETE /api/wishlist - 찜 목록 비우기: userId={}", userDetails.getUserId());
        wishlistService.clearWishlist(userDetails.getUserId());
        return ApiResponse.ok();
    }
}
