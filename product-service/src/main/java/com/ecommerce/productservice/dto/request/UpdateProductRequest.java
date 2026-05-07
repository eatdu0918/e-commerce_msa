package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.*;
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
public class UpdateProductRequest {

    @NotBlank(message = "?  ?   ? ?   ??  ??")
    @Size(max = 255, message = "?  ?   ? 255????  ?? ???  ??")
    String name;

    @Size(max = 255, message = "??? ?  ?   ? 255????  ?? ???  ??")
    String nameKo;

    String description;

    String descriptionKo;

    String imageUrl;

    @NotNull(message = "      ? ?   ??  ??")
    @DecimalMin(value = "0", message = "      ? 0 ??  ??  ????  ??")
    BigDecimal price;

    @NotNull(message = "??????  ?? ?   ??  ??")
    @Min(value = 0, message = "??????  ?? 0 ??  ??  ????  ??")
    Integer stockQuantity;

    Long categoryId;
}
