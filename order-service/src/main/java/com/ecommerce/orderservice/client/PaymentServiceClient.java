package com.ecommerce.orderservice.client;

import com.ecommerce.orderservice.client.dto.PaymentInfo;
import com.ecommerce.orderservice.config.FeignConfig;
import com.ecommerce.orderservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "payment-service",
        url = "${feign.client.payment-service.url}",
        configuration = FeignConfig.class
)
public interface PaymentServiceClient {

    @GetMapping("/api/payments/order/{orderId}")
    ApiResponse<PaymentInfo> getPaymentByOrderId(@PathVariable("orderId") Long orderId);
}
