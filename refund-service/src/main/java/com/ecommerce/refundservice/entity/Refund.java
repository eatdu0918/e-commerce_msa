package com.ecommerce.refundservice.entity;

import com.ecommerce.refundservice.enums.RefundReason;
import com.ecommerce.refundservice.enums.RefundStatus;
import com.ecommerce.common.entity.BaseEntity;
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
@Table(name = "refunds", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_cancel_id", columnList = "cancel_id"),
        @Index(name = "idx_payment_id", columnList = "payment_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_refund_number", columnList = "refund_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    Long id;

    @Column(name = "order_id", nullable = false)
    Long orderId;

    @Column(name = "cancel_id", nullable = false)
    Long cancelId;

    @Column(name = "payment_id")
    Long paymentId;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "refund_number", nullable = false, unique = true, length = 32)
    String refundNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    RefundStatus status = RefundStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_reason", nullable = false, length = 20)
    RefundReason refundReason;

    @Column(name = "refund_detail", length = 500)
    String refundDetail;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal amount;

    @Column(name = "completed_at")
    LocalDateTime completedAt;

    @Column(name = "failed_at")
    LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 500)
    String failureReason;

    public static Refund create(Long orderId, Long cancelId, Long paymentId, Long userId,
                                 RefundReason refundReason, String refundDetail, BigDecimal amount) {
        return Refund.builder()
                .orderId(orderId)
                .cancelId(cancelId)
                .paymentId(paymentId)
                .userId(userId)
                .refundNumber(generateRefundNumber())
                .refundReason(refundReason)
                .refundDetail(refundDetail)
                .amount(amount)
                .build();
    }

    private static String generateRefundNumber() {
        return "REF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public void process() {
        this.status = RefundStatus.PROCESSING;
    }

    public void complete() {
        this.status = RefundStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = RefundStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    public void updateStatus(RefundStatus newStatus) {
        this.status = newStatus;
    }

    public boolean canRetry() {
        return this.status == RefundStatus.FAILED;
    }
}
