package com.linkly.bookmark;

import com.linkly.bookmark.dto.CategorySuggestionResponse;
import java.util.List;

/** Ollama 기반 카테고리 추천 서비스 인터페이스 */
public interface OllamaService {

	/**
	 * 제목·설명과 기존 카테고리 목록을 기반으로 가장 적합한 카테고리를 추천한다.
	 *
	 * @param title
	 *            북마크 제목
	 * @param description
	 *            북마크 설명
	 * @param existingCategories
	 *            사용자의 기존 카테고리 이름 목록
	 * @return 추천된 카테고리 (실패 시 suggestedCategory가 null)
	 */
	CategorySuggestionResponse suggestCategory(String title, String description, List<String> existingCategories);
}
