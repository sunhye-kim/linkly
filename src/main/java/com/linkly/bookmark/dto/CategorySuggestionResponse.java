package com.linkly.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "AI 카테고리 추천 응답")
public class CategorySuggestionResponse {

	@Schema(description = "추천된 카테고리 이름 (null 가능)", example = "개발")
	private String suggestedCategory;
}
