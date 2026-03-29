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

    /** 진행 중 취소(REQUESTED/APPROVED/COMPLETED) 요약. 없으면 null. */
    OrderCancelSummaryResponse activeCancel;

    /** 해당 주문에 출고 전 주문 취소(ORDER_CANCEL)가 거절된 이력이 있으면 true */
    boolean hasRejectedOrderCancelRequest;

    /** 해당 주문에 반품·환불(RETURN_REFUND)가 거절된 이력이 있으면 true */
    boolean hasRejectedReturnRefundRequest;
}
