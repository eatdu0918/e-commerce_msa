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

    /** 주문 확인·상품 준비 단계 생략(결제 완료 후 배송 중까지 즉시) */
    Boolean skipConfirmAndPreparing;

    /** 배송 중·배송 완료 단계 생략 — {@code skipConfirmAndPreparing}가 true일 때만 true 허용 */
    Boolean skipShippingAndDelivered;

    @AssertTrue(message = "배송 단계 생략을 선택하려면 먼저 주문 확인·상품 준비 단계 생략을 선택해야 합니다.")
    public boolean isFulfillmentSkipCombinationValid() {
        if (Boolean.TRUE.equals(skipShippingAndDelivered)) {
            return Boolean.TRUE.equals(skipConfirmAndPreparing);
        }
        return true;
    }
}
