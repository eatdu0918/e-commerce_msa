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
        testProduct = createTestProduct(PRODUCT_ID, "??? ???  ?", INITIAL_STOCK);
    }

    @Nested
    @DisplayName("????   ????? ??)
    class GetStockTest {

        @Test
        @DisplayName("????   ???   ")
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
        @DisplayName("????   ????   -    ???? ??   ?  ?")
        void getStock_productNotFound_throwsException() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockService.getStock(999L, null))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?  ???   ??????  ??  ");
        }
    }

    @Nested
    @DisplayName("????    ???? ??)
    class DecreaseStockTest {

        @Test
        @DisplayName("????    ??   ")
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
        @DisplayName("????    ???   - ?????   ?)
        void decreaseStock_insufficientStock_throwsException() {
            // given
            int decreaseQuantity = 150; // ???  ? ????
            StockRequest request = new StockRequest(PRODUCT_ID, decreaseQuantity);

            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when & then
            assertThatThrownBy(() -> stockService.decreaseStock(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("???  ? ?       ??  ");
        }

        @Test
        @DisplayName("????    ???   -    ???? ??   ?  ?")
        void decreaseStock_productNotFound_throwsException() {
            // given
            StockRequest request = new StockRequest(999L, 10);

            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockService.decreaseStock(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?  ???   ??????  ??  ");
        }

        @Test
        @DisplayName("?????        ??   ")
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
    @DisplayName("????   ? ??? ??)
    class IncreaseStockTest {

        @Test
        @DisplayName("????   ? ?   ")
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
        @DisplayName("????   ? ??   -    ???? ??   ?  ?")
        void increaseStock_productNotFound_throwsException() {
            // given
            StockRequest request = new StockRequest(999L, 50);

            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockService.increaseStock(request))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?  ???   ??????  ??  ");
        }
    }

    @Nested
    @DisplayName("????   ????? ??)
    class RestoreStockTest {

        @Test
        @DisplayName("????   ???   ")
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
            assertThat(response.getMessage()).contains("   ??);
        }
    }

    @Nested
    @DisplayName("?????    ??? ??)
    class CheckStockTest {

        @Test
        @DisplayName("?????    - ?  ???)
        void checkStock_sufficient_returnsTrue() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            boolean result = stockService.checkStock(PRODUCT_ID, 50);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?????    - ?      ?)
        void checkStock_insufficient_returnsFalse() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            boolean result = stockService.checkStock(PRODUCT_ID, 150);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("?????    - ?   ??   ??)
        void checkStock_exact_returnsTrue() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(PRODUCT_ID)).thenReturn(Optional.of(testProduct));

            // when
            boolean result = stockService.checkStock(PRODUCT_ID, INITIAL_STOCK);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("?????    ??   -    ???? ??   ?  ?")
        void checkStock_productNotFound_throwsException() {
            // given
            when(productRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockService.checkStock(999L, 10))
                    .isInstanceOf(ProductDomainException.class)
                    .hasMessageContaining("?  ???   ??????  ??  ");
        }
    }

    private Product createTestProduct(Long id, String name, int stockQuantity) {
        Product product = Product.create(name, "??  ", new BigDecimal("10000"), stockQuantity, null, "http://image.url");
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
