package com.linkly.healthcheck;

import com.linkly.domain.LinkCheckResult;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkCheckResultRepository extends JpaRepository<LinkCheckResult, Long> {

	/** 북마크별 가장 최근 체크 결과 */
	Optional<LinkCheckResult> findTopByBookmarkIdOrderByCheckedAtDesc(Long bookmarkId);

	/** 여러 북마크의 체크 결과 목록 (최신순) */
	List<LinkCheckResult> findAllByBookmarkIdInOrderByCheckedAtDesc(List<Long> bookmarkIds);
}
