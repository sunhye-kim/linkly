package com.linkly.category;

import com.linkly.category.dto.CategoryResponse;
import com.linkly.category.dto.CreateCategoryRequest;
import com.linkly.category.dto.UpdateCategoryRequest;
import java.util.List;

/** 카테고리 관리 서비스 인터페이스 */
public interface CategoryService {

	/**
	 * 카테고리 생성
	 *
	 * @param userId
	 *            사용자 ID
	 * @param request
	 *            카테고리 생성 정보
	 * @return 생성된 카테고리 정보
	 */
	CategoryResponse createCategory(Long userId, CreateCategoryRequest request);

	/**
	 * 카테고리 ID로 조회
	 *
	 * @param categoryId
	 *            카테고리 ID
	 * @return 카테고리 정보
	 */
	CategoryResponse getCategoryById(Long categoryId);

	/**
	 * 사용자의 모든 카테고리 조회
	 *
	 * @param userId
	 *            사용자 ID
	 * @return 카테고리 목록
	 */
	List<CategoryResponse> getCategoriesByUserId(Long userId);

	/**
	 * 카테고리 정보 수정
	 *
	 * @param categoryId
	 *            카테고리 ID
	 * @param userId
	 *            사용자 ID (권한 체크용)
	 * @param request
	 *            수정할 정보
	 * @return 수정된 카테고리 정보
	 */
	CategoryResponse updateCategory(Long categoryId, Long userId, UpdateCategoryRequest request);

	/**
	 * 카테고리 삭제 (Soft Delete)
	 *
	 * @param categoryId
	 *            카테고리 ID
	 * @param userId
	 *            사용자 ID (권한 체크용)
	 */
	void deleteCategory(Long categoryId, Long userId);
}
