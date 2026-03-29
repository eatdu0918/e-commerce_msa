package com.ecommerce.cancelservice.service;

import com.ecommerce.cancelservice.client.dto.OrderPayload;
import com.ecommerce.cancelservice.entity.Cancel;
import com.ecommerce.cancelservice.entity.CancelItem;
import com.ecommerce.cancelservice.enums.CancelReason;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RefundAmountCalculatorTest {

    @Test
    @DisplayName("전체 취소 시 환불액은 주문 finalAmount와 같다")
    void fullCancel_matchesFinalAmount() {
        Cancel cancel = baseCancel();
        OrderPayload order = orderSnapshot(
                new BigDecimal("20000"),
                new BigDecimal("1000"),
                List.of(line(1L, 2, new BigDecimal("20000")))
        );

        BigDecimal refund = RefundAmountCalculator.computeRefundAmount(cancel, order);

        assertThat(refund).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("주문 finalAmount가 지연됐어도 paymentAmount가 더 작으면 실결제 기준으로 환불한다")
    void fullCancel_prefersPaymentAmountWhenSmallerThanTotal() {
        Cancel cancel = baseCancel();
        cancel.getCancelItems().clear();
        cancel.addCancelItem(CancelItem.create(1L, "테스트 상품", 1, new BigDecimal("1000")));

        OrderPayload order = orderSnapshot(
                new BigDecimal("1000"),
                new BigDecimal("1000"),
                List.of(line(1L, 1, new BigDecimal("1000")))
        );
        order.setPaymentAmount(new BigDecimal("900"));

        BigDecimal refund = RefundAmountCalculator.computeRefundAmount(cancel, order);

        assertThat(refund).isEqualByComparingTo(new BigDecimal("900.00"));
    }

    @Test
    @DisplayName("부분 취소 시 할인 비율에 따라 라인 비중만큼 환불한다")
    void partialCancel_appliesDiscountRatioToLinePortion() {
        Cancel cancel = baseCancel();
        cancel.getCancelItems().clear();
        cancel.addCancelItem(CancelItem.create(1L, "A", 1, new BigDecimal("999999")));

        OrderPayload order = orderSnapshot(
                new BigDecimal("30000"),
                new BigDecimal("27000"),
                List.of(
                        line(1L, 2, new BigDecimal("20000")),
                        line(2L, 1, new BigDecimal("10000"))
                )
        );

        BigDecimal refund = RefundAmountCalculator.computeRefundAmount(cancel, order);

        assertThat(refund).isEqualByComparingTo(new BigDecimal("9000.00"));
    }

    private static Cancel baseCancel() {
        Cancel cancel = Cancel.create(
                100L, "ORD-1", 1L, CancelReason.CHANGE_OF_MIND, "memo", CancelRequestType.ORDER_CANCEL
        );
        ReflectionTestUtils.setField(cancel, "id", 1L);
        cancel.addCancelItem(CancelItem.create(1L, "테스트 상품", 2, new BigDecimal("10000")));
        return cancel;
    }

    private static OrderPayload orderSnapshot(
            BigDecimal totalAmount,
            BigDecimal finalAmount,
            List<OrderPayload.OrderItemLinePayload> lines
    ) {
        OrderPayload p = new OrderPayload();
        p.setTotalAmount(totalAmount);
        p.setFinalAmount(finalAmount);
        p.setItems(lines);
        p.setPaymentAmount(null);
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
