package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.AddWishlistRequest;
import com.ecommerce.productservice.dto.response.WishlistItemResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.util.CatalogLocaleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProductService productService;

    private static final String WISHLIST_KEY_PREFIX = "wishlist:";
    private static final Duration WISHLIST_TTL = Duration.ofDays(30);

    public List<WishlistItemResponse> getWishlist(Long userId, String acceptLanguage) {
        boolean preferKo = CatalogLocaleHelper.preferKorean(acceptLanguage);
        String key = WISHLIST_KEY_PREFIX + userId;
        Set<String> members = redisTemplate.opsForSet().members(key);

        List<WishlistItemResponse> items = new ArrayList<>();
        if (members != null) {
            for (String productIdStr : members) {
                Long productId = Long.parseLong(productIdStr);
                try {
                    Product product = productService.getActiveProduct(productId);
                    items.add(buildWishlistItemResponse(product, preferKo));
                } catch (Exception e) {
                    log.warn("찜 상품 조회 실패 (삭제된 상품 제거): productId={}", productId);
                    redisTemplate.opsForSet().remove(key, productIdStr);
                }
            }
        }

        return items;
    }

    public WishlistItemResponse addToWishlist(Long userId, AddWishlistRequest request, String acceptLanguage) {
        boolean preferKo = CatalogLocaleHelper.preferKorean(acceptLanguage);
        log.info("찜 추가 시도: userId={}, productId={}", userId, request.getProductId());

        String key = WISHLIST_KEY_PREFIX + userId;
        String productIdStr = request.getProductId().toString();

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, productIdStr))) {
            throw new ProductDomainException(ProductDomainExceptionCode.WishlistItemAlreadyExistsException);
        }

        Product product = productService.getActiveProduct(request.getProductId());
        redisTemplate.opsForSet().add(key, productIdStr);
        redisTemplate.expire(key, WISHLIST_TTL);

        log.info("찜 추가 완료: productId={}", request.getProductId());
        return buildWishlistItemResponse(product, preferKo);
    }

    public void removeFromWishlist(Long userId, Long productId) {
        log.info("찜 삭제 시도: userId={}, productId={}", userId, productId);

        String key = WISHLIST_KEY_PREFIX + userId;
        String productIdStr = productId.toString();

        if (!Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, productIdStr))) {
            throw new ProductDomainException(ProductDomainExceptionCode.WishlistItemNotFoundException);
        }

        redisTemplate.opsForSet().remove(key, productIdStr);
        log.info("찜 삭제 완료: productId={}", productId);
    }

    public boolean isInWishlist(Long userId, Long productId) {
        String key = WISHLIST_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, productId.toString()));
    }

    public int getWishlistCount(Long userId) {
        Long size = redisTemplate.opsForSet().size(WISHLIST_KEY_PREFIX + userId);
        return size != null ? size.intValue() : 0;
    }

    public void clearWishlist(Long userId) {
        log.info("찜 목록 비우기: userId={}", userId);
        redisTemplate.delete(WISHLIST_KEY_PREFIX + userId);
        log.info("찜 목록 비우기 완료: userId={}", userId);
    }

    private WishlistItemResponse buildWishlistItemResponse(Product product, boolean preferKorean) {
        return WishlistItemResponse.builder()
                .wishlistItemId(product.getId())
                .productId(product.getId())
                .productName(CatalogLocaleHelper.productName(product, preferKorean))
                .productDescription(CatalogLocaleHelper.productDescription(product, preferKorean))
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .isAvailable(product.getStockQuantity() > 0)
                .build();
    }
}
