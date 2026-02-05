package com.ecommerce.cancelservice.dto.request;

import com.ecommerce.cancelservice.enums.CancelReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateCancelRequest {

    @NotNull(message = "주문 ID는 필수입니다.")
    Long orderId;

    @NotBlank(message = "주문 번호는 필수입니다.")
    String orderNumber;

    @NotNull(message = "취소 사유는 필수입니다.")
    CancelReason cancelReason;

    String cancelDetail;

    @NotEmpty(message = "취소 상품은 필수입니다.")
    @Valid
    List<CancelItemRequest> items;
}
