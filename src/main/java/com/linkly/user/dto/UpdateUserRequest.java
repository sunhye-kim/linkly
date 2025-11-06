package com.linkly.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 수정 요청")
public class UpdateUserRequest {

    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Schema(description = "비밀번호 (선택)", example = "newpassword123")
    private String password;

    @Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하여야 합니다")
    @Schema(description = "이름 (선택)", example = "김철수")
    private String name;
}