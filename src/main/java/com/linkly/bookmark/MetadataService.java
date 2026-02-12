package com.linkly.bookmark;

import com.linkly.bookmark.dto.UrlMetadataResponse;

/** URL 메타데이터 추출 서비스 인터페이스 */
public interface MetadataService {

	/**
	 * URL에서 메타데이터(제목, 설명)를 추출한다.
	 *
	 * @param url
	 *            메타데이터를 추출할 URL
	 * @return 추출된 메타데이터 (실패 시 필드가 null인 응답 반환)
	 */
	UrlMetadataResponse extractMetadata(String url);
}
