package com.ecommerce.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrderRequest {

    @NotEmpty(message = "     ?  ??? ?   ??  ??")
    @Valid
    List<OrderItemRequest> items;

    Long userCouponId;

    @NotBlank(message = "   ?  ????   ??  ??")
    String shippingAddress;

    @NotBlank(message = "??  ????  ?? ?   ??  ??")
    String recipientName;

    @NotBlank(message = "??  ???      ????   ??  ??")
    String recipientPhone;

    /**      ?    ?  ?    ????  ???  (   ???    ??   ??   ?  ?    ?? */
    Boolean skipConfirmAndPreparing;

    /**    ??      ???    ??  ???   ??{@code skipConfirmAndPreparing}    true?????  true ??   */
    Boolean skipShippingAndDelivered;

    @AssertTrue(message = "   ????  ???  ???   ??   ??  ?      ?    ?  ?    ????  ???  ???   ??   ??  ??")
    public boolean isFulfillmentSkipCombinationValid() {
        if (Boolean.TRUE.equals(skipShippingAndDelivered)) {
            return Boolean.TRUE.equals(skipConfirmAndPreparing);
        }
        return true;
    }
}
