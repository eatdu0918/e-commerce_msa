package com.ecommerce.userservice.exception;

import com.ecommerce.common.exception.BaseExceptionCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum UserDomainExceptionCode implements BaseExceptionCode {

    DuplicateEmailException(HttpStatus.ALREADY_REPORTED, "이미 가입된 이메일 주소입니다."),
    EmailNotFoundException(HttpStatus.NOT_FOUND, "가입되지 않은 이메일 주소입니다."),
    InvalidPasswordException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    SamePasswordException(HttpStatus.BAD_REQUEST, "현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
    UserAlreadyWithdrawnException(HttpStatus.BAD_REQUEST, "이미 탈퇴한 회원입니다."),
    UserNotFoundException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // JWT 관련 예외 코드
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "사용이 중지된 토큰입니다."),
    RefreshTokenNotFoundException(HttpStatus.UNAUTHORIZED, "Refresh Token이 존재하지 않습니다."),
    RefreshTokenMismatchException(HttpStatus.UNAUTHORIZED, "Refresh Token이 일치하지 않습니다."),
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
