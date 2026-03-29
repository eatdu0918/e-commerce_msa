package com.ecommerce.orderservice.entity;

import com.ecommerce.orderservice.enums.CancelRequestKind;
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

    /** coupon-used 시 저장되는 표시용 쿠폰 스냅샷 */
    @Column(name = "applied_coupon_name", length = 100)
    String appliedCouponName;

    @Column(name = "applied_coupon_code", length = 50)
    String appliedCouponCode;

    @Column(name = "applied_coupon_type", length = 20)
    String appliedCouponType;

    @Column(name = "applied_coupon_rule_value", precision = 12, scale = 2)
    BigDecimal appliedCouponRuleValue;

    @Column(name = "shipping_address", length = 500)
    String shippingAddress;

    @Column(name = "recipient_name", length = 50)
    String recipientName;

    @Column(name = "recipient_phone", length = 20)
    String recipientPhone;

    /** 체크아웃 시 주문 확인(확정)·상품 준비 단계를 건너뛰고 배송 중까지 진행 */
    @Column(name = "skip_confirm_and_preparing", nullable = false)
    @Builder.Default
    boolean skipConfirmAndPreparing = false;

    /** 체크아웃 시 배송 중·배송 완료 단계를 건너뛰고 즉시 배송 완료 (위 플래그가 true일 때만 요청 가능) */
    @Column(name = "skip_shipping_and_delivered", nullable = false)
    @Builder.Default
    boolean skipShippingAndDelivered = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<OrderItem> orderItems = new ArrayList<>();

    public static Order create(Long userId, BigDecimal totalAmount, Long userCouponId,
                               String shippingAddress, String recipientName, String recipientPhone,
                               boolean skipConfirmAndPreparing, boolean skipShippingAndDelivered) {
        return Order.builder()
                .userId(userId)
                .orderNumber(generateOrderNumber())
                .totalAmount(totalAmount)
                .finalAmount(totalAmount)
                .userCouponId(userCouponId)
                .shippingAddress(shippingAddress)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .skipConfirmAndPreparing(skipConfirmAndPreparing)
                .skipShippingAndDelivered(skipShippingAndDelivered)
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
        applyDiscount(discountAmount, null, null, null, null);
    }

    /**
     * 할인액 반영 및 적용 쿠폰 메타(주문 상세·관리자 표시용).
     */
    public void applyDiscount(
            BigDecimal discountAmount,
            String couponName,
            String couponCode,
            String couponType,
            BigDecimal couponRuleValue) {
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = discountAmount;
            this.finalAmount = this.totalAmount.subtract(discountAmount);
        }
        if (couponName != null && !couponName.isBlank()) {
            this.appliedCouponName = couponName;
        }
        if (couponCode != null && !couponCode.isBlank()) {
            this.appliedCouponCode = couponCode;
        }
        if (couponType != null && !couponType.isBlank()) {
            this.appliedCouponType = couponType;
        }
        if (couponRuleValue != null) {
            this.appliedCouponRuleValue = couponRuleValue;
        }
    }

    /**
     * 실제 결제 금액이 상품 합계보다 작은데 할인이 아직 0이면, 차액을 할인으로 추론해 반영합니다.
     * Toss 등으로 이미 할인된 금액이 결제되었으나 coupon-used 사가가 지연·실패한 경우 보정용.
     */
    public void reconcileDiscountFromPaidAmountIfUnset(BigDecimal paidAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal total = this.totalAmount;
        if (total == null) {
            return;
        }
        BigDecimal disc = this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO;
        if (disc.compareTo(BigDecimal.ZERO) > 0) {
            return;
        }
        if (paidAmount.compareTo(total) >= 0) {
            return;
        }
        BigDecimal implied = total.subtract(paidAmount);
        if (implied.compareTo(BigDecimal.ZERO) > 0) {
            applyDiscount(implied);
        }
    }

    public void confirm() {
        if (this.status == OrderStatus.PENDING) {
            this.status = OrderStatus.CONFIRMED;
        }
    }

    /**
     * 결제·쿠폰 사가 완료 시 호출: PENDING이면 확정 후, 옵션에 따라 배송 단계를 즉시 진행합니다.
     * coupon-used / payment-completed 처리 순서가 바뀌어도 상태가 덮어씌워지지 않도록 합니다.
     */
    public void markPaidAndApplyFulfillmentFastForward() {
        confirm();
        applyFulfillmentFastForwardFromConfirmed();
    }

    private void applyFulfillmentFastForwardFromConfirmed() {
        if (this.status != OrderStatus.CONFIRMED) {
            return;
        }
        if (this.skipShippingAndDelivered) {
            this.status = OrderStatus.DELIVERED;
        } else if (this.skipConfirmAndPreparing) {
            this.status = OrderStatus.SHIPPING;
        }
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.statusBeforeCancelRequest = null;
    }

    /** 취소 서비스에서 취소·반품 신청 접수 시 진행 상태 표시용 */
    public void markCancelRequested(CancelRequestKind kind) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.CANCEL_REQUESTED) {
            return;
        }
        if (!this.status.allowsInboundCancelRequest(kind)) {
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
