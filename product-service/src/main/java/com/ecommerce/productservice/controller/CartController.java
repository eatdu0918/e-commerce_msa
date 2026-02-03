package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.AddCartItemRequest;
import com.ecommerce.productservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.productservice.dto.response.CartItemResponse;
import com.ecommerce.productservice.dto.response.CartResponse;
import com.ecommerce.productservice.response.ApiResponse;
import com.ecommerce.productservice.security.CustomUserDetails;
import com.ecommerce.productservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/cart - 장바구니 조회: userId={}", userDetails.getUserId());
        CartResponse response = cartService.getCart(userDetails.getUserId());
        return ApiResponse.success(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartItemResponse> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddCartItemRequest request) {
        log.info("POST /api/cart - 장바구니 추가: userId={}", userDetails.getUserId());
        CartItemResponse response = cartService.addToCart(userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{cartItemId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartItemResponse> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("PUT /api/cart/{} - 장바구니 수량 수정: userId={}", cartItemId, userDetails.getUserId());
        CartItemResponse response = cartService.updateCartItem(userDetails.getUserId(), cartItemId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{cartItemId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> removeFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId) {
        log.info("DELETE /api/cart/{} - 장바구니 항목 삭제: userId={}", cartItemId, userDetails.getUserId());
        cartService.removeFromCart(userDetails.getUserId(), cartItemId);
        return ApiResponse.ok();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("DELETE /api/cart - 장바구니 비우기: userId={}", userDetails.getUserId());
        cartService.clearCart(userDetails.getUserId());
        return ApiResponse.ok();
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Integer> getCartItemCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("GET /api/cart/count - 장바구니 개수 조회: userId={}", userDetails.getUserId());
        int count = cartService.getCartItemCount(userDetails.getUserId());
        return ApiResponse.success(count);
    }
}
