package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCategoryRequest {

    @NotBlank(message = "카테고리명은 필수입니다.")
    @Size(max = 100, message = "카테고리명은 100자 이하여야 합니다.")
    String name;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
    String description;

    Long parentId;

    Integer displayOrder;
}
