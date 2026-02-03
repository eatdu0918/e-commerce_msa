package com.ecommerce.productservice.dto.response;

import com.ecommerce.productservice.entity.CartItem;
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
    Boolean isAvailable;
    LocalDateTime createdAt;

    public static CartItemResponse from(CartItem cartItem) {
        BigDecimal price = cartItem.getProduct().getPrice();
        Integer quantity = cartItem.getQuantity();
        Integer stockQuantity = cartItem.getProduct().getStockQuantity();

        return CartItemResponse.builder()
                .cartItemId(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .productDescription(cartItem.getProduct().getDescription())
                .price(price)
                .quantity(quantity)
                .totalPrice(price.multiply(BigDecimal.valueOf(quantity)))
                .stockQuantity(stockQuantity)
                .isAvailable(stockQuantity >= quantity)
                .createdAt(cartItem.getCreatedAt())
                .build();
    }
}
