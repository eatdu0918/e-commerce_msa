package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.client.dto.OrderCancelSummaryResponse;
import com.ecommerce.orderservice.config.FeignConfig;
import com.ecommerce.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "cancel-service",
        url = "${feign.client.cancel-service.url}",
        configuration = FeignConfig.class
)
public interface CancelServiceClient {

    @GetMapping("/api/cancels/by-order/{orderId}/active")
    ApiResponse<OrderCancelSummaryResponse> getActiveCancelForOrder(@PathVariable("orderId") Long orderId);
}
