package com.ecommerce.userservice.dto.request;

import com.ecommerce.userservice.enums.Gender;
import jakarta.validation.constraints.Email;
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
public class SignUpRequest {

    @NotBlank(message = "??  ??? ?   ??  ??")
    @Email(message = "?? ? ???  ???   ???   ??  .")
    String email;

    @NotBlank(message = "?? ?   ????   ??  ??")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "?? ?   ???8????  , ?  ? ??  , ?  ?     ???? ??   ??  ??"
    )
    String password;

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
