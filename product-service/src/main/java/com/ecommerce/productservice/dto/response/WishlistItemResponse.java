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
public class WishlistItemResponse {

    Long wishlistItemId;
    Long productId;
    String productName;
    String productDescription;
    BigDecimal price;
    Integer stockQuantity;
    String imageUrl;
    Boolean isAvailable;
    LocalDateTime createdAt;
}
