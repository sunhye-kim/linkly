package com.linkly.bookmark.dto;

import com.linkly.domain.Bookmark;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "북마크 정보 응답")
public class BookmarkResponse {

    @Schema(description = "북마크 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "개발")
    private String categoryName;

    @Schema(description = "북마크 URL", example = "https://example.com")
    private String url;

    @Schema(description = "북마크 제목", example = "유용한 개발 자료")
    private String title;

    @Schema(description = "북마크 설명", example = "Spring Boot 관련 튜토리얼")
    private String description;

    @Schema(description = "태그 목록", example = "[\"Java\", \"Spring\", \"Backend\"]")
    private List<String> tags;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    /**
     * Entity를 DTO로 변환
     */
    public static BookmarkResponse from(Bookmark bookmark, List<String> tags) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .userId(bookmark.getAppUser().getId())
                .categoryId(bookmark.getCategory() != null ? bookmark.getCategory().getId() : null)
                .categoryName(bookmark.getCategory() != null ? bookmark.getCategory().getName() : null)
                .url(bookmark.getUrl())
                .title(bookmark.getTitle())
                .description(bookmark.getDescription())
                .tags(tags)
                .createdAt(bookmark.getCreatedAt())
                .updatedAt(bookmark.getUpdatedAt())
                .build();
    }
}