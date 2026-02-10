package com.linkly.bookmark;

import com.linkly.bookmark.dto.BookmarkResponse;
import com.linkly.bookmark.dto.CreateBookmarkRequest;
import com.linkly.bookmark.dto.UpdateBookmarkRequest;
import com.linkly.category.CategoryRepository;
import com.linkly.domain.*;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.tag.TagRepository;
import com.linkly.user.AppUserRepository;
import java.util.ArrayList;
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
public class BookmarkServiceImpl implements BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final AppUserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final TagRepository tagRepository;
	private final BookmarkTagMapRepository bookmarkTagMapRepository;

	@Override
	@Transactional
	public BookmarkResponse createBookmark(Long userId, CreateBookmarkRequest request) {
		log.info("북마크 생성 시도: userId={}, url={}", userId, request.getUrl());

		// 사용자 조회
		AppUser user = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));

		// URL 중복 체크 (같은 사용자 내에서)
		if (bookmarkRepository.findByAppUserAndUrl(user, request.getUrl()).isPresent()) {
			throw new InvalidRequestException("이미 저장된 URL입니다", "url=" + request.getUrl());
		}

		// 카테고리 조회 (있는 경우)
		Category category = null;
		if (request.getCategoryId() != null) {
			category = categoryRepository.findByIdAndDeletedAtIsNull(request.getCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

			// 카테고리 소유자 확인
			if (!category.getAppUser().getId().equals(userId)) {
				throw new InvalidRequestException("해당 카테고리를 사용할 권한이 없습니다", "categoryId=" + request.getCategoryId());
			}
		}

		// 북마크 생성
		Bookmark bookmark = Bookmark.builder().appUser(user).category(category).url(request.getUrl())
				.title(request.getTitle()).description(request.getDescription()).build();

		Bookmark savedBookmark = bookmarkRepository.save(bookmark);

		// 태그 처리
		List<String> tagNames = processTags(savedBookmark, user, request.getTags());

		log.info("북마크 생성 완료: bookmarkId={}, tags={}", savedBookmark.getId(), tagNames);

		return BookmarkResponse.from(savedBookmark, tagNames);
	}

	@Override
	public BookmarkResponse getBookmarkById(Long bookmarkId) {
		log.debug("북마크 조회: bookmarkId={}", bookmarkId);

		Bookmark bookmark = bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)
				.orElseThrow(() -> new ResourceNotFoundException("Bookmark", bookmarkId));

		List<String> tagNames = getTagNames(bookmark);

		return BookmarkResponse.from(bookmark, tagNames);
	}

	@Override
	public List<BookmarkResponse> getBookmarksByUserId(Long userId) {
		log.debug("사용자의 북마크 목록 조회: userId={}", userId);

		// 사용자 존재 확인
		if (!userRepository.existsById(userId)) {
			throw new ResourceNotFoundException("User", userId);
		}

		List<Bookmark> bookmarks = bookmarkRepository.findAllByAppUserIdAndDeletedAtIsNull(userId);

		return bookmarks.stream().map(bookmark -> BookmarkResponse.from(bookmark, getTagNames(bookmark)))
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public BookmarkResponse updateBookmark(Long bookmarkId, Long userId, UpdateBookmarkRequest request) {
		log.info("북마크 수정 시도: bookmarkId={}, userId={}", bookmarkId, userId);

		// 북마크 조회
		Bookmark bookmark = bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)
				.orElseThrow(() -> new ResourceNotFoundException("Bookmark", bookmarkId));

		// 권한 체크: 북마크 소유자만 수정 가능
		if (!bookmark.getAppUser().getId().equals(userId)) {
			throw new InvalidRequestException("해당 북마크를 수정할 권한이 없습니다",
					"bookmarkId=" + bookmarkId + ", userId=" + userId);
		}

		// URL 변경 시 중복 체크
		if (request.getUrl() != null && !request.getUrl().equals(bookmark.getUrl())) {
			if (bookmarkRepository.findByAppUserAndUrl(bookmark.getAppUser(), request.getUrl()).isPresent()) {
				throw new InvalidRequestException("이미 저장된 URL입니다", "url=" + request.getUrl());
			}
		}

		// 카테고리 변경 (null 가능)
		Category category = null;
		if (request.getCategoryId() != null) {
			category = categoryRepository.findByIdAndDeletedAtIsNull(request.getCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

			// 카테고리 소유자 확인
			if (!category.getAppUser().getId().equals(userId)) {
				throw new InvalidRequestException("해당 카테고리를 사용할 권한이 없습니다", "categoryId=" + request.getCategoryId());
			}
		}

		// 북마크 정보 수정
		bookmark.updateInfo(request.getTitle(), request.getDescription(), request.getUrl());
		bookmark.changeCategory(category);

		// 태그 수정 (있는 경우 전체 교체)
		List<String> tagNames;
		if (request.getTags() != null) {
			// 기존 태그 매핑 삭제
			bookmarkTagMapRepository.deleteAllByBookmark(bookmark);
			// 새 태그 추가
			tagNames = processTags(bookmark, bookmark.getAppUser(), request.getTags());
		} else {
			tagNames = getTagNames(bookmark);
		}

		log.info("북마크 수정 완료: bookmarkId={}", bookmarkId);

		return BookmarkResponse.from(bookmark, tagNames);
	}

	@Override
	@Transactional
	public void deleteBookmark(Long bookmarkId, Long userId) {
		log.info("북마크 삭제 시도: bookmarkId={}, userId={}", bookmarkId, userId);

		// 북마크 조회
		Bookmark bookmark = bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)
				.orElseThrow(() -> new ResourceNotFoundException("Bookmark", bookmarkId));

		// 권한 체크: 북마크 소유자만 삭제 가능
		if (!bookmark.getAppUser().getId().equals(userId)) {
			throw new InvalidRequestException("해당 북마크를 삭제할 권한이 없습니다",
					"bookmarkId=" + bookmarkId + ", userId=" + userId);
		}

		// 태그 매핑 삭제
		bookmarkTagMapRepository.deleteAllByBookmark(bookmark);

		// Soft Delete
		bookmark.softDelete();

		log.info("북마크 삭제 완료: bookmarkId={}", bookmarkId);
	}

	@Override
	public List<BookmarkResponse> searchBookmarks(Long userId, String keyword, Long categoryId) {
		log.debug("북마크 검색: userId={}, keyword={}, categoryId={}", userId, keyword, categoryId);

		if (keyword == null || keyword.isBlank()) {
			return getBookmarksByUserId(userId);
		}

		List<Bookmark> bookmarks = bookmarkRepository.searchBookmarks(userId, keyword, categoryId);

		return bookmarks.stream().map(b -> BookmarkResponse.from(b, getTagNames(b))).collect(Collectors.toList());
	}

	/** 태그 처리: 태그가 없으면 생성, 있으면 재사용 */
	private List<String> processTags(Bookmark bookmark, AppUser user, List<String> tagNames) {
		if (tagNames == null || tagNames.isEmpty()) {
			return new ArrayList<>();
		}

		List<String> result = new ArrayList<>();

		for (String tagName : tagNames) {
			// 태그 조회 또는 생성
			Tag tag = tagRepository.findByAppUserAndName(user, tagName).orElseGet(() -> {
				Tag newTag = Tag.builder().appUser(user).name(tagName).build();
				return tagRepository.save(newTag);
			});

			// 북마크-태그 매핑 생성 (중복 체크)
			if (!bookmarkTagMapRepository.existsByBookmarkAndTag(bookmark, tag)) {
				BookmarkTagMap mapping = BookmarkTagMap.builder().bookmark(bookmark).tag(tag).build();
				bookmarkTagMapRepository.save(mapping);
			}

			result.add(tag.getName());
		}

		return result;
	}

	/** 북마크의 태그 이름 목록 조회 */
	private List<String> getTagNames(Bookmark bookmark) {
		return bookmarkTagMapRepository.findAllByBookmark(bookmark).stream().map(mapping -> mapping.getTag().getName())
				.collect(Collectors.toList());
	}
}
