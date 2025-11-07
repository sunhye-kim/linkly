package com.linkly.user;

import com.linkly.domain.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	/** 이메일로 사용자 조회 */
	Optional<AppUser> findByEmail(String email);

	/** 이메일로 사용자 존재 여부 확인 */
	boolean existsByEmail(String email);

	/** 삭제되지 않은 사용자 조회 (소프트 삭제 고려) */
	Optional<AppUser> findByIdAndDeletedAtIsNull(Long id);

	/** 삭제되지 않은 사용자를 이메일로 조회 */
	Optional<AppUser> findByEmailAndDeletedAtIsNull(String email);
}
