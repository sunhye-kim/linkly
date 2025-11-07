package com.linkly.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "북마크 수정 요청")
public class UpdateBookmarkRequest {

    @URL(message = "유효한 URL 형식이어야 합니다")
    @Size(max = 500, message = "URL은 500자 이하여야 합니다")
    @Schema(description = "북마크 URL (선택)", example = "https://example.com")
    private String url;

    @Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하여야 합니다")
    @Schema(description = "북마크 제목 (선택)", example = "수정된 제목")
    private String title;

    @Schema(description = "북마크 설명 (선택)", example = "수정된 설명")
    private String description;

    @Schema(description = "카테고리 ID (선택, null이면 카테고리 없음)", example = "2")
    private Long categoryId;

    @Schema(description = "태그 목록 (선택, 전체 교체됨)", example = "[\"Java\", \"Spring Boot\"]")
    private List<String> tags;
}