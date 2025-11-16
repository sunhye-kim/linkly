package com.linkly.user;

import com.linkly.global.dto.ApiResponse;
import com.linkly.user.dto.CreateUserRequest;
import com.linkly.user.dto.UpdateUserRequest;
import com.linkly.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "회원 관리 API")
public class UserController {

	private final UserService userService;

	@PostMapping
	@Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다. 이메일은 중복될 수 없습니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원 가입 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복, 유효성 검증 실패 등)")})
	public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
		log.info("POST /users - 회원 가입 요청: email={}", request.getEmail());

		UserResponse response = userService.createUser(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@GetMapping("/{id}")
	@Operation(summary = "회원 조회", description = "회원 ID로 회원 정보를 조회합니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
	public ResponseEntity<ApiResponse<UserResponse>> getUserById(
			@Parameter(description = "회원 ID", example = "1") @PathVariable Long id) {
		log.info("GET /users/{} - 회원 조회", id);

		UserResponse response = userService.getUserById(id);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping
	@Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회하거나, 이메일로 특정 회원을 검색합니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이메일로 조회 시 회원을 찾을 수 없음")})
	public ResponseEntity<ApiResponse<?>> getUsers(
			@Parameter(description = "검색할 이메일 (선택)", example = "user@example.com") @RequestParam(required = false) String email) {
		if (email != null && !email.isBlank()) {
			log.info("GET /users?email={} - 이메일로 회원 조회", email);
			UserResponse response = userService.getUserByEmail(email);
			return ResponseEntity.ok(ApiResponse.success(response));
		} else {
			log.info("GET /users - 전체 회원 조회");
			List<UserResponse> responses = userService.getAllUsers();
			return ResponseEntity.ok(ApiResponse.success(responses));
		}
	}

	@PutMapping("/{id}")
	@Operation(summary = "회원 정보 수정", description = "회원의 비밀번호 또는 이름을 수정합니다. 수정하지 않을 필드는 null로 전달하면 됩니다.")
	@ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검증 실패")})
	public ResponseEntity<ApiResponse<UserResponse>> updateUser(
			@Parameter(description = "회원 ID", example = "1") @PathVariable Long id,
			@Valid @RequestBody UpdateUserRequest request) {
		log.info("PUT /users/{} - 회원 정보 수정", id);

		UserResponse response = userService.updateUser(id, request);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "회원 탈퇴", description = "회원을 삭제합니다. (Soft Delete - 실제로는 삭제되지 않고 삭제 시간만 기록됩니다)")
	@ApiResponses({
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content(schema = @Schema(hidden = true))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")})
	public ResponseEntity<Void> deleteUser(@Parameter(description = "회원 ID", example = "1") @PathVariable Long id) {
		log.info("DELETE /users/{} - 회원 삭제", id);

		userService.deleteUser(id);

		return ResponseEntity.noContent().build();
	}
}
