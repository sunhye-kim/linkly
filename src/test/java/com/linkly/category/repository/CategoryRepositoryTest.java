package com.linkly.category.repository;

import com.linkly.category.CategoryRepository;
import com.linkly.domain.AppUser;
import com.linkly.domain.Category;
import com.linkly.global.config.JpaAuditingConfig;
import com.linkly.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("CategoryRepository 테스트")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        testUser = AppUser.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트 사용자")
                .build();
        testUser = userRepository.save(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("카테고리 저장 후 조회")
    void saveAndFindCategory() {
        // given
        Category category = Category.builder()
                .appUser(testUser)
                .name("개발")
                .description("개발 관련 북마크")
                .build();

        // when
        Category savedCategory = categoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("개발");
        assertThat(savedCategory.getAppUser().getId()).isEqualTo(testUser.getId());
        assertThat(savedCategory.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자와 이름으로 카테고리 조회")
    void findByAppUserAndName() {
        // given
        Category category = Category.builder()
                .appUser(testUser)
                .name("디자인")
                .description("디자인 자료")
                .build();
        categoryRepository.save(category);
        entityManager.flush();

        // when
        Optional<Category> found = categoryRepository.findByAppUserAndName(testUser, "디자인");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("디자인");
    }

    @Test
    @DisplayName("사용자의 카테고리 이름 중복 체크")
    void existsByAppUserAndName() {
        // given
        Category category = Category.builder()
                .appUser(testUser)
                .name("프로그래밍")
                .build();
        categoryRepository.save(category);
        entityManager.flush();

        // when & then
        assertThat(categoryRepository.existsByAppUserAndName(testUser, "프로그래밍")).isTrue();
        assertThat(categoryRepository.existsByAppUserAndName(testUser, "존재하지않음")).isFalse();
    }

    @Test
    @DisplayName("사용자의 모든 활성 카테고리 조회")
    void findAllByAppUserAndDeletedAtIsNull() {
        // given
        Category category1 = Category.builder()
                .appUser(testUser)
                .name("카테고리1")
                .build();

        Category category2 = Category.builder()
                .appUser(testUser)
                .name("카테고리2")
                .build();

        Category deletedCategory = Category.builder()
                .appUser(testUser)
                .name("삭제된카테고리")
                .build();
        deletedCategory.softDelete();

        categoryRepository.save(category1);
        categoryRepository.save(category2);
        categoryRepository.save(deletedCategory);
        entityManager.flush();

        // when
        List<Category> categories = categoryRepository.findAllByAppUserAndDeletedAtIsNull(testUser);

        // then
        assertThat(categories).hasSize(2);
        assertThat(categories).extracting("name")
                .containsExactlyInAnyOrder("카테고리1", "카테고리2");
    }

    @Test
    @DisplayName("사용자 ID로 활성 카테고리 조회")
    void findAllByAppUser_IdAndDeletedAtIsNull() {
        // given
        Category category = Category.builder()
                .appUser(testUser)
                .name("테스트")
                .build();
        categoryRepository.save(category);
        entityManager.flush();

        // when
        List<Category> categories = categoryRepository.findAllByAppUser_IdAndDeletedAtIsNull(testUser.getId());

        // then
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).getName()).isEqualTo("테스트");
    }

    @Test
    @DisplayName("다른 사용자는 같은 이름의 카테고리를 만들 수 있음")
    void differentUsersSameCategory() {
        // given
        AppUser anotherUser = AppUser.builder()
                .email("another@example.com")
                .password("password123")
                .name("다른 사용자")
                .build();
        userRepository.save(anotherUser);

        Category category1 = Category.builder()
                .appUser(testUser)
                .name("개발")
                .build();

        Category category2 = Category.builder()
                .appUser(anotherUser)
                .name("개발")
                .build();

        // when
        categoryRepository.save(category1);
        categoryRepository.save(category2);
        entityManager.flush();

        // then
        assertThat(categoryRepository.existsByAppUserAndName(testUser, "개발")).isTrue();
        assertThat(categoryRepository.existsByAppUserAndName(anotherUser, "개발")).isTrue();
    }

    @Test
    @DisplayName("카테고리 정보 수정 (Dirty Checking)")
    void updateCategory() {
        // given
        Category category = Category.builder()
                .appUser(testUser)
                .name("수정 전")
                .description("이전 설명")
                .build();
        categoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        // when
        Category foundCategory = categoryRepository.findById(category.getId()).orElseThrow();
        foundCategory.updateInfo("수정 후", "새로운 설명");
        entityManager.flush();
        entityManager.clear();

        // then
        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(updatedCategory.getName()).isEqualTo("수정 후");
        assertThat(updatedCategory.getDescription()).isEqualTo("새로운 설명");
    }

    @Test
    @DisplayName("Soft Delete 테스트")
    void softDelete() {
        // given
        Category category = Category.builder()
                .appUser(testUser)
                .name("삭제 테스트")
                .build();
        categoryRepository.save(category);
        entityManager.flush();

        // when - 삭제 전
        Optional<Category> beforeDelete = categoryRepository.findByIdAndDeletedAtIsNull(category.getId());
        assertThat(beforeDelete).isPresent();

        // when - 삭제
        category.softDelete();
        categoryRepository.save(category);
        entityManager.flush();

        // then - 삭제 후
        Optional<Category> afterDelete = categoryRepository.findByIdAndDeletedAtIsNull(category.getId());
        assertThat(afterDelete).isEmpty();

        // 물리적으로는 존재
        Optional<Category> physicallyExists = categoryRepository.findById(category.getId());
        assertThat(physicallyExists).isPresent();
        assertThat(physicallyExists.get().isDeleted()).isTrue();
    }
}