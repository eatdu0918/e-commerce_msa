package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.request.StockRequest;
import com.ecommerce.productservice.dto.response.StockResponse;
import com.ecommerce.common.response.ApiResponse;
import com.ecommerce.productservice.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StockController {

    private final StockService stockService;

    @GetMapping("/{productId}")
    public ApiResponse<StockResponse> getStock(
            @PathVariable Long productId,
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage) {
        log.info("GET /api/stocks/{} - ????   ??, productId);
        StockResponse response = stockService.getStock(productId, acceptLanguage);
        return ApiResponse.success(response);
    }

    @PostMapping("/decrease")
    public ApiResponse<StockResponse> decreaseStock(@Valid @RequestBody StockRequest request) {
        log.info("POST /api/stocks/decrease - ????    ?);
        StockResponse response = stockService.decreaseStock(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/increase")
    public ApiResponse<StockResponse> increaseStock(@Valid @RequestBody StockRequest request) {
        log.info("POST /api/stocks/increase - ????   ?");
        StockResponse response = stockService.increaseStock(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/restore")
    public ApiResponse<StockResponse> restoreStock(@Valid @RequestBody StockRequest request) {
        log.info("POST /api/stocks/restore - ????   ??(     ?  ??");
        StockResponse response = stockService.restoreStock(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/check")
    public ApiResponse<Boolean> checkStock(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        log.info("GET /api/stocks/check - ?????   : productId={}, quantity={}", productId, quantity);
        boolean available = stockService.checkStock(productId, quantity);
        return ApiResponse.success(available);
    }
}
