package com.ecommerce.refundservice.dto.request;

import com.ecommerce.refundservice.enums.RefundStatus;
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
public class UpdateRefundStatusRequest {

    @NotNull(message = "환불 상태는 필수입니다.")
    RefundStatus status;
}
