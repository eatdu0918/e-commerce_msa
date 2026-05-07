package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProgressStatusResolverTest {

    @Test
    @DisplayName("   ???    +  ???  ???  : ??   ?   ??SHIPPING")
    void skipConfirm_preparing_showsShippingWhenPaid() {
        OrderStatus r = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                OrderStatus.PENDING, "COMPLETED", null, true, false);
        assertThat(r).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    @DisplayName("   ???    + ????  ???  : ??   ?   ??DELIVERED")
    void skipAll_showsDeliveredWhenPaid() {
        OrderStatus r = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                OrderStatus.CONFIRMED, "COMPLETED", null, true, true);
        assertThat(r).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("?  ???       ?  ???  ???      ????   CANCEL_REQUESTED")
    void activeCancelRequested_takesPrecedence() {
        OrderStatus r = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                OrderStatus.CONFIRMED, "COMPLETED", "REQUESTED", true, true);
        assertThat(r).isEqualTo(OrderStatus.CANCEL_REQUESTED);
    }

    @Test
    @DisplayName("   ??   ?  ??  ??  ???      ??   ???)
    void unpaid_noSkipElevation() {
        OrderStatus r = OrderProgressStatusResolver.resolveForDisplayWithActiveCancel(
                OrderStatus.PENDING, "PENDING", null, true, true);
        assertThat(r).isEqualTo(OrderStatus.PENDING);
    }
}
