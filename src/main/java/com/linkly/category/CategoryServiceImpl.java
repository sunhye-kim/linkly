package com.linkly.category;

import com.linkly.category.dto.CategoryResponse;
import com.linkly.category.dto.CreateCategoryRequest;
import com.linkly.category.dto.UpdateCategoryRequest;
import com.linkly.domain.AppUser;
import com.linkly.domain.Category;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.user.AppUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final AppUserRepository userRepository;

	@Override
	@Transactional
	public CategoryResponse createCategory(Long userId, CreateCategoryRequest request) {
		log.info("카테고리 생성 시도: userId={}, name={}", userId, request.getName());

		// 사용자 조회
		AppUser user = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));

		// 카테고리 이름 중복 체크 (같은 사용자 내에서)
		if (categoryRepository.existsByAppUserAndName(user, request.getName())) {
			throw new InvalidRequestException("이미 사용 중인 카테고리 이름입니다", "name=" + request.getName());
		}

		// 카테고리 생성
		Category category = Category.builder().appUser(user).name(request.getName())
				.description(request.getDescription()).build();

		Category savedCategory = categoryRepository.save(category);
		log.info("카테고리 생성 완료: categoryId={}, name={}", savedCategory.getId(), savedCategory.getName());

		return CategoryResponse.from(savedCategory);
	}

	@Override
	public CategoryResponse getCategoryById(Long categoryId) {
		log.debug("카테고리 조회: categoryId={}", categoryId);

		Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

		return CategoryResponse.from(category);
	}

	@Override
	public List<CategoryResponse> getCategoriesByUserId(Long userId) {
		log.debug("사용자의 카테고리 목록 조회: userId={}", userId);

		// 사용자 존재 확인
		if (!userRepository.existsById(userId)) {
			throw new ResourceNotFoundException("User", userId);
		}

		List<Category> categories = categoryRepository.findAllByAppUser_IdAndDeletedAtIsNull(userId);

		return categories.stream().map(CategoryResponse::from).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public CategoryResponse updateCategory(Long categoryId, Long userId, UpdateCategoryRequest request) {
		log.info("카테고리 수정 시도: categoryId={}, userId={}", categoryId, userId);

		// 카테고리 조회
		Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

		// 권한 체크: 카테고리 소유자만 수정 가능
		if (!category.getAppUser().getId().equals(userId)) {
			throw new InvalidRequestException("해당 카테고리를 수정할 권한이 없습니다",
					"categoryId=" + categoryId + ", userId=" + userId);
		}

		// 카테고리 이름 변경 시 중복 체크
		if (request.getName() != null && !request.getName().equals(category.getName())) {
			if (categoryRepository.existsByAppUserAndName(category.getAppUser(), request.getName())) {
				throw new InvalidRequestException("이미 사용 중인 카테고리 이름입니다", "name=" + request.getName());
			}
		}

		// 카테고리 정보 수정
		category.updateInfo(request.getName(), request.getDescription());

		log.info("카테고리 수정 완료: categoryId={}", categoryId);

		return CategoryResponse.from(category);
	}

	@Override
	@Transactional
	public void deleteCategory(Long categoryId, Long userId) {
		log.info("카테고리 삭제 시도: categoryId={}, userId={}", categoryId, userId);

		// 카테고리 조회
		Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));

		// 권한 체크: 카테고리 소유자만 삭제 가능
		if (!category.getAppUser().getId().equals(userId)) {
			throw new InvalidRequestException("해당 카테고리를 삭제할 권한이 없습니다",
					"categoryId=" + categoryId + ", userId=" + userId);
		}

		// Soft Delete
		category.softDelete();

		log.info("카테고리 삭제 완료: categoryId={}", categoryId);
	}
}
