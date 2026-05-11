package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.StockRequest;
import com.ecommerce.productservice.dto.response.StockResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.util.CatalogLocaleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public StockResponse getStock(Long productId, String acceptLanguage) {
        Product product = getActiveProduct(productId);
        return StockResponse.from(product, "재고 정보 조회 성공", CatalogLocaleHelper.preferKorean(acceptLanguage));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#request.productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public StockResponse decreaseStock(StockRequest request) {
        log.info("재고 감소 요청 시작: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        Product product = getActiveProduct(request.getProductId());

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new ProductDomainException(ProductDomainExceptionCode.InsufficientStockException);
        }

        product.decreaseStock(request.getQuantity());
        return StockResponse.from(product, "재고 감소 성공");
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#request.productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public StockResponse increaseStock(StockRequest request) {
        log.info("재고 증가 요청 시작: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        Product product = getActiveProduct(request.getProductId());
        product.increaseStock(request.getQuantity());

        log.info("재고 증가 완료: productId={}, 남은 재고={}", product.getId(), product.getStockQuantity());

        return StockResponse.from(product, "재고 증가 성공");
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#request.productId"),
            @CacheEvict(value = "products", allEntries = true)
    })
    public StockResponse restoreStock(StockRequest request) {
        log.info("재고 복구 요청 시작: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        Product product = getActiveProduct(request.getProductId());
        product.increaseStock(request.getQuantity());

        log.info("재고 복구 완료: productId={}, 남은 재고={}", product.getId(), product.getStockQuantity());

        return StockResponse.from(product, "재고 복구 성공 (주문 취소 등)");
    }

    @Transactional(readOnly = true)
    public boolean checkStock(Long productId, Integer quantity) {
        Product product = getActiveProduct(productId);
        return product.getStockQuantity() >= quantity;
    }

    private Product getActiveProduct(Long productId) {
        return productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.ProductNotFoundException));
    }
}
