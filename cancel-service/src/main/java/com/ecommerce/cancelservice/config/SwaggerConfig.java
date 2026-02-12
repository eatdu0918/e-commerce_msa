package com.ecommerce.cancelservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cancel Service API",
                version = "1.0.0",
                description = "주문 취소 관리 서비스"
        )
)
public class SwaggerConfig {
}
