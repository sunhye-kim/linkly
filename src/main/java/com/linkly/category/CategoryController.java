package com.linkly.category;

import com.linkly.category.dto.CategoryResponse;
import com.linkly.category.dto.CreateCategoryRequest;
import com.linkly.category.dto.UpdateCategoryRequest;
import com.linkly.global.dto.ApiResponse;
import com.linkly.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "카테고리 관리 API")
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	@Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다. 같은 사용자는 중복된 이름의 카테고리를 만들 수 없습니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (이름 중복, 유효성 검증 실패 등)"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")})
	public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
			@Valid @RequestBody CreateCategoryRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("POST /categories - 카테고리 생성 요청: userId={}, name={}", userId, request.getName());

		CategoryResponse response = categoryService.createCategory(userId, request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@GetMapping("/{categoryId}")
	@Operation(summary = "카테고리 조회", description = "카테고리 ID로 카테고리 정보를 조회합니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")})
	public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
			@Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId) {
		log.info("GET /categories/{} - 카테고리 조회", categoryId);

		CategoryResponse response = categoryService.getCategoryById(categoryId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	@Operation(summary = "사용자의 카테고리 목록 조회", description = "인증된 사용자의 모든 카테고리를 조회합니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoriesByUserId() {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("GET /categories - 카테고리 목록 조회: userId={}", userId);

		List<CategoryResponse> responses = categoryService.getCategoriesByUserId(userId);

		return ResponseEntity.ok(ApiResponse.success(responses));
	}

	@PutMapping("/{categoryId}")
	@Operation(summary = "카테고리 수정", description = "카테고리의 이름 또는 설명을 수정합니다. 카테고리 소유자만 수정할 수 있습니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (권한 없음, 이름 중복 등)"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
	public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
			@Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId,
			@Valid @RequestBody UpdateCategoryRequest request) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("PUT /categories/{} - 카테고리 수정: userId={}", categoryId, userId);

		CategoryResponse response = categoryService.updateCategory(categoryId, userId, request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/{categoryId}")
	@Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다. (Soft Delete - 실제로는 삭제되지 않고 삭제 시간만 기록됩니다) 카테고리 소유자만 삭제할 수 있습니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (권한 없음 등)"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")})
	public ResponseEntity<Void> deleteCategory(
			@Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId) {
		Long userId = SecurityUtils.getCurrentUserId();
		log.info("DELETE /categories/{} - 카테고리 삭제: userId={}", categoryId, userId);

		categoryService.deleteCategory(categoryId, userId);

		return ResponseEntity.noContent().build();
	}
}
