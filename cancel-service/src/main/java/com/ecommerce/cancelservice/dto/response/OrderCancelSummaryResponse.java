package com.ecommerce.cancelservice.dto.response;

import com.ecommerce.cancelservice.enums.CancelStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCancelSummaryResponse {

    Long cancelId;
    String cancelNumber;
    CancelStatus status;
}
