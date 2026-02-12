package com.ecommerce.productservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Product Service API",
                version = "1.0.0",
                description = "상품, 카테고리, 장바구니, 찜 목록, 리뷰 관리 서비스"
        )
)
public class SwaggerConfig {
}
