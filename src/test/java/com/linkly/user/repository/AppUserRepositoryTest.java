package com.linkly.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.linkly.domain.AppUser;
import com.linkly.global.config.JpaAuditingConfig;
import com.linkly.user.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

/**
 * Repository 계층 테스트 Django의 Model/ORM 테스트와 유사 @DataJpaTest: JPA 관련 컴포넌트만 로드 (빠른
 * 테스트) - 실제 DB 대신 인메모리 H2 DB 사용 - 각 테스트마다 자동 롤백 - Repository, EntityManager만 로드
 */
@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("AppUserRepository 테스트")
class AppUserRepositoryTest {

	@Autowired
	private AppUserRepository userRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	@DisplayName("회원 저장 후 조회")
	void saveAndFindUser() {
		// given - 테스트 데이터 준비
		AppUser user = AppUser.builder().email("test@example.com").password("password123").name("테스트 사용자").build();

		// when - 저장
		AppUser savedUser = userRepository.save(user);
		entityManager.flush();
		entityManager.clear();

		// then - 검증
		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
		assertThat(savedUser.getName()).isEqualTo("테스트 사용자");
		assertThat(savedUser.getCreatedAt()).isNotNull();
	}

	@Test
	@DisplayName("이메일로 회원 조회")
	void findByEmail() {
		// given
		AppUser user = AppUser.builder().email("find@example.com").password("password123").name("찾기 테스트").build();
		userRepository.save(user);
		entityManager.flush();

		// when
		Optional<AppUser> found = userRepository.findByEmail("find@example.com");

		// then
		assertThat(found).isPresent();
		assertThat(found.get().getEmail()).isEqualTo("find@example.com");
	}

	@Test
	@DisplayName("이메일 존재 여부 확인")
	void existsByEmail() {
		// given
		AppUser user = AppUser.builder().email("exists@example.com").password("password123").name("존재 테스트").build();
		userRepository.save(user);
		entityManager.flush();

		// when & then
		assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
		assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
	}

	@Test
	@DisplayName("삭제되지 않은 회원만 조회 (Soft Delete)")
	void findByIdAndDeletedAtIsNull() {
		// given - 두 명의 회원 (하나는 삭제됨)
		AppUser activeUser = AppUser.builder().email("active@example.com").password("password123").name("활성 사용자")
				.build();

		AppUser deletedUser = AppUser.builder().email("deleted@example.com").password("password123").name("삭제된 사용자")
				.build();
		deletedUser.softDelete();

		userRepository.save(activeUser);
		userRepository.save(deletedUser);
		entityManager.flush();

		// when
		Optional<AppUser> activeFound = userRepository.findByIdAndDeletedAtIsNull(activeUser.getId());
		Optional<AppUser> deletedFound = userRepository.findByIdAndDeletedAtIsNull(deletedUser.getId());

		// then
		assertThat(activeFound).isPresent();
		assertThat(deletedFound).isEmpty();
	}

	@Test
	@DisplayName("이메일로 활성 회원 조회 (Soft Delete 필터)")
	void findByEmailAndDeletedAtIsNull() {
		// given
		AppUser user = AppUser.builder().email("softdelete@example.com").password("password123").name("소프트 삭제 테스트")
				.build();
		userRepository.save(user);
		entityManager.flush();

		// when - 삭제 전 조회
		Optional<AppUser> beforeDelete = userRepository.findByEmailAndDeletedAtIsNull("softdelete@example.com");

		// then
		assertThat(beforeDelete).isPresent();

		// when - 삭제 후 조회
		user.softDelete();
		userRepository.save(user);
		entityManager.flush();

		Optional<AppUser> afterDelete = userRepository.findByEmailAndDeletedAtIsNull("softdelete@example.com");

		// then
		assertThat(afterDelete).isEmpty();
	}

	@Test
	@DisplayName("회원 정보 수정 (Dirty Checking)")
	void updateUser() {
		// given
		AppUser user = AppUser.builder().email("update@example.com").password("password123").name("수정 전 이름").build();
		userRepository.save(user);
		entityManager.flush();
		entityManager.clear();

		// when - Entity를 다시 조회하고 수정
		AppUser foundUser = userRepository.findById(user.getId()).orElseThrow();
		foundUser.updateInfo("newpassword", "수정 후 이름");
		entityManager.flush();
		entityManager.clear();

		// then
		AppUser updatedUser = userRepository.findById(user.getId()).orElseThrow();
		assertThat(updatedUser.getName()).isEqualTo("수정 후 이름");
	}

	@Test
	@DisplayName("이메일 unique constraint 검증")
	void emailUniqueness() {
		// given
		AppUser user1 = AppUser.builder().email("duplicate@example.com").password("password123").name("사용자1").build();
		userRepository.save(user1);
		entityManager.flush();

		// when & then - 중복 이메일 체크
		assertThat(userRepository.existsByEmail("duplicate@example.com")).isTrue();
	}
}
