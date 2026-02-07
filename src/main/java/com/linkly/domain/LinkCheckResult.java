package com.linkly.domain;

import com.linkly.domain.enums.LinkCheckStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "link_check_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class LinkCheckResult extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bookmark_id", nullable = false, foreignKey = @ForeignKey(name = "fk_link_check_bookmark"))
	private Bookmark bookmark;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private LinkCheckStatus status;

	@Column(name = "http_status")
	private Integer httpStatus;

	@Column(name = "response_time_ms")
	private Long responseTimeMs;

	@Column(name = "checked_at", nullable = false)
	private LocalDateTime checkedAt;

	public static LinkCheckResult of(Bookmark bookmark, LinkCheckStatus status, Integer httpStatus, Long responseTimeMs) {
		return LinkCheckResult.builder()
				.bookmark(bookmark)
				.status(status)
				.httpStatus(httpStatus)
				.responseTimeMs(responseTimeMs)
				.checkedAt(LocalDateTime.now())
				.build();
	}
}
