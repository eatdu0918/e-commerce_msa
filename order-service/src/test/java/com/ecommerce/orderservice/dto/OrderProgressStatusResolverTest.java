package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProgressStatusResolverTest {

    @Test
    @DisplayName("결제 완료 + 첫 단계 생략: 표시 상태는 SHIPPING")
    void skipConfirm_preparing_showsShippingWhenPaid() {
        OrderStatus r = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                OrderStatus.PENDING, "COMPLETED", null, true, false);
        assertThat(r).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    @DisplayName("결제 완료 + 두 단계 생략: 표시 상태는 DELIVERED")
    void skipAll_showsDeliveredWhenPaid() {
        OrderStatus r = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                OrderStatus.CONFIRMED, "COMPLETED", null, true, true);
        assertThat(r).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("취소 요청 중이면 단계 생략 보정 없이 CANCEL_REQUESTED")
    void activeCancelRequested_takesPrecedence() {
        OrderStatus r = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                OrderStatus.CONFIRMED, "COMPLETED", "REQUESTED", true, true);
        assertThat(r).isEqualTo(OrderStatus.CANCEL_REQUESTED);
    }

    @Test
    @DisplayName("결제 미완료면 단계 생략 보정 미적용")
    void unpaid_noSkipElevation() {
        OrderStatus r = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                OrderStatus.PENDING, "PENDING", null, true, true);
        assertThat(r).isEqualTo(OrderStatus.PENDING);
    }
}
