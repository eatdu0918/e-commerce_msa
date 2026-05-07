package com.ecommerce.paymentservice.dto.request;

import com.ecommerce.paymentservice.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePaymentStatusRequest {

    @NotNull(message = "   ???   ???   ??  ??")
    PaymentStatus status;
}
