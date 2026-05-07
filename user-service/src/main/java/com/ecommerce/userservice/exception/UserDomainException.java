package com.ecommerce.userservice.exception;

import com.ecommerce.common.exception.BusinessException;
import lombok.Getter;

@Getter
public class UserDomainException extends BusinessException {

    public UserDomainException(UserDomainExceptionCode userDomainExceptionCode) {
        super(userDomainExceptionCode);
    }

    @Override
    public String getMessage() { return super.getMessage(); }
}
