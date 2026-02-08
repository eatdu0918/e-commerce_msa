package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.CreateProductRequest;
import com.ecommerce.productservice.dto.request.UpdateProductRequest;
import com.ecommerce.productservice.dto.response.PageResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("상품 등록 시도: name={}", request.getName());

        if (productRepository.existsByName(request.getName())) {
            throw new ProductDomainException(ProductDomainExceptionCode.DuplicateProductNameException);
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndIsActiveTrue(request.getCategoryId())
                    .orElseThrow(
                            () -> new ProductDomainException(ProductDomainExceptionCode.CategoryNotFoundException));
        }

        Product product = Product.create(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                category,
                request.getImageUrl());

        Product savedProduct = productRepository.save(product);
        log.info("상품 등록 완료: productId={}, name={}", savedProduct.getId(), savedProduct.getName());

        return ProductResponse.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        Product product = getActiveProduct(productId);
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(Pageable pageable, String keyword, Long categoryId) {
        Page<Product> products;

        if (categoryId != null && keyword != null && !keyword.isBlank()) {
            products = productRepository.findAllByIsActiveTrueAndCategoryIdAndNameContaining(categoryId, keyword,
                    pageable);
        } else if (categoryId != null) {
            products = productRepository.findAllByIsActiveTrueAndCategoryId(categoryId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            products = productRepository.findAllByIsActiveTrueAndNameContaining(keyword, pageable);
        } else {
            products = productRepository.findAllByIsActiveTrue(pageable);
        }

        Page<ProductResponse> responsePage = products.map(ProductResponse::from);
        return PageResponse.from(responsePage);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        log.info("상품 수정 시도: productId={}", productId);

        Product product = getActiveProduct(productId);

        // 상품명 중복 체크 (자기 자신 제외)
        if (productRepository.existsByNameAndIdNot(request.getName(), productId)) {
            throw new ProductDomainException(ProductDomainExceptionCode.DuplicateProductNameException);
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndIsActiveTrue(request.getCategoryId())
                    .orElseThrow(
                            () -> new ProductDomainException(ProductDomainExceptionCode.CategoryNotFoundException));
        }

        product.update(
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getStockQuantity(),
                category,
                request.getImageUrl());

        log.info("상품 수정 완료: productId={}", productId);
        return ProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        log.info("상품 삭제 시도: productId={}", productId);

        Product product = getActiveProduct(productId);
        product.delete();

        log.info("상품 삭제 완료: productId={}", productId);
    }

    public Product getActiveProduct(Long productId) {
        return productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.ProductNotFoundException));
    }
}
