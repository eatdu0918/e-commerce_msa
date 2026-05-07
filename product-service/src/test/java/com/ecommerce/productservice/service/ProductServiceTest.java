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
    private static final String PRODUCT_NAME = "??? ???  ?";

    @BeforeEach
    void setUp() {
        testCategory = createTestCategory(CATEGORY_ID, "??? ??   ?    ??);
        testProduct = createTestProduct(PRODUCT_ID, PRODUCT_NAME, testCategory);
    }

    @Nested
    @DisplayName("?  ? ?    ??? ??)
    class CreateProductTest {

        @Test
        @DisplayName("?  ? ?    ?   ")
        void createProduct_success() {
            // given
            CreateProductRequest request = new CreateProductRequest(
                    PRODUCT_NAME, null, "?  ? ??  ", null, "http://image.url", new BigDecimal("10000"), 100, null
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
        @DisplayName("?  ? ?    ?    -    ?    ???? ?)
        void createProduct_withCategory_success() {
            // given
            CreateProductRequest request = new CreateProductRequest(
                    PRODUCT_NAME, null, "?  ? ??  ", null, "http://image.url", new BigDecimal("10000"), 100, CATEGORY_ID
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
        @DisplayName("?  ? ?    ??   -    ???  ? ?)
        void createProduct_duplicateName_throwsException() {
            // given
            CreateProductRequest request = new CreateProductRequest(
                    PRODUCT_NAME, null, "?  ? ??  ", null, null, new BigDecimal("10000"), 100, null
            );

            when(productRepository.existsByName(PRODUCT_NAME)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?? ?    ???   ?  ?   ???  ");

            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        @DisplayName("?  ? ?    ??   -    ???? ??      ?    ??)
        void createProduct_categoryNotFound_throwsException() {
            // given
            CreateProductRequest request = new CreateProductRequest(
                    PRODUCT_NAME, null, "?  ? ??  ", null, null, new BigDecimal("10000"), 100, 999L
            );

            when(productRepository.existsByName(PRODUCT_NAME)).thenReturn(false);
            when(categoryRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("   ?    ?  ?   ??????  ??  ");
        }
    }

    @Nested
    @DisplayName("?  ?    ????? ??)
    class GetProductTest {

        @Test
        @DisplayName("?  ?    ???   ")
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
        @DisplayName("?  ?    ????   -    ???? ??   ?  ?")
        void getProduct_notFound_throwsException() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(999L))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?  ???   ??????  ??  ");
        }
    }

    @Nested
    @DisplayName("?  ?     ?   ????? ??)
    class GetProductsTest {

        @Test
        @DisplayName("?  ?     ?   ???   ")
        void getProducts_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Product product1 = createTestProduct(1L, "?  ?1", null);
            Product product2 = createTestProduct(2L, "?  ?2", null);
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
        @DisplayName("?  ?     ?   ??- ??  ??   ??)
        void getProducts_withKeyword_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "??? ??;
            Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);

            when(productRepository.findAllByIsActiveTrueAndNameContaining(keyword, pageable)).thenReturn(productPage);

            // when
            PageResponse<ProductResponse> response = productService.getProducts(pageable, keyword, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("?  ?     ?   ??-    ?    ???   ")
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
        @DisplayName("?  ?     ?   ??- ??  ??+    ?    ??)
        void getProducts_withKeywordAndCategory_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            String keyword = "??? ??;
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
    @DisplayName("?  ? ??   ??? ??)
    class UpdateProductTest {

        @Test
        @DisplayName("?  ? ??   ?   ")
        void updateProduct_success() {
            // given
            UpdateProductRequest request = new UpdateProductRequest(
                    "??  ???  ? ?, null, "??  ????  ", null, "http://new-image.url", new BigDecimal("20000"), 200, null
            );

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
            when(productRepository.existsByNameAndIdNot("??  ???  ? ?, PRODUCT_ID)).thenReturn(false);

            // when
            ProductResponse response = productService.updateProduct(PRODUCT_ID, request);

            // then
            assertThat(response).isNotNull();
            assertThat(testProduct.getName()).isEqualTo("??  ???  ? ?);
            assertThat(testProduct.getPrice()).isEqualByComparingTo(new BigDecimal("20000"));
        }

        @Test
        @DisplayName("?  ? ??   ??   -    ???? ??   ?  ?")
        void updateProduct_notFound_throwsException() {
            // given
            UpdateProductRequest request = new UpdateProductRequest(
                    "??  ???  ? ?, null, "??  ????  ", null, null, new BigDecimal("20000"), 200, null
            );

            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(999L, request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?  ???   ??????  ??  ");
        }

        @Test
        @DisplayName("?  ? ??   ??   -    ???  ? ?)
        void updateProduct_duplicateName_throwsException() {
            // given
            UpdateProductRequest request = new UpdateProductRequest(
                    "??   ?  ? ?, null, "??  ????  ", null, null, new BigDecimal("20000"), 200, null
            );

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));
            when(productRepository.existsByNameAndIdNot("??   ?  ? ?, PRODUCT_ID)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?? ?    ???   ?  ?   ???  ");
        }
    }

    @Nested
    @DisplayName("?  ? ??????? ??)
    class DeleteProductTest {

        @Test
        @DisplayName("?  ? ?????   ")
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
        @DisplayName("?  ? ??????   -    ???? ??   ?  ?")
        void deleteProduct_notFound_throwsException() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?  ???   ??????  ??  ");
        }
    }

    private Product createTestProduct(Long id, String name, Category category) {
        Product product = Product.create(name, "??  ", new BigDecimal("10000"), 100, category, "http://image.url");
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Category createTestCategory(Long id, String name) {
        Category category = Category.create(name, "   ?    ????  ", null, 0);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
