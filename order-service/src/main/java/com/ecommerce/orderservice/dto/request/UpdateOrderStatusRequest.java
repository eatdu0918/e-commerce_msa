package com.ecommerce.orderservice.dto.request;

import com.ecommerce.orderservice.enums.OrderStatus;
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
public class UpdateOrderStatusRequest {

    @NotNull(message = "주문 상태는 필수입니다.")
    OrderStatus status;
}
