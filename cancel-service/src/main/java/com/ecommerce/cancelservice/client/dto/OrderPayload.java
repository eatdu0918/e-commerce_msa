package com.ecommerce.cancelservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * order-service      ?     ??  ??         ?   ??   ???   ???    ???  .
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPayload {
    String status;
    String statusBeforeCancelRequest;
    /**       ??      ??? ??  ????  ??    ??   (??      ????DB  ?????   ????  ). */
    String progressStatus;

    /**      ?  ? ??  ??    ??. */
    BigDecimal totalAmount;

    /**
 * ?   ???      ?   ???   ?     ??       ??.
     *    ???       ??   ???  ?            ???   ??   ???
     */
    BigDecimal finalAmount;

    /** payment-service    ? ??   ?  ??    ? finalAmount       ?   ???  ??????   ?    ?          ????? */
    BigDecimal paymentAmount;

    List<OrderItemLinePayload> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class OrderItemLinePayload {
        Long productId;
        Integer quantity;
        BigDecimal totalPrice;
    }
}
