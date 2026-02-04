package com.ecommerce.orderservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDomainException extends RuntimeException {

    HttpStatus httpStatus;
    String code;

    public OrderDomainException(OrderDomainExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.httpStatus = exceptionCode.getStatus();
        this.code = exceptionCode.name();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
