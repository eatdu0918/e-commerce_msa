package com.ecommerce.orderservice.dto.response;

import com.ecommerce.orderservice.client.dto.ProductInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemDetailResponse {

    Long id;
    Long productId;
    String productName;
    BigDecimal unitPrice;
    Integer quantity;
    BigDecimal totalPrice;
    String imageUrl;
    String categoryName;
    Integer currentStock;

    public static OrderItemDetailResponse from(OrderItemResponse item, ProductInfo productInfo) {
        return OrderItemDetailResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .imageUrl(item.getImageUrl() != null ? item.getImageUrl() : (productInfo != null ? productInfo.getImageUrl() : null))
                .categoryName(productInfo != null ? productInfo.getCategoryName() : null)
                .currentStock(productInfo != null ? productInfo.getStockQuantity() : null)
                .build();
    }
}
