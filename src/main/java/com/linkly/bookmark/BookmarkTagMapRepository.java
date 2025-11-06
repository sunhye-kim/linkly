package com.linkly.bookmark;

import com.linkly.domain.Bookmark;
import com.linkly.domain.BookmarkTagMap;
import com.linkly.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkTagMapRepository extends JpaRepository<BookmarkTagMap, Long> {

    /**
     * 북마크별 모든 태그 매핑 조회
     */
    List<BookmarkTagMap> findAllByBookmark(Bookmark bookmark);

    /**
     * 태그별 모든 북마크 매핑 조회
     */
    List<BookmarkTagMap> findAllByTag(Tag tag);

    /**
     * 북마크와 태그로 매핑 조회
     */
    Optional<BookmarkTagMap> findByBookmarkAndTag(Bookmark bookmark, Tag tag);

    /**
     * 북마크와 태그 매핑 존재 여부 확인
     */
    boolean existsByBookmarkAndTag(Bookmark bookmark, Tag tag);

    /**
     * 북마크 ID로 모든 매핑 조회
     */
    List<BookmarkTagMap> findAllByBookmarkId(Long bookmarkId);

    /**
     * 태그 ID로 모든 매핑 조회
     */
    List<BookmarkTagMap> findAllByTagId(Long tagId);

    /**
     * 북마크에 연결된 모든 매핑 삭제
     */
    void deleteAllByBookmark(Bookmark bookmark);

    /**
     * 태그에 연결된 모든 매핑 삭제
     */
    void deleteAllByTag(Tag tag);

    /**
     * 특정 태그들이 모두 포함된 북마크 조회
     */
    @Query("SELECT btm.bookmark FROM BookmarkTagMap btm " +
           "WHERE btm.tag.id IN :tagIds " +
           "GROUP BY btm.bookmark " +
           "HAVING COUNT(DISTINCT btm.tag.id) = :tagCount")
    List<Bookmark> findBookmarksByAllTags(@Param("tagIds") List<Long> tagIds,
                                          @Param("tagCount") long tagCount);
}