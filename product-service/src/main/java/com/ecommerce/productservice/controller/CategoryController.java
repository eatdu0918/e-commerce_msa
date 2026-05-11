package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.CreateCategoryRequest;
import com.ecommerce.productservice.dto.request.UpdateCategoryRequest;
import com.ecommerce.productservice.dto.response.CategoryResponse;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.productservice.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        log.info("POST /api/categories - 카테고리 생성 요청");
        CategoryResponse response = categoryService.createCategory(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Long categoryId) {
        log.info("GET /api/categories/{} - 카테고리 상세 정보 조회", categoryId);
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        log.info("GET /api/categories - 전체 카테고리 계층 구조 조회");
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ApiResponse.success(response);
    }

    @GetMapping("/root")
    public ApiResponse<List<CategoryResponse>> getRootCategories() {
        log.info("GET /api/categories/root - 최상위 카테고리 목록 조회");
        List<CategoryResponse> response = categoryService.getRootCategories();
        return ApiResponse.success(response);
    }

    @GetMapping("/{parentId}/children")
    public ApiResponse<List<CategoryResponse>> getChildCategories(@PathVariable Long parentId) {
        log.info("GET /api/categories/{}/children - 하위 카테고리 목록 조회", parentId);
        List<CategoryResponse> response = categoryService.getChildCategories(parentId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("PUT /api/categories/{} - 카테고리 정보 수정", categoryId);
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        log.info("DELETE /api/categories/{} - 카테고리 삭제 요청", categoryId);
        categoryService.deleteCategory(categoryId);
        return ApiResponse.ok();
    }
}
