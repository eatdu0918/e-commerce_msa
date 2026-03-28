package com.ecommerce.orderservice.client.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCancelSummaryResponse {
    Long cancelId;
    String cancelNumber;
    String status;
    /** ORDER_CANCEL | RETURN_REFUND (cancel-service request_type) */
    String requestType;
}
