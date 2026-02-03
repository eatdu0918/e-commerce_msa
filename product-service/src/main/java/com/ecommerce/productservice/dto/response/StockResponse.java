package com.ecommerce.productservice.dto.response;

import com.ecommerce.productservice.entity.Product;
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
        return StockResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .stockQuantity(product.getStockQuantity())
                .message(message)
                .build();
    }
}
