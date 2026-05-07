package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.Min;
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
public class UpdateCartItemRequest {

    @NotNull(message = "??  ?? ?   ??  ??")
    @Min(value = 1, message = "??  ?? 1 ??  ??  ????  ??")
    Integer quantity;
}
