package com.ecommerce.common.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public enum CommonExceptionCode implements BaseExceptionCode {
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "??   ?? ? ??          ??  ??  ."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "?? ? ?? ??? ??    ????  ."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "??  ??? ???     ??  ??  ."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "?  ?? ?    ??????  ??  ."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "?        ????  ??  ."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "?? ? ?? ??? ????  ??  .");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}