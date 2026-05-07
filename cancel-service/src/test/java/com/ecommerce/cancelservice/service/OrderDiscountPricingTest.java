package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.client.dto.OrderPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDiscountPricingTest {

    @Test
    @DisplayName("?    ??   ?  ?????????(??  ?   ?  ?)/??  ??   ??)
    void fullLine_discountAppliedPerUnit() {
        OrderPayload order = snapshot(
                new BigDecimal("20000"),
                new BigDecimal("1000"),
                List.of(line(1L, 2, new BigDecimal("20000")))
        );

        BigDecimal unit = OrderDiscountPricing.discountedUnitPriceForCancelQuantity(
                order, 1L, 2, new BigDecimal("10000"));

        assertThat(unit).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("?  ????   ?  ?????? ????  ???    ?????")
    void partialQty_matchesRefundCalculatorPortion() {
        OrderPayload order = snapshot(
                new BigDecimal("30000"),
                new BigDecimal("27000"),
                List.of(
                        line(1L, 2, new BigDecimal("20000")),
                        line(2L, 1, new BigDecimal("10000"))
                )
        );

        BigDecimal unit = OrderDiscountPricing.discountedUnitPriceForCancelQuantity(
                order, 1L, 1, new BigDecimal("999999"));

        assertThat(unit).isEqualByComparingTo(new BigDecimal("9000.00"));
    }

    private static OrderPayload snapshot(
            BigDecimal totalAmount,
            BigDecimal finalAmount,
            List<OrderPayload.OrderItemLinePayload> lines
    ) {
        OrderPayload p = new OrderPayload();
        p.setTotalAmount(totalAmount);
        p.setFinalAmount(finalAmount);
        p.setPaymentAmount(null);
        p.setItems(lines);
        return p;
    }

    private static OrderPayload.OrderItemLinePayload line(Long productId, int qty, BigDecimal totalPrice) {
        OrderPayload.OrderItemLinePayload l = new OrderPayload.OrderItemLinePayload();
        l.setProductId(productId);
        l.setQuantity(qty);
        l.setTotalPrice(totalPrice);
        return l;
    }
}
