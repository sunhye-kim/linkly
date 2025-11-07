package com.linkly.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.linkly.bookmark.BookmarkRepository;
import com.linkly.bookmark.BookmarkServiceImpl;
import com.linkly.bookmark.BookmarkTagMapRepository;
import com.linkly.bookmark.dto.BookmarkResponse;
import com.linkly.bookmark.dto.CreateBookmarkRequest;
import com.linkly.bookmark.dto.UpdateBookmarkRequest;
import com.linkly.category.CategoryRepository;
import com.linkly.domain.*;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.tag.TagRepository;
import com.linkly.user.AppUserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Service 계층 테스트 Django의 Service 테스트와 유사 @ExtendWith(MockitoExtension.class):
 * Mockito 사용 - 비즈니스 로직만 테스트 - Repository는 Mock으로 대체
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkServiceImpl 테스트")
class BookmarkServiceImplTest {

	@InjectMocks
	private BookmarkServiceImpl bookmarkService;

	@Mock
	private BookmarkRepository bookmarkRepository;

	@Mock
	private AppUserRepository userRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private TagRepository tagRepository;

	@Mock
	private BookmarkTagMapRepository bookmarkTagMapRepository;

	private AppUser testUser;
	private Category testCategory;
	private Bookmark testBookmark;
	private Tag testTag;

	@BeforeEach
	void setUp() {
		testUser = AppUser.builder().id(1L).email("test@example.com").password("password123").name("테스트 사용자").build();

		testCategory = Category.builder().id(1L).appUser(testUser).name("개발").description("개발 관련 북마크").build();

		testBookmark = Bookmark.builder().id(1L).appUser(testUser).category(testCategory).url("https://example.com")
				.title("테스트 북마크").description("테스트 설명").build();

		testTag = Tag.builder().id(1L).appUser(testUser).name("Java").build();
	}

	@Test
	@DisplayName("북마크 생성 성공 - 태그 포함")
	void createBookmark_WithTags() {
		// given
		Long userId = 1L;
		CreateBookmarkRequest request = CreateBookmarkRequest.builder().userId(userId).categoryId(1L)
				.url("https://example.com").title("테스트 북마크").description("테스트 설명").tags(Arrays.asList("Java", "Spring"))
				.build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(testUser));
		given(categoryRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(testCategory));
		given(bookmarkRepository.findByAppUserAndUrl(testUser, request.getUrl())).willReturn(Optional.empty());
		given(bookmarkRepository.save(any(Bookmark.class))).willReturn(testBookmark);
		given(tagRepository.findByAppUserAndName(eq(testUser), eq("Java"))).willReturn(Optional.of(testTag));
		given(tagRepository.findByAppUserAndName(eq(testUser), eq("Spring"))).willReturn(Optional.empty());
		given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> {
			Tag tag = invocation.getArgument(0);
			return Tag.builder().id(2L).name(tag.getName()).appUser(tag.getAppUser()).build();
		});
		given(bookmarkTagMapRepository.existsByBookmarkAndTag(any(), any())).willReturn(false);

		// when
		BookmarkResponse response = bookmarkService.createBookmark(userId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getUrl()).isEqualTo("https://example.com");
		assertThat(response.getTags()).hasSize(2);
		then(bookmarkRepository).should(times(1)).save(any(Bookmark.class));
		then(bookmarkTagMapRepository).should(times(2)).save(any(BookmarkTagMap.class));
	}

	@Test
	@DisplayName("북마크 생성 성공 - 태그 없음")
	void createBookmark_WithoutTags() {
		// given
		Long userId = 1L;
		CreateBookmarkRequest request = CreateBookmarkRequest.builder().userId(userId).url("https://example.com")
				.title("테스트 북마크").build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(testUser));
		given(bookmarkRepository.findByAppUserAndUrl(testUser, request.getUrl())).willReturn(Optional.empty());
		given(bookmarkRepository.save(any(Bookmark.class))).willReturn(testBookmark);

		// when
		BookmarkResponse response = bookmarkService.createBookmark(userId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getTags()).isEmpty();
		then(bookmarkRepository).should(times(1)).save(any(Bookmark.class));
	}

	@Test
	@DisplayName("북마크 생성 실패 - 사용자 없음")
	void createBookmark_UserNotFound() {
		// given
		Long userId = 999L;
		CreateBookmarkRequest request = CreateBookmarkRequest.builder().userId(userId).url("https://example.com")
				.title("테스트 북마크").build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> bookmarkService.createBookmark(userId, request))
				.isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("User");
	}

	@Test
	@DisplayName("북마크 생성 실패 - URL 중복")
	void createBookmark_DuplicateUrl() {
		// given
		Long userId = 1L;
		CreateBookmarkRequest request = CreateBookmarkRequest.builder().userId(userId).url("https://example.com")
				.title("테스트 북마크").build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(testUser));
		given(bookmarkRepository.findByAppUserAndUrl(testUser, request.getUrl())).willReturn(Optional.of(testBookmark));

		// when & then
		assertThatThrownBy(() -> bookmarkService.createBookmark(userId, request))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("이미 저장된 URL입니다");
	}

	@Test
	@DisplayName("북마크 생성 실패 - 카테고리 권한 없음")
	void createBookmark_CategoryUnauthorized() {
		// given
		Long userId = 1L;
		CreateBookmarkRequest request = CreateBookmarkRequest.builder().userId(userId).categoryId(1L)
				.url("https://example.com").title("테스트 북마크").build();

		AppUser otherUser = AppUser.builder().id(2L).email("other@example.com").password("password123").name("다른 사용자")
				.build();

		Category otherCategory = Category.builder().id(1L).appUser(otherUser).name("다른 카테고리").build();

		given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(testUser));
		given(categoryRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(otherCategory));

		// when & then
		assertThatThrownBy(() -> bookmarkService.createBookmark(userId, request))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("해당 카테고리를 사용할 권한이 없습니다");
	}

	@Test
	@DisplayName("북마크 ID로 조회 성공")
	void getBookmarkById() {
		// given
		Long bookmarkId = 1L;
		given(bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)).willReturn(Optional.of(testBookmark));
		given(bookmarkTagMapRepository.findAllByBookmark(testBookmark)).willReturn(Arrays.asList());

		// when
		BookmarkResponse response = bookmarkService.getBookmarkById(bookmarkId);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(bookmarkId);
		assertThat(response.getUrl()).isEqualTo("https://example.com");
	}

	@Test
	@DisplayName("북마크 ID로 조회 실패 - 북마크 없음")
	void getBookmarkById_NotFound() {
		// given
		Long bookmarkId = 999L;
		given(bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> bookmarkService.getBookmarkById(bookmarkId))
				.isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Bookmark");
	}

	@Test
	@DisplayName("사용자별 북마크 목록 조회")
	void getBookmarksByUserId() {
		// given
		Long userId = 1L;
		Bookmark bookmark1 = Bookmark.builder().id(1L).appUser(testUser).url("https://example1.com").title("북마크1")
				.build();

		Bookmark bookmark2 = Bookmark.builder().id(2L).appUser(testUser).url("https://example2.com").title("북마크2")
				.build();

		given(userRepository.existsById(userId)).willReturn(true);
		given(bookmarkRepository.findAllByAppUserIdAndDeletedAtIsNull(userId))
				.willReturn(Arrays.asList(bookmark1, bookmark2));
		given(bookmarkTagMapRepository.findAllByBookmark(any())).willReturn(Arrays.asList());

		// when
		List<BookmarkResponse> responses = bookmarkService.getBookmarksByUserId(userId);

		// then
		assertThat(responses).hasSize(2);
		assertThat(responses).extracting("title").containsExactlyInAnyOrder("북마크1", "북마크2");
	}

	@Test
	@DisplayName("사용자별 북마크 목록 조회 실패 - 사용자 없음")
	void getBookmarksByUserId_UserNotFound() {
		// given
		Long userId = 999L;
		given(userRepository.existsById(userId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> bookmarkService.getBookmarksByUserId(userId))
				.isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("User");
	}

	@Test
	@DisplayName("북마크 수정 성공 - 태그 교체")
	void updateBookmark_Success() {
		// given
		Long bookmarkId = 1L;
		Long userId = 1L;
		UpdateBookmarkRequest request = UpdateBookmarkRequest.builder().title("수정된 제목")
				.tags(Arrays.asList("Python", "Django")).build();

		given(bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)).willReturn(Optional.of(testBookmark));
		given(tagRepository.findByAppUserAndName(eq(testUser), any())).willReturn(Optional.empty());
		given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> {
			Tag tag = invocation.getArgument(0);
			return Tag.builder().id(System.currentTimeMillis()).name(tag.getName()).appUser(tag.getAppUser()).build();
		});
		given(bookmarkTagMapRepository.existsByBookmarkAndTag(any(), any())).willReturn(false);

		// when
		BookmarkResponse response = bookmarkService.updateBookmark(bookmarkId, userId, request);

		// then
		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("수정된 제목");
		then(bookmarkTagMapRepository).should(times(1)).deleteAllByBookmark(testBookmark);
		then(bookmarkTagMapRepository).should(times(2)).save(any(BookmarkTagMap.class));
	}

	@Test
	@DisplayName("북마크 수정 실패 - 권한 없음")
	void updateBookmark_Unauthorized() {
		// given
		Long bookmarkId = 1L;
		Long userId = 2L; // 다른 사용자
		UpdateBookmarkRequest request = UpdateBookmarkRequest.builder().title("수정된 제목").build();

		given(bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)).willReturn(Optional.of(testBookmark));

		// when & then
		assertThatThrownBy(() -> bookmarkService.updateBookmark(bookmarkId, userId, request))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("해당 북마크를 수정할 권한이 없습니다");
	}

	@Test
	@DisplayName("북마크 수정 실패 - URL 중복")
	void updateBookmark_DuplicateUrl() {
		// given
		Long bookmarkId = 1L;
		Long userId = 1L;
		UpdateBookmarkRequest request = UpdateBookmarkRequest.builder().url("https://duplicate.com").build();

		Bookmark duplicateBookmark = Bookmark.builder().id(2L).appUser(testUser).url("https://duplicate.com")
				.title("중복 북마크").build();

		given(bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)).willReturn(Optional.of(testBookmark));
		given(bookmarkRepository.findByAppUserAndUrl(testUser, "https://duplicate.com"))
				.willReturn(Optional.of(duplicateBookmark));

		// when & then
		assertThatThrownBy(() -> bookmarkService.updateBookmark(bookmarkId, userId, request))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("이미 저장된 URL입니다");
	}

	@Test
	@DisplayName("북마크 삭제 성공")
	void deleteBookmark_Success() {
		// given
		Long bookmarkId = 1L;
		Long userId = 1L;

		given(bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)).willReturn(Optional.of(testBookmark));

		// when
		bookmarkService.deleteBookmark(bookmarkId, userId);

		// then
		then(bookmarkTagMapRepository).should(times(1)).deleteAllByBookmark(testBookmark);
		assertThat(testBookmark.getDeletedAt()).isNotNull();
	}

	@Test
	@DisplayName("북마크 삭제 실패 - 북마크 없음")
	void deleteBookmark_NotFound() {
		// given
		Long bookmarkId = 999L;
		Long userId = 1L;

		given(bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> bookmarkService.deleteBookmark(bookmarkId, userId))
				.isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Bookmark");
	}

	@Test
	@DisplayName("북마크 삭제 실패 - 권한 없음")
	void deleteBookmark_Unauthorized() {
		// given
		Long bookmarkId = 1L;
		Long userId = 2L; // 다른 사용자

		given(bookmarkRepository.findByIdAndDeletedAtIsNull(bookmarkId)).willReturn(Optional.of(testBookmark));

		// when & then
		assertThatThrownBy(() -> bookmarkService.deleteBookmark(bookmarkId, userId))
				.isInstanceOf(InvalidRequestException.class).hasMessageContaining("해당 북마크를 삭제할 권한이 없습니다");
	}
}
