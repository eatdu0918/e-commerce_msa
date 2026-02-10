package com.ecommerce.productservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {

    Long cartItemId;
    Long productId;
    String productName;
    String productDescription;
    BigDecimal price;
    Integer quantity;
    BigDecimal totalPrice;
    Integer stockQuantity;
    String imageUrl;
    Boolean isAvailable;
    LocalDateTime createdAt;
}
