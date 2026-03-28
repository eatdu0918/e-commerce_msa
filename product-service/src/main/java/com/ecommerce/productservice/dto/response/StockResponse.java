package com.ecommerce.productservice.dto.response;

import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.util.CatalogLocaleHelper;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockResponse {

    Long productId;
    String productName;
    Integer stockQuantity;
    String message;

    public static StockResponse from(Product product, String message) {
        return from(product, message, false);
    }

    public static StockResponse from(Product product, String message, boolean preferKorean) {
        return StockResponse.builder()
                .productId(product.getId())
                .productName(CatalogLocaleHelper.productName(product, preferKorean))
                .stockQuantity(product.getStockQuantity())
                .message(message)
                .build();
    }
}
