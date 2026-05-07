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

    @NotBlank(message = "   ?    ?  ?? ?   ??  ??")
    @Size(max = 100, message = "   ?    ?  ?? 100????  ?? ???  ??")
    String name;

    @Size(max = 100, message = "???    ?    ?  ?? 100????  ?? ???  ??")
    String nameKo;

    @Size(max = 500, message = "??  ?? 500????  ?? ???  ??")
    String description;

    @Size(max = 500, message = "??? ??  ?? 500????  ?? ???  ??")
    String descriptionKo;

    Long parentId;

    Integer displayOrder;
}
