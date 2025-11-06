package com.linkly.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AppUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 비즈니스 로직 메서드
    public void updateInfo(String password, String name) {
        if (password != null && !password.isBlank()) {
            this.password = password;
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    // 소프트 삭제 관련 메서드
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public void restore() {
        this.deletedAt = null;
    }
}