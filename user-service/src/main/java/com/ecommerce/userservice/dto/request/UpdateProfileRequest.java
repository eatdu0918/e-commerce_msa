package com.ecommerce.userservice.dto.request;

import com.ecommerce.userservice.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateProfileRequest {

    @NotBlank(message = "??  ?? ?   ??  ??")
    String name;

    @NotBlank(message = "?? ???   ????   ??  ??")
    @Pattern(
            regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
            message = "?? ? ??? ???   ???   ???   ??  ."
    )
    String phoneNumber;

    @NotNull(message = "?   ?? ?   ??  ??")
    Gender gender;
}
