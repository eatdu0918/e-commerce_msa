package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.StockRequest;
import com.ecommerce.productservice.dto.response.StockResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public StockResponse getStock(Long productId) {
        Product product = getActiveProduct(productId);
        return StockResponse.from(product, "현재 재고 조회");
    }

    @Transactional
    public StockResponse decreaseStock(StockRequest request) {
        log.info("재고 차감 시도: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        Product product = getActiveProduct(request.getProductId());

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new ProductDomainException(ProductDomainExceptionCode.InsufficientStockException);
        }

        product.decreaseStock(request.getQuantity());
        log.info("재고 차감 완료: productId={}, 남은 재고={}", product.getId(), product.getStockQuantity());

        return StockResponse.from(product, "재고 차감 완료");
    }

    @Transactional
    public StockResponse increaseStock(StockRequest request) {
        log.info("재고 증가 시도: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        Product product = getActiveProduct(request.getProductId());
        product.increaseStock(request.getQuantity());

        log.info("재고 증가 완료: productId={}, 현재 재고={}", product.getId(), product.getStockQuantity());

        return StockResponse.from(product, "재고 증가 완료");
    }

    @Transactional
    public StockResponse restoreStock(StockRequest request) {
        log.info("재고 복구 시도: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        Product product = getActiveProduct(request.getProductId());
        product.increaseStock(request.getQuantity());

        log.info("재고 복구 완료: productId={}, 현재 재고={}", product.getId(), product.getStockQuantity());

        return StockResponse.from(product, "재고 복구 완료 (주문 취소)");
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
