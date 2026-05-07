package com.ecommerce.paymentservice.dto.request;

import com.ecommerce.paymentservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePaymentRequest {

    @NotNull(message = "     ID???   ??  ??")
    Long orderId;

    @NotBlank(message = "        ????   ??  ??")
    String orderNumber;

    @NotNull(message = "   ????  ?? ?   ??  ??")
    PaymentMethod paymentMethod;

    @NotNull(message = "   ??    ?? ?   ??  ??")
    @Positive(message = "   ??    ?? 0  ????   ???  ??")
    BigDecimal amount;

    String paymentDetails;
}
