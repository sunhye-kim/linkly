package com.linkly.healthcheck;

import com.linkly.global.dto.ApiResponse;
import com.linkly.global.security.SecurityUtils;
import com.linkly.healthcheck.dto.LinkCheckResultResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/link-health")
@RequiredArgsConstructor
public class LinkHealthCheckController {

	private final LinkHealthCheckService linkHealthCheckService;

	/** 내 북마크 전체 최신 헬스 상태 목록 조회 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<LinkCheckResultResponse>>> getMyResults() {
		Long userId = SecurityUtils.getCurrentUserId();
		List<LinkCheckResultResponse> results = linkHealthCheckService.getMyResults(userId);
		return ResponseEntity.ok(ApiResponse.success(results));
	}

	/** 특정 북마크 즉시 헬스체크 */
	@PostMapping("/{bookmarkId}/check")
	public ResponseEntity<ApiResponse<LinkCheckResultResponse>> checkNow(@PathVariable Long bookmarkId) {
		Long userId = SecurityUtils.getCurrentUserId();
		LinkCheckResultResponse result = linkHealthCheckService.checkBookmarkNow(bookmarkId, userId);
		return ResponseEntity.ok(ApiResponse.success(result));
	}
}
