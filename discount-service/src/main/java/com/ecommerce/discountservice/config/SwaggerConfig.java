package com.ecommerce.discountservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Discount Service API",
                version = "1.0.0",
                description = "쿠폰 및 할인 관리 서비스"
        )
)
public class SwaggerConfig {
}
