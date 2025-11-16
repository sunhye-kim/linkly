package com.linkly.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "북마크 생성 요청")
public class CreateBookmarkRequest {

	@NotBlank(message = "URL은 필수입니다")
	@URL(message = "유효한 URL 형식이어야 합니다")
	@Size(max = 500, message = "URL은 500자 이하여야 합니다")
	@Schema(description = "북마크 URL", example = "https://example.com")
	private String url;

	@NotBlank(message = "제목은 필수입니다")
	@Size(min = 1, max = 255, message = "제목은 1자 이상 255자 이하여야 합니다")
	@Schema(description = "북마크 제목", example = "유용한 개발 자료")
	private String title;

	@Schema(description = "북마크 설명", example = "Spring Boot 관련 튜토리얼")
	private String description;

	@Schema(description = "카테고리 ID (선택)", example = "1")
	private Long categoryId;

	@Schema(description = "태그 목록 (선택)", example = "[\"Java\", \"Spring\", \"Backend\"]")
	private List<String> tags;
}
