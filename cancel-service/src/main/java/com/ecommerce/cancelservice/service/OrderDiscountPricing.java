package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.client.dto.OrderPayload;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 주문 금액(할인 전/후)과 주문 라인 합계를 기준으로 취소 품목 단가·환불액을 맞춘다.
 * {@link RefundAmountCalculator}와 동일한 비중·비율 규칙을 공유한다.
 */
final class OrderDiscountPricing {

    private OrderDiscountPricing() {
    }

    /**
     * PG 승인액이 주문 합계보다 작으면 실결제 기준으로 본다.
     */
    static BigDecimal effectiveFinalAmount(OrderPayload order) {
        if (order == null) {
            return BigDecimal.ZERO;
        }
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

    static Map<Long, LineAggregate> aggregateLinesByProductId(OrderPayload order) {
        Map<Long, LineAggregate> map = new HashMap<>();
        if (order == null || order.getItems() == null) {
            return map;
        }
        for (OrderPayload.OrderItemLinePayload line : order.getItems()) {
            if (line.getProductId() == null
                    || line.getQuantity() == null
                    || line.getQuantity() <= 0
                    || line.getTotalPrice() == null) {
                continue;
            }
            map.merge(
                    line.getProductId(),
                    new LineAggregate(line.getQuantity(), line.getTotalPrice()),
                    LineAggregate::merge
            );
        }
        return map;
    }

    /**
     * 취소(반품) 라인에 대한 할인 반영 단가. 주문 스냅샷으로 산출할 수 없으면 {@code fallbackUnitPrice}.
     */
    static BigDecimal discountedUnitPriceForCancelQuantity(
            OrderPayload order,
            Long productId,
            int quantity,
            BigDecimal fallbackUnitPrice) {
        if (order == null || productId == null || quantity <= 0) {
            return money(fallbackUnitPrice);
        }
        BigDecimal orderTotal = order.getTotalAmount();
        if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return money(fallbackUnitPrice);
        }
        Map<Long, LineAggregate> byProduct = aggregateLinesByProductId(order);
        LineAggregate agg = byProduct.get(productId);
        if (agg == null || agg.totalQuantity <= 0 || agg.lineTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return money(fallbackUnitPrice);
        }
        if (quantity > agg.totalQuantity) {
            return money(fallbackUnitPrice);
        }
        BigDecimal portion = agg.lineTotal
                .multiply(BigDecimal.valueOf(quantity))
                .divide(BigDecimal.valueOf(agg.totalQuantity), 10, RoundingMode.HALF_UP);
        BigDecimal finalAmount = effectiveFinalAmount(order);
        BigDecimal discountedLine = portion.multiply(finalAmount).divide(orderTotal, 10, RoundingMode.HALF_UP);
        return discountedLine.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal money(BigDecimal v) {
        if (v == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    static final class LineAggregate {
        final int totalQuantity;
        final BigDecimal lineTotal;

        LineAggregate(int totalQuantity, BigDecimal lineTotal) {
            this.totalQuantity = totalQuantity;
            this.lineTotal = lineTotal;
        }

        static LineAggregate merge(LineAggregate a, LineAggregate b) {
            return new LineAggregate(
                    a.totalQuantity + b.totalQuantity,
                    a.lineTotal.add(b.lineTotal)
            );
        }
    }
}
