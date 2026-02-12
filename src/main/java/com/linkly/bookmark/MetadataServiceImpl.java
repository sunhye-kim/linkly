package com.linkly.bookmark;

import com.linkly.bookmark.dto.UrlMetadataResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MetadataServiceImpl implements MetadataService {

	private static final int TIMEOUT_MS = 5000;
	private static final String USER_AGENT = "Mozilla/5.0 (compatible; Linkly/1.0)";

	@Override
	public UrlMetadataResponse extractMetadata(String url) {
		try {
			Document doc = Jsoup.connect(url)
					.userAgent(USER_AGENT)
					.timeout(TIMEOUT_MS)
					.followRedirects(true)
					.get();

			String title = getTitle(doc);
			String description = getDescription(doc);

			return UrlMetadataResponse.builder()
					.title(title)
					.description(description)
					.build();
		} catch (Exception e) {
			log.warn("URL 메타데이터 추출 실패: url={}, error={}", url, e.getMessage());
			return UrlMetadataResponse.builder().build();
		}
	}

	private String getTitle(Document doc) {
		// og:title 우선
		String ogTitle = doc.select("meta[property=og:title]").attr("content");
		if (!ogTitle.isBlank()) {
			return ogTitle;
		}
		// <title> 태그 폴백
		String title = doc.title();
		return title.isBlank() ? null : title;
	}

	private String getDescription(Document doc) {
		// og:description 우선
		String ogDesc = doc.select("meta[property=og:description]").attr("content");
		if (!ogDesc.isBlank()) {
			return ogDesc;
		}
		// meta[name=description] 폴백
		String desc = doc.select("meta[name=description]").attr("content");
		return desc.isBlank() ? null : desc;
	}
}
