package com.ecommerce.orderservice.dto.request;

import jakarta.validation.Valid;
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

    @NotEmpty(message = "주문 상품은 필수입니다.")
    @Valid
    List<OrderItemRequest> items;

    Long userCouponId;

    @NotBlank(message = "배송지는 필수입니다.")
    String shippingAddress;

    @NotBlank(message = "수령인 이름은 필수입니다.")
    String recipientName;

    @NotBlank(message = "수령인 전화번호는 필수입니다.")
    String recipientPhone;
}
