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

    DuplicateEmailException(HttpStatus.ALREADY_REPORTED, "이미 사용 중인 이메일입니다.");

    final HttpStatus status;
    final String message;
}
