package com.ecommerce.productservice.dto.response;

import com.ecommerce.productservice.entity.WishlistItem;
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

    public static WishlistItemResponse from(WishlistItem wishlistItem) {
        return WishlistItemResponse.builder()
                .wishlistItemId(wishlistItem.getId())
                .productId(wishlistItem.getProduct().getId())
                .productName(wishlistItem.getProduct().getName())
                .productDescription(wishlistItem.getProduct().getDescription())
                .imageUrl(wishlistItem.getProduct().getImageUrl())
                .price(wishlistItem.getProduct().getPrice())
                .stockQuantity(wishlistItem.getProduct().getStockQuantity())
                .isAvailable(wishlistItem.getProduct().getStockQuantity() > 0)
                .createdAt(wishlistItem.getCreatedAt())
                .build();
    }
}
