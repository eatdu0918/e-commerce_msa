package com.ecommerce.cancelservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BusinessException;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CancelDomainException extends BusinessException {

    public CancelDomainException(CancelDomainExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
