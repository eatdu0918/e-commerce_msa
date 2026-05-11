package com.ecommerce.cancelservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BaseExceptionCode;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum CancelDomainExceptionCode implements BaseExceptionCode {

    // Cancel 관련 예외 코드
    CancelNotFoundException(HttpStatus.NOT_FOUND, "취소 정보를 찾을 수 없습니다."),
    CancelAlreadyApprovedException(HttpStatus.BAD_REQUEST, "이미 승인된 취소 요청입니다."),
    CancelAlreadyRejectedException(HttpStatus.BAD_REQUEST, "이미 거절된 취소 요청입니다."),
    CancelAlreadyCompletedException(HttpStatus.BAD_REQUEST, "이미 완료된 취소 요청입니다."),
    InvalidCancelStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 취소 상태입니다."),
    CancelNotInRequestedStatusException(HttpStatus.BAD_REQUEST, "취소 요청 상태가 아닙니다."),
    EmptyCancelItemsException(HttpStatus.BAD_REQUEST, "취소할 상품이 없습니다."),
    DuplicateCancelRequestException(HttpStatus.CONFLICT, "중복된 취소 요청이 존재합니다."),
    /** 거절 후 재발행 블록 (예: 결제취소 실패/반품거절 후) - 실제로는 정책에 따라 다를 수 있음. */
    CancelRequestBlockedAfterRejectionException(
            HttpStatus.CONFLICT, "거절된 취소 요청에 대해 재요청이 불가능합니다."),
    OrderInfoUnavailableException(HttpStatus.BAD_GATEWAY, "주문 정보를 가져올 수 없습니다. 주문 서비스 상태를 확인해 주세요."),
    CancelBlockedWhileShippingException(HttpStatus.BAD_REQUEST, "배송 중에는 취소가 불가능합니다."),
    ReturnRefundOnlyAfterDeliveredException(HttpStatus.BAD_REQUEST, "배송 완료 후에만 반품/환불이 가능합니다."),
    OrderCancelOnlyBeforeShippingException(HttpStatus.BAD_REQUEST, "주문 취소는 배송 준비 중 단계까지만 가능합니다."),
    CancelAdminActionBlockedException(HttpStatus.BAD_REQUEST, "현재 상태에서는 관리자 조작이 불가능합니다."),

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
