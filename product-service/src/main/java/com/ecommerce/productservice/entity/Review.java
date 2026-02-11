package com.ecommerce.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    Long id;

    @Column(name = "product_id", nullable = false)
    Long productId;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "user_name", nullable = false, length = 100)
    String userName;

    @Column(nullable = false)
    Integer score;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(name = "image_url", length = 500)
    String imageUrl;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    public static Review create(Long productId, Long userId, String userName, Integer score, String content, String imageUrl) {
        return Review.builder()
                .productId(productId)
                .userId(userId)
                .userName(userName)
                .score(score)
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

    public void update(Integer score, String content, String imageUrl) {
        this.score = score;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public void delete() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }
}
