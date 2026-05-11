package com.ecommerce.discountservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BusinessException;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DiscountDomainException extends BusinessException {

    public DiscountDomainException(DiscountDomainExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
