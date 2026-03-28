package com.ecommerce.orderservice.entity;

import com.ecommerce.orderservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_order_number", columnList = "order_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_before_cancel_request", length = 20)
    OrderStatus statusBeforeCancelRequest;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    BigDecimal finalAmount;

    @Column(name = "user_coupon_id")
    Long userCouponId;

    @Column(name = "shipping_address", length = 500)
    String shippingAddress;

    @Column(name = "recipient_name", length = 50)
    String recipientName;

    @Column(name = "recipient_phone", length = 20)
    String recipientPhone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<OrderItem> orderItems = new ArrayList<>();

    public static Order create(Long userId, BigDecimal totalAmount, Long userCouponId,
                               String shippingAddress, String recipientName, String recipientPhone) {
        return Order.builder()
                .userId(userId)
                .orderNumber(generateOrderNumber())
                .totalAmount(totalAmount)
                .finalAmount(totalAmount)
                .userCouponId(userCouponId)
                .shippingAddress(shippingAddress)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .build();
    }

    private static String generateOrderNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void applyDiscount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
        this.finalAmount = this.totalAmount.subtract(discountAmount);
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.statusBeforeCancelRequest = null;
    }

    /** 취소 서비스에서 취소 신청 접수 시 진행 상태 표시용 */
    public void markCancelRequested() {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.CANCEL_REQUESTED) {
            return;
        }
        if (!this.status.canMarkCancelRequested()) {
            return;
        }
        this.statusBeforeCancelRequest = this.status;
        this.status = OrderStatus.CANCEL_REQUESTED;
    }

    /** 관리자가 취소 신청을 거부한 경우 취소 신청 직전 상태로 복귀 */
    public void restoreAfterCancelRejected() {
        if (this.status != OrderStatus.CANCEL_REQUESTED) {
            return;
        }
        this.status = this.statusBeforeCancelRequest != null
                ? this.statusBeforeCancelRequest
                : OrderStatus.CONFIRMED;
        this.statusBeforeCancelRequest = null;
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public boolean canCancel() {
        return status.canCancel();
    }

    public boolean canUpdateStatus() {
        return status.canUpdateStatus();
    }
}
