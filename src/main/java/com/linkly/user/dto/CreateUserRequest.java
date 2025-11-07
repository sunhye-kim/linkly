package com.linkly.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 가입 요청")
public class CreateUserRequest {

	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "유효한 이메일 형식이어야 합니다")
	@Schema(description = "이메일", example = "user@example.com")
	private String email;

	@NotBlank(message = "비밀번호는 필수입니다")
	@Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
	@Schema(description = "비밀번호", example = "password123")
	private String password;

	@NotBlank(message = "이름은 필수입니다")
	@Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하여야 합니다")
	@Schema(description = "이름", example = "홍길동")
	private String name;
}
