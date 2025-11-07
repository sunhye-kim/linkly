package com.linkly.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private final boolean success;
	private final T data;
	private final ErrorInfo error;
	private final LocalDateTime timestamp;

	private ApiResponse(boolean success, T data, ErrorInfo error) {
		this.success = success;
		this.data = data;
		this.error = error;
		this.timestamp = LocalDateTime.now();
	}

	// 성공 응답
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	// 성공 응답 (데이터 없음)
	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(true, null, null);
	}

	// 실패 응답
	public static <T> ApiResponse<T> error(String message) {
		return new ApiResponse<>(false, null, new ErrorInfo(message, null));
	}

	// 실패 응답 (상세 정보 포함)
	public static <T> ApiResponse<T> error(String message, String details) {
		return new ApiResponse<>(false, null, new ErrorInfo(message, details));
	}

	@Getter
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ErrorInfo {
		private final String message;
		private final String details;

		public ErrorInfo(String message, String details) {
			this.message = message;
			this.details = details;
		}
	}
}
