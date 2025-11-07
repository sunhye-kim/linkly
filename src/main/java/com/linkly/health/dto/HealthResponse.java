package com.linkly.health.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "헬스 체크 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthResponse {

	@Schema(description = "서비스 상태", example = "UP")
	private String status;

	@Schema(description = "서비스 이름", example = "Linkly API Server")
	private String service;
}
