package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.CreateReviewRequest;
import com.ecommerce.productservice.dto.request.UpdateReviewRequest;
import com.ecommerce.productservice.dto.response.PageResponse;
import com.ecommerce.productservice.dto.response.ReviewResponse;
import com.ecommerce.productservice.response.ApiResponse;
import com.ecommerce.productservice.security.CustomUserDetails;
import com.ecommerce.productservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/products/{productId}")
    public ApiResponse<PageResponse<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/reviews/products/{} - 상품 리뷰 목록 조회", productId);
        return ApiResponse.success(reviewService.getProductReviews(productId, pageable));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/reviews/my - 내 리뷰 목록 조회, userId={}", userDetails.getUserId());
        return ApiResponse.success(reviewService.getMyReviews(userDetails.getUserId(), pageable));
    }

    @PostMapping
    public ApiResponse<ReviewResponse> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {
        log.info("POST /api/reviews - 리뷰 등록, userId={}, productId={}", userDetails.getUserId(), request.getProductId());
        return ApiResponse.success(reviewService.createReview(userDetails.getUserId(), userDetails.getEmail(), request));
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        log.info("PUT /api/reviews/{} - 리뷰 수정, userId={}", reviewId, userDetails.getUserId());
        return ApiResponse.success(reviewService.updateReview(userDetails.getUserId(), reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId) {
        log.info("DELETE /api/reviews/{} - 리뷰 삭제, userId={}", reviewId, userDetails.getUserId());
        reviewService.deleteReview(userDetails.getUserId(), reviewId);
        return ApiResponse.ok();
    }
}
