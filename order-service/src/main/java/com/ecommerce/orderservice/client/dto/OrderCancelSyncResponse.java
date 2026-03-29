package com.ecommerce.orderservice.client.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCancelSyncResponse {

    OrderCancelSummaryResponse activeCancel;
    boolean hasRejectedOrderCancelRequest;
    boolean hasRejectedReturnRefundRequest;
}
