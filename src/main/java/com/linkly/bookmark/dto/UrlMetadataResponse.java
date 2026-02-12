package com.linkly.bookmark.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UrlMetadataResponse {

	private String title;
	private String description;
}
