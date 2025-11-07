package com.linkly.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "tag", uniqueConstraints = {
		@UniqueConstraint(name = "ux_user_tag_name", columnNames = {"app_user_id", "name"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tag extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "app_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tag_user"))
	private AppUser appUser;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

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

	// 비즈니스 메서드
	public void updateName(String name) {
		if (name != null && !name.isBlank()) {
			this.name = name;
		}
	}
}
