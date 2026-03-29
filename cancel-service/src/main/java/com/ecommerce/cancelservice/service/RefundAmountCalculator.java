package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.client.dto.OrderPayload;
import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.entity.CancelItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 취소/반품 환불액: 주문 라인(할인 전) 비중에 실결제·{@link OrderPayload#getFinalAmount()} 배율을 곱해 맞춘다.
 */
final class RefundAmountCalculator {

    private RefundAmountCalculator() {
    }

    static BigDecimal computeRefundAmount(Cancel cancel, OrderPayload order) {
        if (cancel.getCancelItems() == null || cancel.getCancelItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        if (order == null
                || order.getTotalAmount() == null
                || order.getItems() == null
                || order.getItems().isEmpty()) {
            return sumCancelItemTotals(cancel);
        }

        BigDecimal orderTotal = order.getTotalAmount();
        if (orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return OrderDiscountPricing.effectiveFinalAmount(order).setScale(2, RoundingMode.HALF_UP);
        }

        Map<Long, OrderDiscountPricing.LineAggregate> byProductId = OrderDiscountPricing.aggregateLinesByProductId(order);
        BigDecimal cancelledPreDiscount = BigDecimal.ZERO;

        for (CancelItem cancelItem : cancel.getCancelItems()) {
            if (cancelItem.getProductId() == null
                    || cancelItem.getQuantity() == null
                    || cancelItem.getQuantity() <= 0) {
                return sumCancelItemTotals(cancel);
            }

            OrderDiscountPricing.LineAggregate agg = byProductId.get(cancelItem.getProductId());
            if (agg == null || agg.totalQuantity <= 0 || agg.lineTotal.compareTo(BigDecimal.ZERO) <= 0) {
                return sumCancelItemTotals(cancel);
            }
            if (cancelItem.getQuantity() > agg.totalQuantity) {
                return sumCancelItemTotals(cancel);
            }

            BigDecimal portion = agg.lineTotal
                    .multiply(BigDecimal.valueOf(cancelItem.getQuantity()))
                    .divide(BigDecimal.valueOf(agg.totalQuantity), 10, RoundingMode.HALF_UP);
            cancelledPreDiscount = cancelledPreDiscount.add(portion);
        }

        if (cancelledPreDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal finalAmount = OrderDiscountPricing.effectiveFinalAmount(order);
        return cancelledPreDiscount
                .multiply(finalAmount)
                .divide(orderTotal, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal sumCancelItemTotals(Cancel cancel) {
        return cancel.getCancelItems().stream()
                .map(CancelItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
