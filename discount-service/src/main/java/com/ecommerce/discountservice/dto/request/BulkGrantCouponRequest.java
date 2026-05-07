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

    @NotBlank(message = "?   ??   ???   ??  ??")
    String couponCode;

    @NotEmpty(message = "   ???????????ID     ?? ??  ???   ????  ??  .")
    List<Long> userIds;
}
