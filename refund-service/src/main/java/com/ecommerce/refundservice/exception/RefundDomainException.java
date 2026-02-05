package com.ecommerce.refundservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundDomainException extends RuntimeException {

    HttpStatus httpStatus;
    String code;

    public RefundDomainException(RefundDomainExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.httpStatus = exceptionCode.getStatus();
        this.code = exceptionCode.name();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
