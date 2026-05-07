package com.ecommerce.cancelservice.client;

import com.ecommerce.cancelservice.client.dto.OrderPayload;
import com.ecommerce.cancelservice.config.FeignConfig;
import com.ecommerce.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "order-service",
        url = "${feign.client.order-service.url}",
        configuration = FeignConfig.class
)
public interface OrderServiceClient {

    @GetMapping("/api/orders/{orderId}")
    ApiResponse<OrderPayload> getMyOrder(@PathVariable("orderId") Long orderId);

    @GetMapping("/api/admin/orders/{orderId}")
    ApiResponse<OrderPayload> getAdminOrder(@PathVariable("orderId") Long orderId);
}
