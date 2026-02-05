package com.ecommerce.refundservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum RefundDomainExceptionCode {

    // Refund 관련 예외
    RefundNotFoundException(HttpStatus.NOT_FOUND, "환불을 찾을 수 없습니다."),
    RefundAlreadyCompletedException(HttpStatus.BAD_REQUEST, "이미 완료된 환불입니다."),
    RefundAlreadyProcessingException(HttpStatus.BAD_REQUEST, "이미 처리중인 환불입니다."),
    InvalidRefundStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 환불 상태입니다."),
    RefundAmountExceedsPaymentException(HttpStatus.BAD_REQUEST, "환불 금액이 결제 금액을 초과합니다."),

    // JWT 관련 예외
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "무효화된 토큰입니다."),
    AccessDeniedException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    final HttpStatus status;
    final String message;
}
