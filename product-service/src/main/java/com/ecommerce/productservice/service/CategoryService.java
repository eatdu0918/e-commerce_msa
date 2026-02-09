package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.CreateCategoryRequest;
import com.ecommerce.productservice.dto.request.UpdateCategoryRequest;
import com.ecommerce.productservice.dto.response.CategoryResponse;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.exception.ProductDomainExceptionCode;
import com.ecommerce.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("카테고리 등록 시도: name={}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new ProductDomainException(ProductDomainExceptionCode.DuplicateCategoryNameException);
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = getActiveCategory(request.getParentId());
        }

        Category category = Category.create(
                request.getName(),
                request.getDescription(),
                parent,
                request.getDisplayOrder()
        );

        Category savedCategory = categoryRepository.save(category);
        log.info("카테고리 등록 완료: categoryId={}", savedCategory.getId());

        return CategoryResponse.from(savedCategory);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long categoryId) {
        Category category = getActiveCategory(categoryId);
        return CategoryResponse.fromWithChildren(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'root'")
    public List<CategoryResponse> getRootCategories() {
        return categoryRepository.findAllRootCategories()
                .stream()
                .map(CategoryResponse::fromWithChildren)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getChildCategories(Long parentId) {
        return categoryRepository.findAllByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(parentId)
                .stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        log.info("카테고리 수정 시도: categoryId={}", categoryId);

        Category category = getActiveCategory(categoryId);

        if (categoryRepository.existsByNameAndIdNot(request.getName(), categoryId)) {
            throw new ProductDomainException(ProductDomainExceptionCode.DuplicateCategoryNameException);
        }

        Category parent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(categoryId)) {
                throw new ProductDomainException(ProductDomainExceptionCode.InvalidCategoryParentException);
            }
            parent = getActiveCategory(request.getParentId());
        }

        category.update(
                request.getName(),
                request.getDescription(),
                parent,
                request.getDisplayOrder()
        );

        log.info("카테고리 수정 완료: categoryId={}", categoryId);
        return CategoryResponse.from(category);
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long categoryId) {
        log.info("카테고리 삭제 시도: categoryId={}", categoryId);

        Category category = getActiveCategory(categoryId);
        category.deactivate();

        // 하위 카테고리도 비활성화
        deactivateChildren(category);

        log.info("카테고리 삭제 완료: categoryId={}", categoryId);
    }

    private void deactivateChildren(Category category) {
        for (Category child : category.getChildren()) {
            child.deactivate();
            deactivateChildren(child);
        }
    }

    private Category getActiveCategory(Long categoryId) {
        return categoryRepository.findByIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new ProductDomainException(ProductDomainExceptionCode.CategoryNotFoundException));
    }
}
