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

    @NotNull(message = "주문 ID는 필수입니다.")
    Long orderId;

    @NotBlank(message = "주문 번호는 필수입니다.")
    String orderNumber;

    @NotNull(message = "결제 수단은 필수입니다.")
    PaymentMethod paymentMethod;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    BigDecimal amount;
}
