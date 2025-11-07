package com.linkly.bookmark;

import com.linkly.domain.AppUser;
import com.linkly.domain.Bookmark;
import com.linkly.domain.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
