package com.linkly.healthcheck.dto;

import com.linkly.domain.LinkCheckResult;
import com.linkly.domain.enums.LinkCheckStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinkCheckResultResponse {

	private Long bookmarkId;
	private String bookmarkTitle;
	private String bookmarkUrl;
	private LinkCheckStatus status;
	private Integer httpStatus;
	private Long responseTimeMs;
	private LocalDateTime checkedAt;

	public static LinkCheckResultResponse from(LinkCheckResult result) {
		return LinkCheckResultResponse.builder()
				.bookmarkId(result.getBookmark().getId())
				.bookmarkTitle(result.getBookmark().getTitle())
				.bookmarkUrl(result.getBookmark().getUrl())
				.status(result.getStatus())
				.httpStatus(result.getHttpStatus())
				.responseTimeMs(result.getResponseTimeMs())
				.checkedAt(result.getCheckedAt())
				.build();
	}
}
