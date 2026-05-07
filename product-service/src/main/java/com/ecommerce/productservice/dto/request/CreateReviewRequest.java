package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateReviewRequest {

    @NotNull(message = "?  ? ID???   ??  ??")
    Long productId;

    @NotNull(message = "??  ?? ?   ??  ??")
    @Min(value = 1, message = "??  ?? 1 ??  ??  ????  ??")
    @Max(value = 5, message = "??  ?? 5 ??  ??  ????  ??")
    Integer score;

    @NotBlank(message = "?    ??  ?? ?   ??  ??")
    @Size(min = 5, max = 1000, message = "?    ??  ?? 5????   1000????  ??  ????  ??")
    String content;

    String imageUrl;
}
