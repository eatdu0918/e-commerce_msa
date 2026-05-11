package com.ecommerce.orderservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BaseExceptionCode;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum OrderDomainExceptionCode implements BaseExceptionCode {

    // Order 관련 예외 코드
    OrderNotFoundException(HttpStatus.NOT_FOUND, "주문 정보를 찾을 수 없습니다."),
    OrderCannotBeCancelledException(HttpStatus.BAD_REQUEST, "취소가 불가능한 주문 상태입니다."),
    OrderCannotBeUpdatedException(HttpStatus.BAD_REQUEST, "주문 정보를 수정할 수 없는 상태입니다."),
    OrderAlreadyCancelledException(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다."),
    OrderAlreadyCompletedException(HttpStatus.BAD_REQUEST, "이미 완료된 주문입니다."),
    InvalidOrderStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태입니다."),
    OrderStatusTransitionNotAllowedException(HttpStatus.BAD_REQUEST, "허용되지 않는 주문 상태 변경입니다."),
    OrderFulfillmentBlockedByCancelOrRefundException(HttpStatus.BAD_REQUEST,
            "취소 또는 환불 절차가 진행 중이어서 주문 처리가 불가능합니다."),
    EmptyOrderItemsException(HttpStatus.BAD_REQUEST, "주문할 상품이 없습니다."),

    // OrderItem 관련 예외 코드
    InvalidQuantityException(HttpStatus.BAD_REQUEST, "유효하지 않은 수량입니다."),

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
