package com.ecommerce.productservice.entity;

import com.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
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

    @Column(name = "name_ko", length = 100)
    String nameKo;

    @Column(length = 500)
    String description;

    @Column(name = "description_ko", length = 500)
    String descriptionKo;

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

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    public static Category create(String name, String description, Category parent, Integer displayOrder) {
        return create(name, description, parent, displayOrder, null, null);
    }

    public static Category create(String name, String description, Category parent, Integer displayOrder,
            String nameKo, String descriptionKo) {
        return Category.builder()
                .name(name)
                .description(description)
                .nameKo(nameKo)
                .descriptionKo(descriptionKo)
                .parent(parent)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .build();
    }

    public void update(String name, String description, Category parent, Integer displayOrder) {
        update(name, description, parent, displayOrder, null, null);
    }

    public void update(String name, String description, Category parent, Integer displayOrder, String nameKo,
            String descriptionKo) {
        this.name = name;
        this.description = description;
        this.nameKo = nameKo;
        this.descriptionKo = descriptionKo;
        this.parent = parent;
        this.displayOrder = displayOrder != null ? displayOrder : this.displayOrder;
    }

    public void deactivate() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    public void activate() {
        this.isActive = true;
        this.deletedAt = null;
    }
}
