package com.linkly.bookmark;

import com.linkly.domain.AppUser;
import com.linkly.domain.Bookmark;
import com.linkly.domain.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	/** 사용자별 모든 북마크 조회 */
	List<Bookmark> findAllByAppUser(AppUser appUser);

	/** 사용자별 삭제되지 않은 북마크 조회 */
	List<Bookmark> findAllByAppUserAndDeletedAtIsNull(AppUser appUser);

	/** 카테고리별 북마크 조회 */
	List<Bookmark> findAllByCategory(Category category);

	/** 사용자와 카테고리로 북마크 조회 */
	List<Bookmark> findAllByAppUserAndCategory(AppUser appUser, Category category);

	/** 사용자와 URL로 북마크 조회 (중복 체크용) */
	Optional<Bookmark> findByAppUserAndUrl(AppUser appUser, String url);

	/** 삭제되지 않은 북마크 조회 */
	Optional<Bookmark> findByIdAndDeletedAtIsNull(Long id);

	/** 사용자별, URL별 북마크 존재 여부 확인 */
	boolean existsByAppUserAndUrl(AppUser appUser, String url);

	/** 사용자 ID로 북마크 조회 */
	List<Bookmark> findAllByAppUserId(Long userId);

	/** 사용자 ID와 삭제되지 않은 북마크 조회 */
	List<Bookmark> findAllByAppUserIdAndDeletedAtIsNull(Long userId);

	/** 삭제되지 않은 전체 북마크 조회 (헬스체크 스케줄러용) */
	List<Bookmark> findAllByDeletedAtIsNull();

	/** 키워드 + 선택적 카테고리 검색 (제목·URL·설명·태그명 대상) */
	@Query("SELECT DISTINCT b FROM Bookmark b " +
		   "LEFT JOIN BookmarkTagMap btm ON btm.bookmark = b " +
		   "LEFT JOIN btm.tag t " +
		   "WHERE b.appUser.id = :userId " +
		   "AND b.deletedAt IS NULL " +
		   "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
		   "AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		   "  OR LOWER(b.url) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		   "  OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		   "  OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	List<Bookmark> searchBookmarks(@Param("userId") Long userId,
								   @Param("keyword") String keyword,
								   @Param("categoryId") Long categoryId);
}
