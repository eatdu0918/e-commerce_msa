package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.client.dto.OrderCancelSummaryResponse;
import com.ecommerce.orderservice.client.dto.OrderCancelSyncResponse;
import com.ecommerce.orderservice.config.FeignConfig;
import com.ecommerce.common.response.ApiResponse;
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

    @GetMapping("/api/cancels/by-order/{orderId}/sync")
    ApiResponse<OrderCancelSyncResponse> getCancelSyncForOrder(@PathVariable("orderId") Long orderId);

    /** ?  ?    JWT ??    ??     ????? ? ?  ???        ID    ? ?  ??    ??    */
    @GetMapping("/api/admin/cancels/orders/{orderId}/active")
    ApiResponse<OrderCancelSummaryResponse> getActiveCancelForOrderAdmin(@PathVariable("orderId") Long orderId);

    @GetMapping("/api/admin/cancels/orders/{orderId}/sync")
    ApiResponse<OrderCancelSyncResponse> getCancelSyncForOrderAdmin(@PathVariable("orderId") Long orderId);
}
