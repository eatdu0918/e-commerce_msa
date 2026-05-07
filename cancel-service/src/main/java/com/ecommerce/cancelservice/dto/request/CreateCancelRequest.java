package com.ecommerce.cancelservice.dto.request;

import com.ecommerce.cancelservice.enums.CancelReason;
import com.ecommerce.cancelservice.enums.CancelRequestType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateCancelRequest {

    @NotNull(message = "     ID???   ??  ??")
    Long orderId;

    @NotBlank(message = "        ????   ??  ??")
    String orderNumber;

    @NotNull(message = "?  ????? ???   ??  ??")
    CancelReason cancelReason;

    String cancelDetail;

    /** null?? ???  ?????  ??ORDER_CANCEL) ?   ??*/
    CancelRequestType requestType;

    @NotEmpty(message = "?  ???  ??? ?   ??  ??")
    @Valid
    List<CancelItemRequest> items;
}
