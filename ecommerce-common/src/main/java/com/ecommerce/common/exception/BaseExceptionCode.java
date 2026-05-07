package com.ecommerce.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseExceptionCode {
    HttpStatus getStatus();
    String getMessage();
    String name();
}
