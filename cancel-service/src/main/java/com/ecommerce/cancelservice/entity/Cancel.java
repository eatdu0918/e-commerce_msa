package com.ecommerce.cancelservice.entity;

import com.ecommerce.cancelservice.enums.CancelReason;
import com.ecommerce.cancelservice.enums.CancelStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cancels", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_cancel_number", columnList = "cancel_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Cancel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cancel_id")
    Long id;

    @Column(name = "order_id", nullable = false)
    Long orderId;

    @Column(name = "order_number", nullable = false, length = 32)
    String orderNumber;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "cancel_number", nullable = false, unique = true, length = 32)
    String cancelNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    CancelStatus status = CancelStatus.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_reason", nullable = false, length = 20)
    CancelReason cancelReason;

    @Column(name = "cancel_detail", length = 500)
    String cancelDetail;

    @Column(name = "rejected_reason", length = 500)
    String rejectedReason;

    @Column(name = "approved_at")
    LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    LocalDateTime rejectedAt;

    @Column(name = "completed_at")
    LocalDateTime completedAt;

    @OneToMany(mappedBy = "cancel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<CancelItem> cancelItems = new ArrayList<>();

    public static Cancel create(Long orderId, String orderNumber, Long userId,
                                 CancelReason cancelReason, String cancelDetail) {
        return Cancel.builder()
                .orderId(orderId)
                .orderNumber(orderNumber)
                .userId(userId)
                .cancelNumber(generateCancelNumber())
                .cancelReason(cancelReason)
                .cancelDetail(cancelDetail)
                .build();
    }

    private static String generateCancelNumber() {
        return "CAN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public void addCancelItem(CancelItem cancelItem) {
        cancelItems.add(cancelItem);
        cancelItem.setCancel(this);
    }

    public void approve() {
        this.status = CancelStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        this.status = CancelStatus.REJECTED;
        this.rejectedReason = reason;
        this.rejectedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = CancelStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isRequested() {
        return this.status == CancelStatus.REQUESTED;
    }
}
