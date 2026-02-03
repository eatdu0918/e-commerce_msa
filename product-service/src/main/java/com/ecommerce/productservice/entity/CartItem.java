package com.ecommerce.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "cart_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}),
       indexes = {
           @Index(name = "idx_user_id", columnList = "user_id")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @Column(nullable = false)
    @Builder.Default
    Integer quantity = 1;

    public static CartItem create(Long userId, Product product, Integer quantity) {
        return CartItem.builder()
                .userId(userId)
                .product(product)
                .quantity(quantity)
                .build();
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void increaseQuantity(Integer quantity) {
        this.quantity += quantity;
    }
}
