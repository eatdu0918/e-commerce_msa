package com.ecommerce.productservice.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

import com.ecommerce.common.exception.BusinessException;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductDomainException extends BusinessException {

    public ProductDomainException(ProductDomainExceptionCode exceptionCode) {
        super(exceptionCode);
    }
}
