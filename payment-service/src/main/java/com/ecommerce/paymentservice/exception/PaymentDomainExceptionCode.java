package com.ecommerce.paymentservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PaymentDomainExceptionCode {

    // Payment ?  ????  
    PaymentNotFoundException(HttpStatus.NOT_FOUND, "   ?  ?   ??????  ??  ."),
    PaymentAlreadyCompletedException(HttpStatus.BAD_REQUEST, "?? ? ?   ??   ???  ??"),
    PaymentAlreadyCancelledException(HttpStatus.BAD_REQUEST, "?? ? ?  ???   ???  ??"),
    PaymentAlreadyRefundedException(HttpStatus.BAD_REQUEST, "?? ? ??  ??   ???  ??"),
    InvalidPaymentStatusException(HttpStatus.BAD_REQUEST, "?   ??? ???    ???   ??  ??"),
    PaymentAmountMismatchException(HttpStatus.BAD_REQUEST, "   ??    ????  ??? ??  ??  ."),
    InvalidPaymentMethodException(HttpStatus.BAD_REQUEST, "?   ??? ???    ????  ??  ??"),

    // JWT ?  ????  
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "?   ??? ??? ?   ??  ??"),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "    ???   ??  ??"),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "?  ??    ?   ??  ??"),
    AccessDeniedException(HttpStatus.FORBIDDEN, "?        ????  ??  .");

    final HttpStatus status;
    final String message;
}
