package com.linkly.category.dto;

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
@Schema(description = "카테고리 수정 요청")
public class UpdateCategoryRequest {

    @Size(min = 1, max = 50, message = "카테고리 이름은 1자 이상 50자 이하여야 합니다")
    @Schema(description = "카테고리 이름 (선택)", example = "프로그래밍")
    private String name;

    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    @Schema(description = "카테고리 설명 (선택)", example = "프로그래밍 관련 북마크")
    private String description;
}