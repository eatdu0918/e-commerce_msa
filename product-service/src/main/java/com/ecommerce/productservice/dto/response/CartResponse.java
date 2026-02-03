package com.ecommerce.productservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {

    List<CartItemResponse> items;
    Integer totalItemCount;
    BigDecimal totalPrice;

    public static CartResponse from(List<CartItemResponse> items) {
        BigDecimal totalPrice = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(items)
                .totalItemCount(items.size())
                .totalPrice(totalPrice)
                .build();
    }
}
