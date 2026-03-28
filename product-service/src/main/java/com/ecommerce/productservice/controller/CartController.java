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
    public ApiResponse<CartResponse> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        log.info("GET /api/cart - 장바구니 조회: userId={}", userDetails.getUserId());
        CartResponse response = cartService.getCart(userDetails.getUserId(), acceptLanguage);
        return ApiResponse.success(response);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartItemResponse> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddCartItemRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        log.info("POST /api/cart - 장바구니 추가: userId={}", userDetails.getUserId());
        CartItemResponse response = cartService.addToCart(userDetails.getUserId(), request, acceptLanguage);
        return ApiResponse.success(response);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CartItemResponse> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        log.info("PUT /api/cart/{} - 장바구니 수량 수정: userId={}", productId, userDetails.getUserId());
        CartItemResponse response = cartService.updateCartItem(userDetails.getUserId(), productId, request,
                acceptLanguage);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> removeFromCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {
        log.info("DELETE /api/cart/{} - 장바구니 항목 삭제: userId={}", productId, userDetails.getUserId());
        cartService.removeFromCart(userDetails.getUserId(), productId);
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
