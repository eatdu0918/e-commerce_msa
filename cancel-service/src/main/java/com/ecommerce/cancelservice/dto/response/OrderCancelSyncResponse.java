package com.ecommerce.cancelservice.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCancelSyncResponse {

    /**     ? ??  ??REQUESTED/APPROVED/COMPLETED) ?   . ??   ?null. */
    OrderCancelSummaryResponse activeCancel;

    /** ?? ??    ???  ????     ?  ??ORDER_CANCEL)       ????? ?????   ?true */
    boolean hasRejectedOrderCancelRequest;

    /** ?? ??    ??      ??  (RETURN_REFUND)       ????? ?????   ?true */
    boolean hasRejectedReturnRefundRequest;
}
