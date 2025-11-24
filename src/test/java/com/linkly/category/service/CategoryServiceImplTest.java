package com.linkly.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import com.linkly.category.CategoryRepository;
import com.linkly.category.CategoryServiceImpl;
import com.linkly.category.dto.CategoryResponse;
import com.linkly.category.dto.CreateCategoryRequest;
import com.linkly.category.dto.UpdateCategoryRequest;
import com.linkly.domain.AppUser;
import com.linkly.domain.Category;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.user.AppUserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl 테스트")
class CategoryServiceImplTest {

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private AppUserRepository userRepository;

	@InjectMocks
	private CategoryServiceImpl categoryService;

	@Test
	@DisplayName("카테고리 생성 성공")
	void createCategory_Success() {
		// given
		Long userId = 1L;
		CreateCategoryRequest request = CreateCategoryRequest.builder().name("개발").description("개발 관련 북마크").build();

		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트")
				.build();

		Category savedCategory = Category.builder().id(1L).appUser(user).name("개발").description("개발 관련 북마크").build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
		given(categoryRepository.existsByAppUserAndName(user, "개발")).willReturn(false);
		given(categoryRepository.save(any(Category.class))).willReturn(savedCategory);

		// when
		CategoryResponse response = categoryService.createCategory(userId, request);

		// then
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getName()).isEqualTo("개발");
		assertThat(response.getUserId()).isEqualTo(userId);

		then(userRepository).should(times(1)).findByIdAndDeletedAtIsNull(userId);
		then(categoryRepository).should(times(1)).existsByAppUserAndName(user, "개발");
		then(categoryRepository).should(times(1)).save(any(Category.class));
	}

	@Test
	@DisplayName("카테고리 생성 실패 - 존재하지 않는 사용자")
	void createCategory_UserNotFound() {
		// given
		Long userId = 999L;
		CreateCategoryRequest request = CreateCategoryRequest.builder().name("개발").build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> categoryService.createCategory(userId, request))
				.isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("User");

		then(categoryRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("카테고리 생성 실패 - 이름 중복")
	void createCategory_DuplicateName() {
		// given
		Long userId = 1L;
		CreateCategoryRequest request = CreateCategoryRequest.builder().name("개발").build();

		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트")
				.build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
		given(categoryRepository.existsByAppUserAndName(user, "개발")).willReturn(true);

		// when & then
		assertThatThrownBy(() -> categoryService.createCategory(userId, request))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("이미 사용 중인 카테고리 이름입니다");

		then(categoryRepository).should(never()).save(any());
	}

	@Test
	@DisplayName("카테고리 ID로 조회 성공")
	void getCategoryById_Success() {
		// given
		Long categoryId = 1L;
		AppUser user = AppUser.builder().id(1L).email("test@example.com").password("password123").name("테스트").build();

		Category category = Category.builder().id(categoryId).appUser(user).name("개발").build();

		given(categoryRepository.findByIdAndDeletedAtIsNull(categoryId)).willReturn(Optional.of(category));

		// when
		CategoryResponse response = categoryService.getCategoryById(categoryId);

		// then
		assertThat(response.getId()).isEqualTo(categoryId);
		assertThat(response.getName()).isEqualTo("개발");

		then(categoryRepository).should(times(1)).findByIdAndDeletedAtIsNull(categoryId);
	}

	@Test
	@DisplayName("카테고리 ID로 조회 실패 - 존재하지 않음")
	void getCategoryById_NotFound() {
		// given
		Long categoryId = 999L;
		given(categoryRepository.findByIdAndDeletedAtIsNull(categoryId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
				.isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Category");
	}

	@Test
	@DisplayName("사용자의 카테고리 목록 조회")
	void getCategoriesByUserId() {
		// given
		Long userId = 1L;
		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트")
				.build();

		Category category1 = Category.builder().id(1L).appUser(user).name("개발").build();

		Category category2 = Category.builder().id(2L).appUser(user).name("디자인").build();

		given(userRepository.existsById(userId)).willReturn(true);
		given(categoryRepository.findAllByAppUser_IdAndDeletedAtIsNull(userId))
				.willReturn(Arrays.asList(category1, category2));

		// when
		List<CategoryResponse> responses = categoryService.getCategoriesByUserId(userId);

		// then
		assertThat(responses).hasSize(2);
		assertThat(responses).extracting("name").containsExactlyInAnyOrder("개발", "디자인");

		then(userRepository).should(times(1)).existsById(userId);
		then(categoryRepository).should(times(1)).findAllByAppUser_IdAndDeletedAtIsNull(userId);
	}

	@Test
	@DisplayName("사용자의 카테고리 목록 조회 실패 - 존재하지 않는 사용자")
	void getCategoriesByUserId_UserNotFound() {
		// given
		Long userId = 999L;
		given(userRepository.existsById(userId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> categoryService.getCategoriesByUserId(userId))
				.isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("User");
	}

	@Test
	@DisplayName("카테고리 수정 성공")
	void updateCategory_Success() {
		// given
		Long categoryId = 1L;
		Long userId = 1L;
		UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("수정된 이름").description("수정된 설명").build();

		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트")
				.build();

		Category category = Category.builder().id(categoryId).appUser(user).name("이전 이름").description("이전 설명").build();

		given(categoryRepository.findByIdAndDeletedAtIsNull(categoryId)).willReturn(Optional.of(category));
		given(categoryRepository.existsByAppUserAndName(user, "수정된 이름")).willReturn(false);

		// when
		CategoryResponse response = categoryService.updateCategory(categoryId, userId, request);

		// then
		assertThat(response.getName()).isEqualTo("수정된 이름");

		then(categoryRepository).should(times(1)).findByIdAndDeletedAtIsNull(categoryId);
		then(categoryRepository).should(times(1)).existsByAppUserAndName(user, "수정된 이름");
	}

	@Test
	@DisplayName("카테고리 수정 실패 - 권한 없음")
	void updateCategory_Unauthorized() {
		// given
		Long categoryId = 1L;
		Long userId = 1L;
		Long anotherUserId = 2L;
		UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("수정된 이름").build();

		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트")
				.build();

		Category category = Category.builder().id(categoryId).appUser(user).name("개발").build();

		given(categoryRepository.findByIdAndDeletedAtIsNull(categoryId)).willReturn(Optional.of(category));

		// when & then
		assertThatThrownBy(() -> categoryService.updateCategory(categoryId, anotherUserId, request))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("권한이 없습니다");
	}

	@Test
	@DisplayName("카테고리 수정 실패 - 이름 중복")
	void updateCategory_DuplicateName() {
		// given
		Long categoryId = 1L;
		Long userId = 1L;
		UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("중복된 이름").build();

		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트")
				.build();

		Category category = Category.builder().id(categoryId).appUser(user).name("개발").build();

		given(categoryRepository.findByIdAndDeletedAtIsNull(categoryId)).willReturn(Optional.of(category));
		given(categoryRepository.existsByAppUserAndName(user, "중복된 이름")).willReturn(true);

		// when & then
		assertThatThrownBy(() -> categoryService.updateCategory(categoryId, userId, request))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("이미 사용 중인 카테고리 이름입니다");
	}

	@Test
	@DisplayName("카테고리 삭제 성공")
	void deleteCategory_Success() {
		// given
		Long categoryId = 1L;
		Long userId = 1L;

		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트")
				.build();

		Category category = Category.builder().id(categoryId).appUser(user).name("개발").build();

		given(categoryRepository.findByIdAndDeletedAtIsNull(categoryId)).willReturn(Optional.of(category));

		// when
		categoryService.deleteCategory(categoryId, userId);

		// then
		assertThat(category.getDeletedAt()).isNotNull();
		assertThat(category.isDeleted()).isTrue();

		then(categoryRepository).should(times(1)).findByIdAndDeletedAtIsNull(categoryId);
	}

	@Test
	@DisplayName("카테고리 삭제 실패 - 권한 없음")
	void deleteCategory_Unauthorized() {
		// given
		Long categoryId = 1L;
		Long userId = 1L;
		Long anotherUserId = 2L;

		AppUser user = AppUser.builder().id(userId).email("test@example.com").password("password123").name("테스트")
				.build();

		Category category = Category.builder().id(categoryId).appUser(user).name("개발").build();

		given(categoryRepository.findByIdAndDeletedAtIsNull(categoryId)).willReturn(Optional.of(category));

		// when & then
		assertThatThrownBy(() -> categoryService.deleteCategory(categoryId, anotherUserId))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("권한이 없습니다");
	}
}
