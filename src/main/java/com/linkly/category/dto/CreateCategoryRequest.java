package com.linkly.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카테고리 생성 요청")
public class CreateCategoryRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    @Schema(description = "사용자 ID (인증 도입 후 제거 예정)", example = "1")
    private Long userId;

    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(min = 1, max = 50, message = "카테고리 이름은 1자 이상 50자 이하여야 합니다")
    @Schema(description = "카테고리 이름", example = "개발")
    private String name;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    @Schema(description = "카테고리 설명", example = "개발 관련 북마크")
    private String description;
}