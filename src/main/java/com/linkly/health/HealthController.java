package com.linkly.health;

import com.linkly.global.dto.ApiResponse;
import com.linkly.health.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
@RestController
public class HealthController {

	@Operation(summary = "헬스 체크", description = "서버의 현재 상태를 확인합니다.")
	@GetMapping("/health")
	public ApiResponse<HealthResponse> health() {
		HealthResponse healthData = HealthResponse.builder().status("UP").service("Linkly API Server").build();

		return ApiResponse.success(healthData);
	}
}
