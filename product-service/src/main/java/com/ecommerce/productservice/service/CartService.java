package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.AddCartItemRequest;
import com.ecommerce.productservice.dto.request.UpdateCartItemRequest;
import com.ecommerce.productservice.dto.response.CartItemResponse;
import com.ecommerce.productservice.dto.response.CartResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.util.CatalogLocaleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProductService productService;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofDays(30);

    public CartResponse getCart(Long userId, String acceptLanguage) {
        boolean preferKo = CatalogLocaleHelper.preferKorean(acceptLanguage);
        String key = CART_KEY_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        List<CartItemResponse> items = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long productId = Long.parseLong(entry.getKey().toString());
            int quantity = Integer.parseInt(entry.getValue().toString());

            try {
                Product product = productService.getActiveProduct(productId);
                items.add(buildCartItemResponse(product, quantity, preferKo));
            } catch (Exception e) {
                log.warn("장바구니 상품 조회 실패 (삭제된 상품 제거): productId={}", productId);
                redisTemplate.opsForHash().delete(key, entry.getKey().toString());
            }
        }

        return CartResponse.from(items);
    }

    public CartItemResponse addToCart(Long userId, AddCartItemRequest request, String acceptLanguage) {
        boolean preferKo = CatalogLocaleHelper.preferKorean(acceptLanguage);
        log.info("장바구니 추가 시도: userId={}, productId={}", userId, request.getProductId());

        Product product = productService.getActiveProduct(request.getProductId());

        String key = CART_KEY_PREFIX + userId;
        String productIdStr = request.getProductId().toString();
        Object existing = redisTemplate.opsForHash().get(key, productIdStr);

        int newQuantity;
        if (existing != null) {
            newQuantity = Integer.parseInt(existing.toString()) + request.getQuantity();
            log.info("장바구니 수량 증가: productId={}, newQuantity={}", request.getProductId(), newQuantity);
        } else {
            newQuantity = request.getQuantity();
            log.info("장바구니 추가 완료: productId={}", request.getProductId());
        }

        redisTemplate.opsForHash().put(key, productIdStr, String.valueOf(newQuantity));
        redisTemplate.expire(key, CART_TTL);
        return buildCartItemResponse(product, newQuantity, preferKo);
    }

    public CartItemResponse updateCartItem(Long userId, Long productId, UpdateCartItemRequest request,
            String acceptLanguage) {
        boolean preferKo = CatalogLocaleHelper.preferKorean(acceptLanguage);
        log.info("장바구니 수량 수정: userId={}, productId={}", userId, productId);

        String key = CART_KEY_PREFIX + userId;
        String productIdStr = productId.toString();

        if (!redisTemplate.opsForHash().hasKey(key, productIdStr)) {
            throw new ProductDomainException(ProductDomainExceptionCode.CartItemNotFoundException);
        }

        Product product = productService.getActiveProduct(productId);
        redisTemplate.opsForHash().put(key, productIdStr, String.valueOf(request.getQuantity()));
        redisTemplate.expire(key, CART_TTL);

        log.info("장바구니 수량 수정 완료: productId={}, newQuantity={}", productId, request.getQuantity());
        return buildCartItemResponse(product, request.getQuantity(), preferKo);
    }

    public void removeFromCart(Long userId, Long productId) {
        log.info("장바구니 항목 삭제: userId={}, productId={}", userId, productId);

        String key = CART_KEY_PREFIX + userId;
        String productIdStr = productId.toString();

        if (!redisTemplate.opsForHash().hasKey(key, productIdStr)) {
            throw new ProductDomainException(ProductDomainExceptionCode.CartItemNotFoundException);
        }

        redisTemplate.opsForHash().delete(key, productIdStr);
        log.info("장바구니 항목 삭제 완료: productId={}", productId);
    }

    public void clearCart(Long userId) {
        log.info("장바구니 비우기: userId={}", userId);
        redisTemplate.delete(CART_KEY_PREFIX + userId);
        log.info("장바구니 비우기 완료: userId={}", userId);
    }

    public int getCartItemCount(Long userId) {
        Long size = redisTemplate.opsForHash().size(CART_KEY_PREFIX + userId);
        return size != null ? size.intValue() : 0;
    }

    private CartItemResponse buildCartItemResponse(Product product, int quantity, boolean preferKorean) {
        BigDecimal price = product.getPrice();
        return CartItemResponse.builder()
                .cartItemId(product.getId())
                .productId(product.getId())
                .productName(CatalogLocaleHelper.productName(product, preferKorean))
                .productDescription(CatalogLocaleHelper.productDescription(product, preferKorean))
                .imageUrl(product.getImageUrl())
                .price(price)
                .quantity(quantity)
                .totalPrice(price.multiply(BigDecimal.valueOf(quantity)))
                .stockQuantity(product.getStockQuantity())
                .isAvailable(product.getStockQuantity() >= quantity)
                .build();
    }
}
