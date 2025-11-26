package com.linkly.user.dto;

import com.linkly.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 권한 변경 요청 (관리자 전용)")
public class UpdateUserRoleRequest {

	@NotNull(message = "권한은 필수입니다")
	@Schema(description = "변경할 권한", example = "ADMIN", allowableValues = {"USER", "ADMIN"})
	private UserRole role;
}
