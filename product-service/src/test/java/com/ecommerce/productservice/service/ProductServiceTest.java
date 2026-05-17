package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.CreateProductRequest;
import com.ecommerce.productservice.dto.request.UpdateProductRequest;
import com.ecommerce.common.response.PageResponse;
import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    ProductService productService;

    private Product testProduct;
    private Category testCategory;
    private static final Long PRODUCT_ID = 1L;
    private static final Long CATEGORY_ID = 1L;
    private static final String PRODUCT_NAME = "테스트 상품";

    @BeforeEach
    void setUp() {
        testCategory = createTestCategory(CATEGORY_ID, "테스트 카테고리");
        testProduct = createTestProduct(PRODUCT_ID, PRODUCT_NAME, testCategory);
    }

    @Nested
    @DisplayName("상품 등록 테스트")
    class CreateProductTest {

        @Test
        @DisplayName("상품 등록 성공")
        void createProduct_success() {
            // given
            CreateProductRequest request = new CreateProductRequest(
                    PRODUCT_NAME, null, "상품 설명", null, "http://image.url", new BigDecimal("10000"), 100, null
            );

            when(productRepository.existsByName(PRODUCT_NAME)).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                ReflectionTestUtils.setField(product, "id", PRODUCT_ID);
                return product;
            });

            // when
            ProductResponse response = productService.createProduct(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo(PRODUCT_NAME);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("상품 등록 성공 - 카테고리 포함")
        void createProduct_withCategory_success() {
            // given
            CreateProductRequest request = new CreateProductRequest(
                    PRODUCT_NAME, null, "상품 설명", null, "http://image.url", new BigDecimal("10000"), 100, CATEGORY_ID
            );

            when(productRepository.existsByName(PRODUCT_NAME)).thenReturn(false);
            when(categoryRepository.findByIdAndIsActiveTrue(CATEGORY_ID)).thenReturn(Optional.of(testCategory));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product product = invocation.getArgument(0);
                ReflectionTestUtils.setField(product, "id", PRODUCT_ID);
                return product;
            });

            // when
            ProductResponse response = productService.createProduct(request);

            // then
            assertThat(response).isNotNull();
            verify(categoryRepository).findByIdAndIsActiveTrue(CATEGORY_ID);
        }

        @Test
        @DisplayName("상품 등록 실패 - 중복 상품명")
        void createProduct_duplicateName_throwsException() {
            // given
            CreateProductRequest request = new CreateProductRequest(
                    PRODUCT_NAME, null, "상품 설명", null, null, new BigDecimal("10000"), 100, null
            );

            when(productRepository.existsByName(PRODUCT_NAME)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("이미 존재하는 상품명입니다");

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("상품 등록 실패 - 존재하지 않는 카테고리")
        void createProduct_categoryNotFound_throwsException() {
            // given
            CreateProductRequest request = new CreateProductRequest(
                    PRODUCT_NAME, null, "상품 설명", null, null, new BigDecimal("10000"), 100, 999L
            );

            when(productRepository.existsByName(PRODUCT_NAME)).thenReturn(false);
            when(categoryRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("카테고리를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("상품 조회 테스트")
    class GetProductTest {

        @Test
        @DisplayName("상품 조회 성공")
        void getProduct_success() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            ProductResponse response = productService.getProduct(PRODUCT_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(PRODUCT_ID);
            assertThat(response.getName()).isEqualTo(PRODUCT_NAME);
        }

        @Test
        @DisplayName("상품 조회 실패 - 존재하지 않는 상품")
        void getProduct_notFound_throwsException() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(999L))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("상품 목록 조회 테스트")
    class GetProductsTest {

        @Test
        @DisplayName("상품 목록 조회 성공")
        void getProducts_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Product product1 = createTestProduct(1L, "상품1", null);
            Product product2 = createTestProduct(2L, "상품2", null);
            Page<Product> productPage = new PageImpl<>(List.of(product1, product2), pageable, 2);

            when(productRepository.findAllByIsActiveTrue(pageable)).thenReturn(productPage);

            // when
            PageResponse<ProductResponse> response = productService.getProducts(pageable, null, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("상품 목록 조회 - 키워드 검색")
        void getProducts_withKeyword_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "테스트";
            Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);

            when(productRepository.findAllByIsActiveTrueAndNameContaining(keyword, pageable)).thenReturn(productPage);

            // when
            PageResponse<ProductResponse> response = productService.getProducts(pageable, keyword, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("상품 목록 조회 - 카테고리 필터")
        void getProducts_withCategory_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);

            when(productRepository.findAllByIsActiveTrueAndCategoryId(CATEGORY_ID, pageable)).thenReturn(productPage);

            // when
            PageResponse<ProductResponse> response = productService.getProducts(pageable, null, CATEGORY_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("상품 목록 조회 - 키워드 + 카테고리")
        void getProducts_withKeywordAndCategory_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "테스트";
            Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);

            when(productRepository.findAllByIsActiveTrueAndCategoryIdAndNameContaining(CATEGORY_ID, keyword, pageable))
                    .thenReturn(productPage);

            // when
            PageResponse<ProductResponse> response = productService.getProducts(pageable, keyword, CATEGORY_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("상품 수정 테스트")
    class UpdateProductTest {

        @Test
        @DisplayName("상품 수정 성공")
        void updateProduct_success() {
            // given
            UpdateProductRequest request = new UpdateProductRequest(
                    "수정된 상품명", null, "수정된 설명", null, "http://new-image.url", new BigDecimal("20000"), 200, null
            );

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
            when(productRepository.existsByNameAndIdNot("수정된 상품명", PRODUCT_ID)).thenReturn(false);

            // when
            ProductResponse response = productService.updateProduct(PRODUCT_ID, request);

            // then
            assertThat(response).isNotNull();
            assertThat(testProduct.getName()).isEqualTo("수정된 상품명");
            assertThat(testProduct.getPrice()).isEqualByComparingTo(new BigDecimal("20000"));
        }

        @Test
        @DisplayName("상품 수정 실패 - 존재하지 않는 상품")
        void updateProduct_notFound_throwsException() {
            // given
            UpdateProductRequest request = new UpdateProductRequest(
                    "수정된 상품명", null, "수정된 설명", null, null, new BigDecimal("20000"), 200, null
            );

            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(999L, request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("상품 수정 실패 - 중복 상품명")
        void updateProduct_duplicateName_throwsException() {
            // given
            UpdateProductRequest request = new UpdateProductRequest(
                    "다른 상품명", null, "수정된 설명", null, null, new BigDecimal("20000"), 200, null
            );

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
            when(productRepository.existsByNameAndIdNot("다른 상품명", PRODUCT_ID)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("이미 존재하는 상품명입니다");
        }
    }

    @Nested
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {

        @Test
        @DisplayName("상품 삭제 성공")
        void deleteProduct_success() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            productService.deleteProduct(PRODUCT_ID);

            // then
            assertThat(testProduct.getIsActive()).isFalse();
            assertThat(testProduct.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("상품 삭제 실패 - 존재하지 않는 상품")
        void deleteProduct_notFound_throwsException() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }

    private Product createTestProduct(Long id, String name, Category category) {
        Product product = Product.create(name, "설명", new BigDecimal("10000"), 100, category, "http://image.url");
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Category createTestCategory(Long id, String name) {
        Category category = Category.create(name, "카테고리 설명", null, 0);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
