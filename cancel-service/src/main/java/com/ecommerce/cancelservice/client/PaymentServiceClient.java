package com.ecommerce.cancelservice.client;

import com.ecommerce.cancelservice.client.dto.PaymentPayload;
import com.ecommerce.cancelservice.config.FeignConfig;
import com.ecommerce.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "payment-service",
        url = "${feign.client.payment-service.url}",
        configuration = FeignConfig.class
)
public interface PaymentServiceClient {

    @GetMapping("/api/admin/payments/order/{orderId}")
    ApiResponse<PaymentPayload> getPaymentByOrderId(@PathVariable("orderId") Long orderId);
}
