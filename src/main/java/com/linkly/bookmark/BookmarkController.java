package com.linkly.bookmark;

import com.linkly.bookmark.dto.BookmarkResponse;
import com.linkly.bookmark.dto.CreateBookmarkRequest;
import com.linkly.bookmark.dto.UpdateBookmarkRequest;
import com.linkly.global.dto.ApiResponse;
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
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
@Tag(name = "Bookmark", description = "북마크 관리 API")
public class BookmarkController {

	private final BookmarkService bookmarkService;

	@PostMapping
	@Operation(summary = "북마크 생성", description = "새로운 북마크를 생성합니다. 태그를 함께 등록할 수 있으며, 같은 URL은 중복 저장할 수 없습니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "북마크 생성 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (URL 중복, 유효성 검증 실패 등)"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 또는 카테고리를 찾을 수 없음")})
	public ResponseEntity<ApiResponse<BookmarkResponse>> createBookmark(
			@Valid @RequestBody CreateBookmarkRequest request) {
		log.info("POST /api/bookmarks - 북마크 생성 요청: userId={}, url={}", request.getUserId(), request.getUrl());

		BookmarkResponse response = bookmarkService.createBookmark(request.getUserId(), request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@GetMapping("/{bookmarkId}")
	@Operation(summary = "북마크 조회", description = "북마크 ID로 북마크 정보를 조회합니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")})
	public ResponseEntity<ApiResponse<BookmarkResponse>> getBookmarkById(
			@Parameter(description = "북마크 ID", example = "1") @PathVariable Long bookmarkId) {
		log.info("GET /api/bookmarks/{} - 북마크 조회", bookmarkId);

		BookmarkResponse response = bookmarkService.getBookmarkById(bookmarkId);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	@Operation(summary = "사용자의 북마크 목록 조회", description = "특정 사용자의 모든 북마크를 조회합니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")})
	public ResponseEntity<ApiResponse<List<BookmarkResponse>>> getBookmarksByUserId(
			@Parameter(description = "사용자 ID (인증 도입 후 제거 예정)", example = "1", required = true) @RequestParam Long userId) {
		log.info("GET /api/bookmarks?userId={} - 북마크 목록 조회", userId);

		List<BookmarkResponse> responses = bookmarkService.getBookmarksByUserId(userId);

		return ResponseEntity.ok(ApiResponse.success(responses));
	}

	@PutMapping("/{bookmarkId}")
	@Operation(summary = "북마크 수정", description = "북마크의 정보를 수정합니다. 태그도 함께 수정할 수 있으며, 태그는 전체 교체됩니다. 북마크 소유자만 수정할 수 있습니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (권한 없음, URL 중복 등)"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")})
	public ResponseEntity<ApiResponse<BookmarkResponse>> updateBookmark(
			@Parameter(description = "북마크 ID", example = "1") @PathVariable Long bookmarkId,
			@Parameter(description = "사용자 ID (권한 체크용, 인증 도입 후 제거 예정)", example = "1", required = true) @RequestParam Long userId,
			@Valid @RequestBody UpdateBookmarkRequest request) {
		log.info("PUT /api/bookmarks/{}?userId={} - 북마크 수정", bookmarkId, userId);

		BookmarkResponse response = bookmarkService.updateBookmark(bookmarkId, userId, request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/{bookmarkId}")
	@Operation(summary = "북마크 삭제", description = "북마크를 삭제합니다. (Soft Delete - 실제로는 삭제되지 않고 삭제 시간만 기록됩니다) 북마크 소유자만 삭제할 수 있습니다.")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (권한 없음 등)"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "북마크를 찾을 수 없음")})
	public ResponseEntity<Void> deleteBookmark(
			@Parameter(description = "북마크 ID", example = "1") @PathVariable Long bookmarkId,
			@Parameter(description = "사용자 ID (권한 체크용, 인증 도입 후 제거 예정)", example = "1", required = true) @RequestParam Long userId) {
		log.info("DELETE /api/bookmarks/{}?userId={} - 북마크 삭제", bookmarkId, userId);

		bookmarkService.deleteBookmark(bookmarkId, userId);

		return ResponseEntity.noContent().build();
	}
}
