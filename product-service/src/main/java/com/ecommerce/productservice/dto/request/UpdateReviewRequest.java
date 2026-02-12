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
public class UpdateReviewRequest {

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5 이하이어야 합니다.")
    Integer score;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 5, max = 1000, message = "리뷰 내용은 5자 이상 1000자 이하이어야 합니다.")
    String content;

    String imageUrl;
}
