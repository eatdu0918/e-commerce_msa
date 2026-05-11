package com.ecommerce.orderservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BusinessException;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderDomainException extends BusinessException {

    public OrderDomainException(OrderDomainExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
