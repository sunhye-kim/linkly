package com.linkly.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "bookmark", uniqueConstraints = {
		@UniqueConstraint(name = "ux_bookmark_user_url", columnNames = {"app_user_id", "url"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Bookmark extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "app_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookmark_user"))
	private AppUser appUser;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "url", nullable = false, length = 500)
	private String url;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_bookmark_category"))
	private Category category;

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
	public void updateInfo(String title, String url, String description) {
		if (title != null && !title.isBlank()) {
			this.title = title;
		}
		if (url != null && !url.isBlank()) {
			this.url = url;
		}
		this.description = description;
	}

	public void changeCategory(Category category) {
		this.category = category;
	}
}
