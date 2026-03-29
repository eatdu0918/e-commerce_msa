package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.client.dto.OrderPayload;
import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.entity.CancelItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 취소/반품 환불액: 주문 라인(할인 전) 비중에 {@link OrderPayload#getFinalAmount()} 배율을 곱해 실제 결제액 기준으로 맞춘다.
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
            return effectiveFinalAmount(order);
        }

        Map<Long, ProductLineAggregate> byProductId = aggregateOrderLinesByProduct(order);
        BigDecimal cancelledPreDiscount = BigDecimal.ZERO;

        for (CancelItem cancelItem : cancel.getCancelItems()) {
            if (cancelItem.getProductId() == null
                    || cancelItem.getQuantity() == null
                    || cancelItem.getQuantity() <= 0) {
                return sumCancelItemTotals(cancel);
            }

            ProductLineAggregate agg = byProductId.get(cancelItem.getProductId());
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

        BigDecimal finalAmount = effectiveFinalAmount(order);
        return cancelledPreDiscount
                .multiply(finalAmount)
                .divide(orderTotal, 2, RoundingMode.HALF_UP);
    }

    /**
     * PG({@link OrderPayload#getPaymentAmount()})가 주문 합계보다 작으면 그 금액을 실결제로 본다.
     * DB {@code final_amount}·쿠폰 사가가 늦어도 환불액이 할인 후 결제와 맞도록 한다.
     */
    private static BigDecimal effectiveFinalAmount(OrderPayload order) {
        BigDecimal total = order.getTotalAmount();
        BigDecimal pay = order.getPaymentAmount();
        if (total != null
                && pay != null
                && pay.compareTo(BigDecimal.ZERO) > 0
                && pay.compareTo(total) < 0) {
            return pay;
        }
        BigDecimal finalAmount = order.getFinalAmount();
        if (finalAmount != null && finalAmount.compareTo(BigDecimal.ZERO) >= 0) {
            return finalAmount;
        }
        return total != null ? total : BigDecimal.ZERO;
    }

    private static Map<Long, ProductLineAggregate> aggregateOrderLinesByProduct(OrderPayload order) {
        Map<Long, ProductLineAggregate> map = new HashMap<>();
        for (OrderPayload.OrderItemLinePayload line : order.getItems()) {
            if (line.getProductId() == null
                    || line.getQuantity() == null
                    || line.getQuantity() <= 0
                    || line.getTotalPrice() == null) {
                continue;
            }
            map.merge(
                    line.getProductId(),
                    new ProductLineAggregate(line.getQuantity(), line.getTotalPrice()),
                    ProductLineAggregate::merge
            );
        }
        return map;
    }

    private static BigDecimal sumCancelItemTotals(Cancel cancel) {
        return cancel.getCancelItems().stream()
                .map(CancelItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static final class ProductLineAggregate {
        int totalQuantity;
        BigDecimal lineTotal;

        static ProductLineAggregate merge(ProductLineAggregate a, ProductLineAggregate b) {
            return new ProductLineAggregate(
                    a.totalQuantity + b.totalQuantity,
                    a.lineTotal.add(b.lineTotal)
            );
        }
    }
}
