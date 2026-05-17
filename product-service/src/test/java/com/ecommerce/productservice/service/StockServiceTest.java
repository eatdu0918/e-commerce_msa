package com.ecommerce.productservice.service;

import com.ecommerce.productservice.dto.request.StockRequest;
import com.ecommerce.productservice.dto.response.StockResponse;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.exception.ProductDomainException;
import com.ecommerce.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    StockService stockService;

    private Product testProduct;
    private static final Long PRODUCT_ID = 1L;
    private static final int INITIAL_STOCK = 100;

    @BeforeEach
    void setUp() {
        testProduct = createTestProduct(PRODUCT_ID, "테스트 상품", INITIAL_STOCK);
    }

    @Nested
    @DisplayName("재고 조회 테스트")
    class GetStockTest {

        @Test
        @DisplayName("재고 조회 성공")
        void getStock_success() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            StockResponse response = stockService.getStock(PRODUCT_ID, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(response.getStockQuantity()).isEqualTo(INITIAL_STOCK);
        }

        @Test
        @DisplayName("재고 조회 실패 - 존재하지 않는 상품")
        void getStock_productNotFound_throwsException() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockService.getStock(999L, null))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("재고 차감 테스트")
    class DecreaseStockTest {

        @Test
        @DisplayName("재고 차감 성공")
        void decreaseStock_success() {
            // given
            int decreaseQuantity = 30;
            StockRequest request = new StockRequest(PRODUCT_ID, decreaseQuantity);

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            StockResponse response = stockService.decreaseStock(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStockQuantity()).isEqualTo(INITIAL_STOCK - decreaseQuantity);
            assertThat(testProduct.getStockQuantity()).isEqualTo(INITIAL_STOCK - decreaseQuantity);
        }

        @Test
        @DisplayName("재고 차감 실패 - 재고 부족")
        void decreaseStock_insufficientStock_throwsException() {
            // given
            int decreaseQuantity = 150; // ???  ? ????
            StockRequest request = new StockRequest(PRODUCT_ID, decreaseQuantity);

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when & then
            assertThatThrownBy(() -> stockService.decreaseStock(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("재고가 부족합니다");
        }

        @Test
        @DisplayName("재고 차감 실패 - 존재하지 않는 상품")
        void decreaseStock_productNotFound_throwsException() {
            // given
            StockRequest request = new StockRequest(999L, 10);

            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockService.decreaseStock(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("재고 전량 차감 성공")
        void decreaseStock_allStock_success() {
            // given
            StockRequest request = new StockRequest(PRODUCT_ID, INITIAL_STOCK);

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            StockResponse response = stockService.decreaseStock(request);

            // then
            assertThat(response.getStockQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("재고 증가 테스트")
    class IncreaseStockTest {

        @Test
        @DisplayName("재고 증가 성공")
        void increaseStock_success() {
            // given
            int increaseQuantity = 50;
            StockRequest request = new StockRequest(PRODUCT_ID, increaseQuantity);

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            StockResponse response = stockService.increaseStock(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStockQuantity()).isEqualTo(INITIAL_STOCK + increaseQuantity);
            assertThat(testProduct.getStockQuantity()).isEqualTo(INITIAL_STOCK + increaseQuantity);
        }

        @Test
        @DisplayName("재고 증가 실패 - 존재하지 않는 상품")
        void increaseStock_productNotFound_throwsException() {
            // given
            StockRequest request = new StockRequest(999L, 50);

            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockService.increaseStock(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("재고 복구 테스트")
    class RestoreStockTest {

        @Test
        @DisplayName("재고 복구 성공")
        void restoreStock_success() {
            // given
            int restoreQuantity = 20;
            StockRequest request = new StockRequest(PRODUCT_ID, restoreQuantity);

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            StockResponse response = stockService.restoreStock(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStockQuantity()).isEqualTo(INITIAL_STOCK + restoreQuantity);
            assertThat(response.getMessage()).contains("복구");
        }
    }

    @Nested
    @DisplayName("재고 확인 테스트")
    class CheckStockTest {

        @Test
        @DisplayName("재고 확인 - 충분함")
        void checkStock_sufficient_returnsTrue() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            boolean result = stockService.checkStock(PRODUCT_ID, 50);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("재고 확인 - 부족함")
        void checkStock_insufficient_returnsFalse() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            boolean result = stockService.checkStock(PRODUCT_ID, 150);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("재고 확인 - 정확히 같음")
        void checkStock_exact_returnsTrue() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            boolean result = stockService.checkStock(PRODUCT_ID, INITIAL_STOCK);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("재고 확인 실패 - 존재하지 않는 상품")
        void checkStock_productNotFound_throwsException() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockService.checkStock(999L, 10))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }

    private Product createTestProduct(Long id, String name, int stockQuantity) {
        Product product = Product.create(name, "설명", new BigDecimal("10000"), stockQuantity, null, "http://image.url");
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
