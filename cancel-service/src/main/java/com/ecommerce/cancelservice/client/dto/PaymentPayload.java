package com.ecommerce.cancelservice.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * payment-service에서 결제 정보를 조회할 때 사용하는 데이터 구조입니다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentPayload {
    Long id;
    Long orderId;
    Long userId;
    String paymentNumber;
    String status;
}
