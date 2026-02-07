package com.linkly.healthcheck;

import com.linkly.domain.Bookmark;
import com.linkly.domain.LinkCheckResult;
import com.linkly.domain.enums.LinkCheckStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * 개별 북마크 URL에 대한 비동기 헬스체크를 담당하는 컴포넌트.
 * @Async 가 같은 클래스 내 자기호출(self-invocation)에서 동작하지 않는 Spring AOP 제약을
 * 피하기 위해 별도 컴포넌트로 분리함.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LinkHealthChecker {

	private final RestTemplate restTemplate;
	private final LinkCheckResultRepository linkCheckResultRepository;

	@Async("linkHealthCheckExecutor")
	@Transactional
	public void checkAndSave(Bookmark bookmark) {
		LinkCheckStatus status;
		Integer httpStatus = null;
		long start = System.currentTimeMillis();
		long responseTimeMs;

		try {
			ResponseEntity<Void> response = restTemplate.exchange(
					bookmark.getUrl(), HttpMethod.HEAD, null, Void.class);
			responseTimeMs = System.currentTimeMillis() - start;
			httpStatus = response.getStatusCode().value();
			status = response.getStatusCode().is2xxSuccessful()
					? LinkCheckStatus.HEALTHY
					: LinkCheckStatus.DEAD;
		} catch (ResourceAccessException e) {
			responseTimeMs = System.currentTimeMillis() - start;
			status = LinkCheckStatus.TIMEOUT;
			log.warn("[HealthCheck] Timeout — bookmarkId={}, url={}", bookmark.getId(), bookmark.getUrl());
		} catch (Exception e) {
			responseTimeMs = System.currentTimeMillis() - start;
			status = LinkCheckStatus.DEAD;
			log.warn("[HealthCheck] Error — bookmarkId={}, error={}", bookmark.getId(), e.getMessage());
		}

		LinkCheckResult result = LinkCheckResult.of(bookmark, status, httpStatus, responseTimeMs);
		linkCheckResultRepository.save(result);
		log.debug("[HealthCheck] bookmarkId={} → {}", bookmark.getId(), status);
	}
}
