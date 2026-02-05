package com.ecommerce.discountservice.exception;

import com.ecommerce.discountservice.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DiscountDomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDiscountDomainException(DiscountDomainException e) {
        log.error("DiscountDomainException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .error(ApiResponse.Error.of(e.getCode(), e.getMessage()))
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("유효성 검사 실패");

        log.error("Validation error: {}", errorMessage);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .error(ApiResponse.Error.of("VALIDATION_ERROR", errorMessage))
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error: ", e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .error(ApiResponse.Error.of("INTERNAL_ERROR", "서버 오류가 발생했습니다."))
                        .build());
    }
}
