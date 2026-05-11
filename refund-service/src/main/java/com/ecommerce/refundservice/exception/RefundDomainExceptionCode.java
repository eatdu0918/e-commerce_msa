package com.ecommerce.refundservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BaseExceptionCode;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum RefundDomainExceptionCode implements BaseExceptionCode {

    // Refund 관련 예외 코드
    RefundNotFoundException(HttpStatus.NOT_FOUND, "환불 정보를 찾을 수 없습니다."),
    RefundAlreadyCompletedException(HttpStatus.BAD_REQUEST, "이미 완료된 환불입니다."),
    RefundAlreadyProcessingException(HttpStatus.BAD_REQUEST, "이미 처리 중인 환불입니다."),
    InvalidRefundStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 환불 상태입니다."),
    RefundAmountExceedsPaymentException(HttpStatus.BAD_REQUEST, "환불 금액이 결제 금액을 초과할 수 없습니다."),

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
