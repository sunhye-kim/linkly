package com.linkly.healthcheck;

import com.linkly.bookmark.BookmarkRepository;
import com.linkly.domain.Bookmark;
import com.linkly.domain.LinkCheckResult;
import com.linkly.domain.enums.LinkCheckStatus;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.healthcheck.dto.LinkCheckResultResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LinkHealthCheckServiceImpl implements LinkHealthCheckService {

	private final BookmarkRepository bookmarkRepository;
	private final LinkCheckResultRepository linkCheckResultRepository;
	private final LinkHealthChecker linkHealthChecker;
	private final RestTemplate restTemplate;

	@Override
	public void checkAllBookmarks() {
		List<Bookmark> bookmarks = bookmarkRepository.findAllByDeletedAtIsNull();
		log.info("[HealthCheck] Scheduling async check for {} bookmarks", bookmarks.size());
		bookmarks.forEach(linkHealthChecker::checkAndSave);
	}

	@Override
	@Transactional
	public LinkCheckResultResponse checkBookmarkNow(Long bookmarkId, Long userId) {
		Bookmark bookmark = bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)
				.orElseThrow(() -> new ResourceNotFoundException("Bookmark", bookmarkId));

		if (!bookmark.getAppUser().getId().equals(userId)) {
			throw new InvalidRequestException("No permission for this bookmark", "bookmarkId=" + bookmarkId);
		}

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
		} catch (Exception e) {
			responseTimeMs = System.currentTimeMillis() - start;
			status = LinkCheckStatus.DEAD;
		}

		LinkCheckResult result = LinkCheckResult.of(bookmark, status, httpStatus, responseTimeMs);
		linkCheckResultRepository.save(result);
		log.info("[HealthCheck] Immediate check — bookmarkId={}, status={}", bookmarkId, status);
		return LinkCheckResultResponse.from(result);
	}

	@Override
	public List<LinkCheckResultResponse> getMyResults(Long userId) {
		List<Bookmark> bookmarks = bookmarkRepository.findAllByAppUserIdAndDeletedAtIsNull(userId);
		List<Long> bookmarkIds = bookmarks.stream().map(Bookmark::getId).toList();

		return bookmarkIds.stream()
				.map(linkCheckResultRepository::findTopByBookmarkIdOrderByCheckedAtDesc)
				.filter(Optional::isPresent)
				.map(opt -> LinkCheckResultResponse.from(opt.get()))
				.collect(Collectors.toList());
	}
}
