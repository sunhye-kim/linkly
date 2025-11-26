package com.linkly.user.dto;

import com.linkly.domain.AppUser;
import com.linkly.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@Schema(description = "회원 정보 응답")
public class UserResponse {

	@Schema(description = "회원 ID", example = "1")
	private final Long id;

	@Schema(description = "이메일", example = "user@example.com")
	private final String email;

	@Schema(description = "이름", example = "홍길동")
	private final String name;

	@Schema(description = "권한", example = "USER", allowableValues = {"USER", "ADMIN"})
	private final UserRole role;

	@Schema(description = "생성일시")
	private final LocalDateTime createdAt;

	@Schema(description = "수정일시")
	private final LocalDateTime updatedAt;

	public UserResponse(Long id, String email, String name, UserRole role, LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		this.id = id;
		this.email = email;
		this.name = name;
		this.role = role;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/** Entity를 DTO로 변환 */
	public static UserResponse from(AppUser user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole(), user.getCreatedAt(),
				user.getUpdatedAt());
	}
}
