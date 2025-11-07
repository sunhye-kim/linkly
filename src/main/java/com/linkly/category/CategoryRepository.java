package com.linkly.category;

import com.linkly.domain.AppUser;
import com.linkly.domain.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	/** 사용자의 특정 카테고리 조회 (이름으로) */
	Optional<Category> findByAppUserAndName(AppUser appUser, String name);

	/** 사용자의 카테고리 이름 중복 체크 */
	boolean existsByAppUserAndName(AppUser appUser, String name);

	/** 삭제되지 않은 카테고리 조회 (ID로) */
	Optional<Category> findByIdAndDeletedAtIsNull(Long id);

	/** 사용자의 모든 카테고리 조회 (삭제된 것 포함) */
	List<Category> findAllByAppUser(AppUser appUser);

	/** 사용자의 모든 활성 카테고리 조회 */
	List<Category> findAllByAppUserAndDeletedAtIsNull(AppUser appUser);

	/** 사용자의 특정 카테고리 조회 (이름으로, 활성만) */
	Optional<Category> findByAppUserAndNameAndDeletedAtIsNull(AppUser appUser, String name);

	/** 사용자 ID로 모든 활성 카테고리 조회 */
	List<Category> findAllByAppUser_IdAndDeletedAtIsNull(Long userId);
}
