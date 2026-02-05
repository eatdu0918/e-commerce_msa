package com.ecommerce.paymentservice.entity;

import com.ecommerce.paymentservice.enums.PaymentMethod;
import com.ecommerce.paymentservice.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_payment_number", columnList = "payment_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    Long id;

    @Column(name = "order_id", nullable = false)
    Long orderId;

    @Column(name = "order_number", nullable = false, length = 32)
    String orderNumber;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "payment_number", nullable = false, unique = true, length = 32)
    String paymentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal amount;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    @Column(name = "failed_at")
    LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 500)
    String failureReason;

    public static Payment create(Long orderId, String orderNumber, Long userId,
                                  PaymentMethod paymentMethod, BigDecimal amount) {
        return Payment.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .userId(userId)
                .paymentNumber(generatePaymentNumber())
                .paymentMethod(paymentMethod)
                .amount(amount)
                .build();
    }

    private static String generatePaymentNumber() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public void complete() {
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }
}
