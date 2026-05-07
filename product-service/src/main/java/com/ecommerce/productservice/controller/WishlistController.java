package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.AddWishlistRequest;
import com.ecommerce.productservice.dto.response.WishlistItemResponse;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.common.security.CustomUserDetails;
import com.ecommerce.productservice.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<WishlistItemResponse>> getWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        log.info("GET /api/wishlist -  ?    ?   ?? userId={}", userDetails.getUserId());
        List<WishlistItemResponse> response = wishlistService.getWishlist(userDetails.getUserId(), acceptLanguage);
        return ApiResponse.success(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<WishlistItemResponse> addToWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddWishlistRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        log.info("POST /api/wishlist -  ??  ?: userId={}", userDetails.getUserId());
        WishlistItemResponse response = wishlistService.addToWishlist(userDetails.getUserId(), request,
                acceptLanguage);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> removeFromWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {
        log.info("DELETE /api/wishlist/{} -  ????? userId={}", productId, userDetails.getUserId());
        wishlistService.removeFromWishlist(userDetails.getUserId(), productId);
        return ApiResponse.ok();
    }

    @GetMapping("/check/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Boolean> isInWishlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {
        log.info("GET /api/wishlist/check/{} -  ???? ?   : userId={}", productId, userDetails.getUserId());
        boolean isInWishlist = wishlistService.isInWishlist(userDetails.getUserId(), productId);
        return ApiResponse.success(isInWishlist);
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Integer> getWishlistCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/wishlist/count -  ?   ??   ?? userId={}", userDetails.getUserId());
        int count = wishlistService.getWishlistCount(userDetails.getUserId());
        return ApiResponse.success(count);
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> clearWishlist(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("DELETE /api/wishlist -  ?    ??? ?? ? userId={}", userDetails.getUserId());
        wishlistService.clearWishlist(userDetails.getUserId());
        return ApiResponse.ok();
    }
}
