package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.CreateReviewRequest;
import com.ecommerce.productservice.dto.request.UpdateReviewRequest;
import com.ecommerce.productservice.dto.response.PageResponse;
import com.ecommerce.productservice.dto.response.ReviewResponse;
import com.ecommerce.productservice.entity.Review;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.ProductNotFoundException));

        Page<ReviewResponse> page = reviewRepository.findAllByProductIdAndIsActiveTrue(productId, pageable)
                .map(ReviewResponse::from);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getMyReviews(Long userId, Pageable pageable) {
        Page<ReviewResponse> page = reviewRepository.findAllByUserIdAndIsActiveTrue(userId, pageable)
                .map(ReviewResponse::from);
        return PageResponse.from(page);
    }

    @Transactional
    public ReviewResponse createReview(Long userId, String userEmail, CreateReviewRequest request) {
        productRepository.findByIdAndIsActiveTrue(request.getProductId())
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.ProductNotFoundException));

        if (reviewRepository.existsByProductIdAndUserIdAndIsActiveTrue(request.getProductId(), userId)) {
            throw new ProductDomainException(ProductDomainExceptionCode.DuplicateReviewException);
        }

        Review review = Review.create(
                request.getProductId(),
                userId,
                userEmail,
                request.getScore(),
                request.getContent(),
                request.getImageUrl()
        );

        reviewRepository.save(review);
        log.info("리뷰 등록 완료 - reviewId={}, productId={}, userId={}", review.getId(), request.getProductId(), userId);
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        Review review = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.ReviewNotFoundException));

        if (!review.getUserId().equals(userId)) {
            throw new ProductDomainException(ProductDomainExceptionCode.AccessDeniedException);
        }

        review.update(request.getScore(), request.getContent(), request.getImageUrl());
        log.info("리뷰 수정 완료 - reviewId={}, userId={}", reviewId, userId);
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.ReviewNotFoundException));

        if (!review.getUserId().equals(userId)) {
            throw new ProductDomainException(ProductDomainExceptionCode.AccessDeniedException);
        }

        review.delete();
        log.info("리뷰 삭제 완료 - reviewId={}, userId={}", reviewId, userId);
    }
}
