package com.linkly.auth;

import com.linkly.auth.dto.LoginRequest;
import com.linkly.auth.dto.LoginResponse;
import com.linkly.auth.dto.SignupRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "로그인 및 회원가입 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
	@PostMapping("/signup")
	public ResponseEntity<LoginResponse> signup(@Valid @RequestBody SignupRequest request) {
		LoginResponse response = authService.signup(request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다. (Soft Delete)")
	@DeleteMapping("/withdraw")
	public ResponseEntity<Void> withdraw() {
		authService.withdraw();
		return ResponseEntity.noContent().build();
	}
}
