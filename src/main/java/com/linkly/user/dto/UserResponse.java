package com.linkly.user.dto;

import com.linkly.domain.AppUser;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "회원 정보 응답")
public class UserResponse {

	@Schema(description = "회원 ID", example = "1")
	private Long id;

	@Schema(description = "이메일", example = "user@example.com")
	private String email;

	@Schema(description = "이름", example = "홍길동")
	private String name;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "수정일시")
	private LocalDateTime updatedAt;

	/** Entity를 DTO로 변환 */
	public static UserResponse from(AppUser user) {
		return UserResponse.builder().id(user.getId()).email(user.getEmail()).name(user.getName())
				.createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt()).build();
	}
}
