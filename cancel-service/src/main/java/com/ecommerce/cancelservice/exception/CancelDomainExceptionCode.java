package com.ecommerce.cancelservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum CancelDomainExceptionCode {

    // Cancel 관련 예외
    CancelNotFoundException(HttpStatus.NOT_FOUND, "취소 요청을 찾을 수 없습니다."),
    CancelAlreadyApprovedException(HttpStatus.BAD_REQUEST, "이미 승인된 취소 요청입니다."),
    CancelAlreadyRejectedException(HttpStatus.BAD_REQUEST, "이미 거부된 취소 요청입니다."),
    CancelAlreadyCompletedException(HttpStatus.BAD_REQUEST, "이미 완료된 취소 요청입니다."),
    InvalidCancelStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 취소 상태입니다."),
    CancelNotInRequestedStatusException(HttpStatus.BAD_REQUEST, "취소 요청 상태가 아닙니다."),
    EmptyCancelItemsException(HttpStatus.BAD_REQUEST, "취소 상품이 비어있습니다."),
    DuplicateCancelRequestException(HttpStatus.CONFLICT, "해당 주문에 대한 취소 요청이 이미 접수되었습니다."),
    OrderInfoUnavailableException(HttpStatus.BAD_GATEWAY, "주문 정보를 확인할 수 없습니다. 잠시 후 다시 시도해 주세요."),
    CancelBlockedWhileShippingException(HttpStatus.BAD_REQUEST, "배송 중인 주문은 취소·반품 요청을 할 수 없습니다."),
    ReturnRefundOnlyAfterDeliveredException(HttpStatus.BAD_REQUEST, "반품·환불은 배송 완료 후에만 요청할 수 있습니다."),
    OrderCancelOnlyBeforeShippingException(HttpStatus.BAD_REQUEST, "주문 취소는 배송 시작 전(상품 준비까지)만 요청할 수 있습니다."),
    CancelAdminActionBlockedException(HttpStatus.BAD_REQUEST, "배송 중인 주문에 대한 취소 건은 승인·거절 처리할 수 없습니다."),

    // JWT 관련 예외
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "무효화된 토큰입니다."),
    AccessDeniedException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    final HttpStatus status;
    final String message;
}
