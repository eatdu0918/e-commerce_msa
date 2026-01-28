package com.ecommerce.userservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDomainException extends RuntimeException {

    HttpStatus httpstatus;
    String code;

    public UserDomainException(UserDomainExceptionCode userDomainExceptionCode) {
        super(userDomainExceptionCode.getMessage());
        this.httpstatus = userDomainExceptionCode.getStatus();
        this.code = userDomainExceptionCode.name();
    }

    @Override
    public String getMessage() { return super.getMessage(); }
}
