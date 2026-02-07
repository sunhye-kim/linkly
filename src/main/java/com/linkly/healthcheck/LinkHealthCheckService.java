package com.linkly.healthcheck;

import com.linkly.healthcheck.dto.LinkCheckResultResponse;
import java.util.List;

public interface LinkHealthCheckService {

	/** 스케줄러 호출용: 전체 활성 북마크 헬스체크 */
	void checkAllBookmarks();

	/** 특정 북마크 즉시 체크 */
	LinkCheckResultResponse checkBookmarkNow(Long bookmarkId, Long userId);

	/** 내 북마크 최신 헬스체크 결과 목록 */
	List<LinkCheckResultResponse> getMyResults(Long userId);
}
