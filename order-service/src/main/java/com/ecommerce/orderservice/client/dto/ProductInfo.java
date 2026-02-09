package com.ecommerce.orderservice.client.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductInfo {

    Long id;
    String name;
    String description;
    String imageUrl;
    BigDecimal price;
    Integer stockQuantity;
    Long categoryId;
    String categoryName;
}
