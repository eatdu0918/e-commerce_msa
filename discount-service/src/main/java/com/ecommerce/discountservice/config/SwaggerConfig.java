package com.ecommerce.discountservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Discount Service API",
                version = "1.0.0",
                description = "     ??      ??  ??API"
        )
)
public class SwaggerConfig {
}
