package com.ecommerce.discountservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum DiscountDomainExceptionCode {

    // Coupon 관련 예외
    CouponNotFoundException(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    CouponAlreadyExistsException(HttpStatus.CONFLICT, "이미 존재하는 쿠폰 코드입니다."),
    CouponExpiredException(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다."),
    CouponNotActiveException(HttpStatus.BAD_REQUEST, "비활성화된 쿠폰입니다."),
    CouponOutOfStockException(HttpStatus.BAD_REQUEST, "쿠폰 수량이 소진되었습니다."),
    CouponNotValidException(HttpStatus.BAD_REQUEST, "사용 가능한 쿠폰이 아닙니다."),
    CouponMinOrderAmountNotMetException(HttpStatus.BAD_REQUEST, "최소 주문 금액을 충족하지 않습니다."),

    // UserCoupon 관련 예외
    UserCouponNotFoundException(HttpStatus.NOT_FOUND, "사용자 쿠폰을 찾을 수 없습니다."),
    UserCouponAlreadyClaimedException(HttpStatus.CONFLICT, "이미 발급받은 쿠폰입니다."),
    UserCouponAlreadyUsedException(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다."),
    UserCouponNotAvailableException(HttpStatus.BAD_REQUEST, "사용 가능한 쿠폰이 아닙니다."),

    // JWT 관련 예외
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "무효화된 토큰입니다."),
    AccessDeniedException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    final HttpStatus status;
    final String message;
}
