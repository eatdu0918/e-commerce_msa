package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.CreateProductRequest;
import com.ecommerce.productservice.dto.request.UpdateProductRequest;
import com.ecommerce.productservice.dto.response.PageResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.response.ApiResponse;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("POST /api/products - 상품 등록 요청");
        ProductResponse response = productService.createProduct(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long productId) {
        log.info("GET /api/products/{} - 상품 조회", productId);
        ProductResponse response = productService.getProduct(productId);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getProducts(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {
        log.info("GET /api/products - 상품 목록 조회, keyword={}, categoryId={}", keyword, categoryId);
        PageResponse<ProductResponse> response = productService.getProducts(pageable, keyword, categoryId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("PUT /api/products/{} - 상품 수정", productId);
        ProductResponse response = productService.updateProduct(productId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long productId) {
        log.info("DELETE /api/products/{} - 상품 삭제", productId);
        productService.deleteProduct(productId);
        return ApiResponse.ok();
    }
}
