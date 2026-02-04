package com.ecommerce.orderservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum OrderDomainExceptionCode {

    // Order 관련 예외
    OrderNotFoundException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    OrderCannotBeCancelledException(HttpStatus.BAD_REQUEST, "취소할 수 없는 주문 상태입니다."),
    OrderCannotBeUpdatedException(HttpStatus.BAD_REQUEST, "상태를 변경할 수 없는 주문입니다."),
    OrderAlreadyCancelledException(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다."),
    OrderAlreadyCompletedException(HttpStatus.BAD_REQUEST, "이미 완료된 주문입니다."),
    InvalidOrderStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태입니다."),
    EmptyOrderItemsException(HttpStatus.BAD_REQUEST, "주문 상품이 비어있습니다."),

    // OrderItem 관련 예외
    InvalidQuantityException(HttpStatus.BAD_REQUEST, "유효하지 않은 수량입니다."),

    // JWT 관련 예외
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "무효화된 토큰입니다."),
    AccessDeniedException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    final HttpStatus status;
    final String message;
}
