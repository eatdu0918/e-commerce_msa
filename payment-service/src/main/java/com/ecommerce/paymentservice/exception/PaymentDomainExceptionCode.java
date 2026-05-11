package com.ecommerce.paymentservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BaseExceptionCode;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PaymentDomainExceptionCode implements BaseExceptionCode {

    // Payment 관련 예외 코드
    PaymentNotFoundException(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PaymentAlreadyCompletedException(HttpStatus.BAD_REQUEST, "이미 완료된 결제입니다."),
    PaymentAlreadyCancelledException(HttpStatus.BAD_REQUEST, "이미 취소된 결제입니다."),
    PaymentAlreadyRefundedException(HttpStatus.BAD_REQUEST, "이미 환불된 결제입니다."),
    InvalidPaymentStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 상태입니다."),
    PaymentAmountMismatchException(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    InvalidPaymentMethodException(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 수단입니다."),

    // JWT 관련 예외 코드
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "사용이 중지된 토큰입니다."),
    AccessDeniedException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    final HttpStatus status;
    final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
