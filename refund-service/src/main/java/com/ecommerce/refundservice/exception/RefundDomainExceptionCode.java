package com.ecommerce.refundservice.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum RefundDomainExceptionCode {

    // Refund ?  ????  
    RefundNotFoundException(HttpStatus.NOT_FOUND, "??  ??   ??????  ??  ."),
    RefundAlreadyCompletedException(HttpStatus.BAD_REQUEST, "?? ? ?   ????  ??  ??"),
    RefundAlreadyProcessingException(HttpStatus.BAD_REQUEST, "?? ?    ?      ??  ??  ??"),
    InvalidRefundStatusException(HttpStatus.BAD_REQUEST, "?   ??? ??? ??   ?   ??  ??"),
    RefundAmountExceedsPaymentException(HttpStatus.BAD_REQUEST, "??       ??   ??    ???  ???  ??"),

    // JWT ?  ????  
    InvalidTokenException(HttpStatus.UNAUTHORIZED, "?   ??? ??? ?   ??  ??"),
    ExpiredTokenException(HttpStatus.UNAUTHORIZED, "    ???   ??  ??"),
    BlacklistedTokenException(HttpStatus.UNAUTHORIZED, "?  ??    ?   ??  ??"),
    AccessDeniedException(HttpStatus.FORBIDDEN, "?        ????  ??  .");

    final HttpStatus status;
    final String message;
}
