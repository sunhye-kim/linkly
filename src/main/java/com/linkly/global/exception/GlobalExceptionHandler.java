package com.linkly.global.exception;

import com.linkly.global.dto.ApiResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/** 비즈니스 로직 예외 처리 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
		log.warn("Business exception occurred: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage(), e.getDetails()));
	}

	/** 리소스를 찾을 수 없는 경우 */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException e) {
		log.warn("Resource not found: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
	}

	/** 잘못된 요청 */
	@ExceptionHandler(InvalidRequestException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidRequestException(InvalidRequestException e) {
		log.warn("Invalid request: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage(), e.getDetails()));
	}

	/** 인증 실패 (로그인 실패) */
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
		log.warn("Authentication failed: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
	}

	/** 인증되지 않은 접근 (SecurityUtils에서 발생하는 IllegalStateException) */
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException e) {
		// SecurityUtils에서 발생하는 인증 관련 예외인 경우 403 Forbidden 반환
		if (e.getMessage() != null && e.getMessage().contains("인증되지 않은")) {
			log.warn("Unauthorized access: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
		}
		// 그 외의 경우는 500 Internal Server Error로 처리
		log.error("Illegal state exception occurred", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("서버 내부 오류가 발생했습니다.", e.getMessage()));
	}

	/** Validation 예외 처리 (@Valid 실패) */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
		String errorMessage = e.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
				.collect(Collectors.joining(", "));

		String details = e.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining("; "));

		log.warn("Validation failed: {}", errorMessage);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("입력값 검증에 실패했습니다.", details));
	}

	/** 타입 변환 실패 (PathVariable, RequestParam 등) */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
		String message = String.format("파라미터 '%s'의 값이 올바르지 않습니다.", e.getName());
		log.warn("Type mismatch: {}", e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
	}

	/** 예상하지 못한 모든 예외 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
		log.error("Unexpected exception occurred", e);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiResponse.error("서버 내부 오류가 발생했습니다.", e.getMessage()));
	}
}
