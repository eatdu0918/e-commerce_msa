package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.AddWishlistRequest;
import com.ecommerce.productservice.dto.response.PageResponse;
import com.ecommerce.productservice.dto.response.WishlistItemResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.WishlistItem;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public PageResponse<WishlistItemResponse> getWishlist(Long userId, Pageable pageable) {
        Page<WishlistItem> wishlistItems = wishlistItemRepository.findAllByUserIdWithProduct(userId, pageable);
        Page<WishlistItemResponse> responsePage = wishlistItems.map(WishlistItemResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional
    public WishlistItemResponse addToWishlist(Long userId, AddWishlistRequest request) {
        log.info("찜 추가 시도: userId={}, productId={}", userId, request.getProductId());

        // 이미 찜한 상품인지 확인
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new ProductDomainException(ProductDomainExceptionCode.WishlistItemAlreadyExistsException);
        }

        Product product = productService.getActiveProduct(request.getProductId());

        WishlistItem wishlistItem = WishlistItem.create(userId, product);
        WishlistItem savedItem = wishlistItemRepository.save(wishlistItem);

        log.info("찜 추가 완료: wishlistItemId={}", savedItem.getId());
        return WishlistItemResponse.from(savedItem);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long wishlistItemId) {
        log.info("찜 삭제 시도: userId={}, wishlistItemId={}", userId, wishlistItemId);

        WishlistItem wishlistItem = wishlistItemRepository.findByIdAndUserId(wishlistItemId, userId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.WishlistItemNotFoundException));

        wishlistItemRepository.delete(wishlistItem);
        log.info("찜 삭제 완료: wishlistItemId={}", wishlistItemId);
    }

    @Transactional
    public void removeFromWishlistByProductId(Long userId, Long productId) {
        log.info("찜 삭제 시도 (상품ID): userId={}, productId={}", userId, productId);

        WishlistItem wishlistItem = wishlistItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.WishlistItemNotFoundException));

        wishlistItemRepository.delete(wishlistItem);
        log.info("찜 삭제 완료: productId={}", productId);
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Transactional(readOnly = true)
    public int getWishlistCount(Long userId) {
        return wishlistItemRepository.countByUserId(userId);
    }

    @Transactional
    public void clearWishlist(Long userId) {
        log.info("찜 목록 비우기: userId={}", userId);
        wishlistItemRepository.deleteAllByUserId(userId);
        log.info("찜 목록 비우기 완료: userId={}", userId);
    }
}
