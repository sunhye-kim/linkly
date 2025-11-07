package com.linkly.bookmark.repository;

import com.linkly.bookmark.BookmarkRepository;
import com.linkly.bookmark.BookmarkTagMapRepository;
import com.linkly.category.CategoryRepository;
import com.linkly.global.config.JpaAuditingConfig;
import com.linkly.domain.*;
import com.linkly.tag.TagRepository;
import com.linkly.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository 계층 테스트
 * Django의 Model 테스트와 유사
 *
 * @DataJpaTest: JPA 관련 컴포넌트만 로드 (빠른 테스트)
 * - Entity, Repository만 로드
 * - 각 테스트 후 자동 롤백
 * - 인메모리 DB 사용
 */
@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("BookmarkRepository 테스트")
class BookmarkRepositoryTest {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private BookmarkTagMapRepository bookmarkTagMapRepository;

    private AppUser testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = AppUser.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트 사용자")
                .build();
        testUser = userRepository.save(testUser);

        // 테스트용 카테고리 생성
        testCategory = Category.builder()
                .appUser(testUser)
                .name("개발")
                .description("개발 관련 북마크")
                .build();
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("북마크 저장 및 조회")
    void saveAndFindBookmark() {
        // given
        Bookmark bookmark = Bookmark.builder()
                .appUser(testUser)
                .category(testCategory)
                .url("https://example.com")
                .title("테스트 북마크")
                .description("테스트용 북마크입니다")
                .build();

        // when
        Bookmark savedBookmark = bookmarkRepository.save(bookmark);

        // then
        assertThat(savedBookmark.getId()).isNotNull();
        assertThat(savedBookmark.getUrl()).isEqualTo("https://example.com");
        assertThat(savedBookmark.getTitle()).isEqualTo("테스트 북마크");
        assertThat(savedBookmark.getAppUser()).isEqualTo(testUser);
        assertThat(savedBookmark.getCategory()).isEqualTo(testCategory);
        assertThat(savedBookmark.getCreatedAt()).isNotNull();
        assertThat(savedBookmark.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자별 북마크 조회")
    void findAllByAppUser() {
        // given
        Bookmark bookmark1 = createBookmark("https://example1.com", "북마크1");
        Bookmark bookmark2 = createBookmark("https://example2.com", "북마크2");
        bookmarkRepository.save(bookmark1);
        bookmarkRepository.save(bookmark2);

        // 다른 사용자의 북마크
        AppUser otherUser = userRepository.save(AppUser.builder()
                .email("other@example.com")
                .password("password123")
                .name("다른 사용자")
                .build());
        Bookmark otherBookmark = Bookmark.builder()
                .appUser(otherUser)
                .url("https://other.com")
                .title("다른 사용자 북마크")
                .build();
        bookmarkRepository.save(otherBookmark);

        // when
        List<Bookmark> bookmarks = bookmarkRepository.findAllByAppUser(testUser);

        // then
        assertThat(bookmarks).hasSize(2);
        assertThat(bookmarks).extracting("url")
                .containsExactlyInAnyOrder("https://example1.com", "https://example2.com");
    }

    @Test
    @DisplayName("사용자 ID로 북마크 조회")
    void findAllByAppUserId() {
        // given
        Bookmark bookmark1 = createBookmark("https://example1.com", "북마크1");
        Bookmark bookmark2 = createBookmark("https://example2.com", "북마크2");
        bookmarkRepository.save(bookmark1);
        bookmarkRepository.save(bookmark2);

        // when
        List<Bookmark> bookmarks = bookmarkRepository.findAllByAppUserIdAndDeletedAtIsNull(testUser.getId());

        // then
        assertThat(bookmarks).hasSize(2);
    }

    @Test
    @DisplayName("카테고리별 북마크 조회")
    void findAllByCategory() {
        // given
        Bookmark bookmark1 = createBookmark("https://example1.com", "북마크1");
        Bookmark bookmark2 = createBookmark("https://example2.com", "북마크2");
        bookmarkRepository.save(bookmark1);
        bookmarkRepository.save(bookmark2);

        // 다른 카테고리
        Category otherCategory = categoryRepository.save(Category.builder()
                .appUser(testUser)
                .name("디자인")
                .build());
        Bookmark otherBookmark = Bookmark.builder()
                .appUser(testUser)
                .category(otherCategory)
                .url("https://other.com")
                .title("다른 카테고리 북마크")
                .build();
        bookmarkRepository.save(otherBookmark);

        // when
        List<Bookmark> bookmarks = bookmarkRepository.findAllByCategory(testCategory);

        // then
        assertThat(bookmarks).hasSize(2);
        assertThat(bookmarks).extracting("category").containsOnly(testCategory);
    }

    @Test
    @DisplayName("사용자와 URL로 북마크 조회 (중복 체크)")
    void findByAppUserAndUrl() {
        // given
        String url = "https://example.com";
        Bookmark bookmark = createBookmark(url, "테스트 북마크");
        bookmarkRepository.save(bookmark);

        // when
        Optional<Bookmark> found = bookmarkRepository.findByAppUserAndUrl(testUser, url);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUrl()).isEqualTo(url);
    }

    @Test
    @DisplayName("URL 중복 체크 - 같은 사용자는 중복 불가")
    void existsByAppUserAndUrl() {
        // given
        String url = "https://example.com";
        Bookmark bookmark = createBookmark(url, "테스트 북마크");
        bookmarkRepository.save(bookmark);

        // when
        boolean exists = bookmarkRepository.existsByAppUserAndUrl(testUser, url);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("URL 중복 체크 - 다른 사용자는 같은 URL 사용 가능")
    void existsByAppUserAndUrl_DifferentUser() {
        // given
        String url = "https://example.com";
        Bookmark bookmark = createBookmark(url, "테스트 북마크");
        bookmarkRepository.save(bookmark);

        AppUser otherUser = userRepository.save(AppUser.builder()
                .email("other@example.com")
                .password("password123")
                .name("다른 사용자")
                .build());

        // when
        boolean exists = bookmarkRepository.existsByAppUserAndUrl(otherUser, url);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Soft Delete - 삭제된 북마크는 조회되지 않음")
    void softDelete() {
        // given
        Bookmark bookmark = createBookmark("https://example.com", "테스트 북마크");
        bookmark = bookmarkRepository.save(bookmark);

        // when
        bookmark.softDelete();
        bookmarkRepository.save(bookmark);

        // then
        Optional<Bookmark> found = bookmarkRepository.findByIdAndDeletedAtIsNull(bookmark.getId());
        assertThat(found).isEmpty();

        List<Bookmark> userBookmarks = bookmarkRepository.findAllByAppUserAndDeletedAtIsNull(testUser);
        assertThat(userBookmarks).isEmpty();
    }

    @Test
    @DisplayName("태그와 북마크 매핑 저장 및 조회")
    void bookmarkTagMapping() {
        // given
        Bookmark bookmark = createBookmark("https://example.com", "테스트 북마크");
        bookmark = bookmarkRepository.save(bookmark);

        Tag tag1 = tagRepository.save(Tag.builder()
                .appUser(testUser)
                .name("Java")
                .build());

        Tag tag2 = tagRepository.save(Tag.builder()
                .appUser(testUser)
                .name("Spring")
                .build());

        BookmarkTagMap mapping1 = bookmarkTagMapRepository.save(BookmarkTagMap.builder()
                .bookmark(bookmark)
                .tag(tag1)
                .build());

        BookmarkTagMap mapping2 = bookmarkTagMapRepository.save(BookmarkTagMap.builder()
                .bookmark(bookmark)
                .tag(tag2)
                .build());

        // when
        List<BookmarkTagMap> mappings = bookmarkTagMapRepository.findAllByBookmark(bookmark);

        // then
        assertThat(mappings).hasSize(2);
        assertThat(mappings).extracting(m -> m.getTag().getName())
                .containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    @DisplayName("태그로 북마크 조회")
    void findBookmarksByTag() {
        // given
        Bookmark bookmark1 = createBookmark("https://example1.com", "북마크1");
        Bookmark bookmark2 = createBookmark("https://example2.com", "북마크2");
        bookmark1 = bookmarkRepository.save(bookmark1);
        bookmark2 = bookmarkRepository.save(bookmark2);

        Tag javaTag = tagRepository.save(Tag.builder()
                .appUser(testUser)
                .name("Java")
                .build());

        bookmarkTagMapRepository.save(BookmarkTagMap.builder()
                .bookmark(bookmark1)
                .tag(javaTag)
                .build());

        bookmarkTagMapRepository.save(BookmarkTagMap.builder()
                .bookmark(bookmark2)
                .tag(javaTag)
                .build());

        // when
        List<BookmarkTagMap> mappings = bookmarkTagMapRepository.findAllByTag(javaTag);

        // then
        assertThat(mappings).hasSize(2);
        assertThat(mappings).extracting(m -> m.getBookmark().getTitle())
                .containsExactlyInAnyOrder("북마크1", "북마크2");
    }

    @Test
    @DisplayName("북마크-태그 매핑 중복 체크")
    void existsByBookmarkAndTag() {
        // given
        Bookmark bookmark = createBookmark("https://example.com", "테스트 북마크");
        bookmark = bookmarkRepository.save(bookmark);

        Tag tag = tagRepository.save(Tag.builder()
                .appUser(testUser)
                .name("Java")
                .build());

        bookmarkTagMapRepository.save(BookmarkTagMap.builder()
                .bookmark(bookmark)
                .tag(tag)
                .build());

        // when
        boolean exists = bookmarkTagMapRepository.existsByBookmarkAndTag(bookmark, tag);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("북마크 삭제 시 태그 매핑도 함께 삭제")
    void deleteBookmarkWithTagMappings() {
        // given
        Bookmark bookmark = createBookmark("https://example.com", "테스트 북마크");
        bookmark = bookmarkRepository.save(bookmark);

        Tag tag = tagRepository.save(Tag.builder()
                .appUser(testUser)
                .name("Java")
                .build());

        bookmarkTagMapRepository.save(BookmarkTagMap.builder()
                .bookmark(bookmark)
                .tag(tag)
                .build());

        // when
        bookmarkTagMapRepository.deleteAllByBookmark(bookmark);
        List<BookmarkTagMap> mappings = bookmarkTagMapRepository.findAllByBookmark(bookmark);

        // then
        assertThat(mappings).isEmpty();
    }

    private Bookmark createBookmark(String url, String title) {
        return Bookmark.builder()
                .appUser(testUser)
                .category(testCategory)
                .url(url)
                .title(title)
                .description("테스트 설명")
                .build();
    }
}