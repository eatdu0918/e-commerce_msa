package com.ecommerce.discountservice.dto.request;

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
public class BulkGrantCouponRequest {

    @NotBlank(message = "쿠폰 코드는 필수입니다.")
    String couponCode;

    @NotEmpty(message = "발급 대상 사용자 ID 목록은 비어 있을 수 없습니다.")
    List<Long> userIds;
}
