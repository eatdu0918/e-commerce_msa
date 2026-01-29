package com.ecommerce.userservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum UserDomainExceptionCode {

    DuplicateEmailException(HttpStatus.ALREADY_REPORTED, "이미 사용 중인 이메일입니다."),
    EmailNotFoundException(HttpStatus.NOT_FOUND, "일치하는 이메일이 없습니다."),
    InvalidPasswordException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),
    SamePasswordException(HttpStatus.BAD_REQUEST, "새 비밀번호는 현재 비밀번호와 달라야 합니다."),
    UserAlreadyWithdrawnException(HttpStatus.BAD_REQUEST, "이미 탈퇴한 회원입니다."),
    UserNotFoundException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");

    final HttpStatus status;
    final String message;
}
