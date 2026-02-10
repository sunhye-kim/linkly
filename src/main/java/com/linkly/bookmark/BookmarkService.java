package com.linkly.bookmark;

import com.linkly.bookmark.dto.BookmarkResponse;
import com.linkly.bookmark.dto.CreateBookmarkRequest;
import com.linkly.bookmark.dto.UpdateBookmarkRequest;
import java.util.List;

/** 북마크 관리 서비스 인터페이스 */
public interface BookmarkService {

	/**
	 * 북마크 생성
	 *
	 * @param userId
	 *            사용자 ID
	 * @param request
	 *            북마크 생성 정보
	 * @return 생성된 북마크 정보
	 */
	BookmarkResponse createBookmark(Long userId, CreateBookmarkRequest request);

	/**
	 * 북마크 ID로 조회
	 *
	 * @param bookmarkId
	 *            북마크 ID
	 * @return 북마크 정보
	 */
	BookmarkResponse getBookmarkById(Long bookmarkId);

	/**
	 * 사용자의 모든 북마크 조회
	 *
	 * @param userId
	 *            사용자 ID
	 * @return 북마크 목록
	 */
	List<BookmarkResponse> getBookmarksByUserId(Long userId);

	/**
	 * 북마크 정보 수정
	 *
	 * @param bookmarkId
	 *            북마크 ID
	 * @param userId
	 *            사용자 ID (권한 체크용)
	 * @param request
	 *            수정할 정보
	 * @return 수정된 북마크 정보
	 */
	BookmarkResponse updateBookmark(Long bookmarkId, Long userId, UpdateBookmarkRequest request);

	/**
	 * 북마크 삭제 (Soft Delete)
	 *
	 * @param bookmarkId
	 *            북마크 ID
	 * @param userId
	 *            사용자 ID (권한 체크용)
	 */
	void deleteBookmark(Long bookmarkId, Long userId);

	/**
	 * 키워드로 북마크 검색 (제목·URL·설명·태그 대상)
	 *
	 * @param userId
	 *            사용자 ID
	 * @param keyword
	 *            검색 키워드
	 * @param categoryId
	 *            카테고리 ID (null 이면 전체)
	 * @return 검색된 북마크 목록
	 */
	List<BookmarkResponse> searchBookmarks(Long userId, String keyword, Long categoryId);
}
