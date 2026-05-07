package com.ecommerce.userservice.entity;

import com.ecommerce.common.entity.BaseEntity;
import com.ecommerce.common.enums.UserRole;
import com.ecommerce.userservice.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_is_active", columnList = "isActive")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@DynamicInsert
@DynamicUpdate
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User extends BaseEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    Long id;

    @Column(nullable = false, unique = true, length = 255)
    String email;

    @Column(nullable = false, length = 255)
    String password;

    @Column(nullable = false, length = 100)
    String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    UserRole role = UserRole.USER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Gender gender;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    public static User create(String email, String encodedPassword, String name, String phoneNumber, Gender gender) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .phoneNumber(phoneNumber)
                .gender(gender)
                .build();
    }

    public void updateProfile(String name, String phoneNumber, Gender gender) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void withdraw() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    public void changeRole(UserRole role) {
        this.role = role;
    }
}
