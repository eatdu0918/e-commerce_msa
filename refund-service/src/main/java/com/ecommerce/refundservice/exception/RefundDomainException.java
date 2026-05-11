package com.ecommerce.refundservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BusinessException;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundDomainException extends BusinessException {

    public RefundDomainException(RefundDomainExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
