package com.linkly.tag;

import com.linkly.domain.AppUser;
import com.linkly.domain.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

	/** 사용자별 모든 태그 조회 */
	List<Tag> findAllByAppUser(AppUser appUser);

	/** 사용자별 삭제되지 않은 태그 조회 */
	List<Tag> findAllByAppUserAndDeletedAtIsNull(AppUser appUser);

	/** 사용자와 태그명으로 조회 */
	Optional<Tag> findByAppUserAndName(AppUser appUser, String name);

	/** 사용자와 태그명으로 존재 여부 확인 */
	boolean existsByAppUserAndName(AppUser appUser, String name);

	/** 삭제되지 않은 태그 조회 */
	Optional<Tag> findByIdAndDeletedAtIsNull(Long id);

	/** 사용자 ID로 태그 조회 */
	List<Tag> findAllByAppUserId(Long userId);

	/** 사용자 ID와 삭제되지 않은 태그 조회 */
	List<Tag> findAllByAppUserIdAndDeletedAtIsNull(Long userId);
}
