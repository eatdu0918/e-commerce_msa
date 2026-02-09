package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.client.dto.ProductInfo;
import com.ecommerce.orderservice.config.FeignConfig;
import com.ecommerce.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service",
        url = "${feign.client.product-service.url}",
        configuration = FeignConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/api/products/{productId}")
    ApiResponse<ProductInfo> getProduct(@PathVariable("productId") Long productId);
}
