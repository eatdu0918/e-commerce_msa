package com.ecommerce.discountservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BaseExceptionCode;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum DiscountDomainExceptionCode implements BaseExceptionCode {

    // Coupon 관련 예외 코드
    CouponNotFoundException(HttpStatus.NOT_FOUND, "쿠폰 정보를 찾을 수 없습니다."),
    CouponAlreadyExistsException(HttpStatus.CONFLICT, "동일한 코드의 쿠폰이 이미 존재합니다."),
    CouponExpiredException(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다."),
    CouponNotActiveException(HttpStatus.BAD_REQUEST, "활성화되지 않은 쿠폰입니다."),
    CouponOutOfStockException(HttpStatus.BAD_REQUEST, "쿠폰 재고가 모두 소진되었습니다."),
    CouponNotValidException(HttpStatus.BAD_REQUEST, "유효하지 않은 쿠폰입니다."),
    CouponMinOrderAmountNotMetException(HttpStatus.BAD_REQUEST, "최소 주문 금액을 충족하지 못했습니다."),

    // UserCoupon 관련 예외 코드
    UserCouponNotFoundException(HttpStatus.NOT_FOUND, "사용자 쿠폰 정보를 찾을 수 없습니다."),
    UserCouponAlreadyClaimedException(HttpStatus.CONFLICT, "이미 발급받은 쿠폰입니다."),
    UserCouponAlreadyUsedException(HttpStatus.BAD_REQUEST, "이미 사용한 쿠폰입니다."),
    UserCouponNotAvailableException(HttpStatus.BAD_REQUEST, "사용 가능한 쿠폰이 아닙니다."),

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
