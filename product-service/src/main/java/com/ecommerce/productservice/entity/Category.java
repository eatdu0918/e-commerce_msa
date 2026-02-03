package com.ecommerce.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    Long id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(length = 500)
    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    public static Category create(String name, String description, Category parent, Integer displayOrder) {
        return Category.builder()
                .name(name)
                .description(description)
                .parent(parent)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .build();
    }

    public void update(String name, String description, Category parent, Integer displayOrder) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.displayOrder = displayOrder != null ? displayOrder : this.displayOrder;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
