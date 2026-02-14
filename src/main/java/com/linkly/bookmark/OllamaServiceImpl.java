package com.linkly.bookmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkly.bookmark.dto.CategorySuggestionResponse;
import com.linkly.global.config.OllamaConfig;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class OllamaServiceImpl implements OllamaService {

	private final RestTemplate ollamaRestTemplate;
	private final OllamaConfig ollamaConfig;
	private final ObjectMapper objectMapper;

	public OllamaServiceImpl(@Qualifier("ollamaRestTemplate") RestTemplate ollamaRestTemplate,
			OllamaConfig ollamaConfig, ObjectMapper objectMapper) {
		this.ollamaRestTemplate = ollamaRestTemplate;
		this.ollamaConfig = ollamaConfig;
		this.objectMapper = objectMapper;
	}

	@Override
	public CategorySuggestionResponse suggestCategory(String title, String description,
			List<String> existingCategories) {
		try {
			String prompt = buildPrompt(title, description, existingCategories);
			String url = ollamaConfig.getBaseUrl() + "/api/generate";

			Map<String, Object> requestBody = Map.of(
					"model", ollamaConfig.getModel(),
					"prompt", prompt,
					"stream", false);

			ResponseEntity<String> response = ollamaRestTemplate.postForEntity(url, requestBody, String.class);

			String rawResponse = extractResponse(response.getBody());
			String suggested = validateCategory(rawResponse, existingCategories);

			log.info("Ollama 카테고리 추천 결과: title={}, suggested={}", title, suggested);
			return CategorySuggestionResponse.builder().suggestedCategory(suggested).build();
		} catch (Exception e) {
			log.warn("Ollama 카테고리 추천 실패: {}", e.getMessage());
			return CategorySuggestionResponse.builder().suggestedCategory(null).build();
		}
	}

	private String buildPrompt(String title, String description, List<String> categories) {
		return String.format(
				"You are a bookmark categorization assistant. "
						+ "Given the following bookmark information, choose the most appropriate category from the list. "
						+ "Reply with ONLY the category name, nothing else.\n\n"
						+ "Title: %s\n"
						+ "Description: %s\n\n"
						+ "Available categories: %s\n\n"
						+ "Best matching category:",
				title,
				description != null ? description : "",
				String.join(", ", categories));
	}

	private String extractResponse(String responseBody) throws Exception {
		JsonNode node = objectMapper.readTree(responseBody);
		return node.get("response").asText().trim();
	}

	private String validateCategory(String rawResponse, List<String> existingCategories) {
		for (String category : existingCategories) {
			if (rawResponse.equalsIgnoreCase(category) || rawResponse.contains(category)) {
				return category;
			}
		}
		return null;
	}
}
