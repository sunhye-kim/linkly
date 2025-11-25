package com.linkly.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkly.auth.dto.LoginRequest;
import com.linkly.auth.dto.SignupRequest;
import com.linkly.domain.AppUser;
import com.linkly.domain.enums.UserRole;
import com.linkly.global.config.JwtTokenProvider;
import com.linkly.global.security.WithMockCustomUser;
import com.linkly.user.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Auth 통합 테스트")
class AuthIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AppUserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}

	@Test
	@DisplayName("회원가입 성공")
	@Transactional
	void signup_Success() throws Exception {
		// given
		SignupRequest request = new SignupRequest("test@example.com", "password123", "테스트 사용자");

		// when & then
		mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists()).andExpect(jsonPath("$.userId").exists())
				.andExpect(jsonPath("$.email").value("test@example.com"))
				.andExpect(jsonPath("$.name").value("테스트 사용자"));

		// DB 확인
		AppUser savedUser = userRepository.findByEmailAndDeletedAtIsNull("test@example.com").orElseThrow();
		assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
		assertThat(savedUser.getName()).isEqualTo("테스트 사용자");
		assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
	}

	@Test
	@DisplayName("회원가입 실패 - 이메일 중복")
	@Transactional
	void signup_DuplicateEmail() throws Exception {
		// given
		AppUser existingUser = AppUser.builder().email("duplicate@example.com")
				.password(passwordEncoder.encode("password123")).name("기존 사용자").role(UserRole.USER).build();
		userRepository.save(existingUser);

		SignupRequest request = new SignupRequest("duplicate@example.com", "password123", "새 사용자");

		// when & then
		mockMvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andDo(print())
				.andExpect(status().isInternalServerError()); // IllegalArgumentException -> 500
	}

	@Test
	@DisplayName("로그인 성공")
	@Transactional
	void login_Success() throws Exception {
		// given
		AppUser user = AppUser.builder().email("test@example.com").password(passwordEncoder.encode("password123"))
				.name("테스트 사용자").role(UserRole.USER).build();
		userRepository.save(user);

		LoginRequest request = new LoginRequest("test@example.com", "password123");

		// when & then
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists()).andExpect(jsonPath("$.email").value("test@example.com"))
				.andExpect(jsonPath("$.name").value("테스트 사용자"));
	}

	@Test
	@DisplayName("로그인 실패 - 잘못된 비밀번호")
	@Transactional
	void login_WrongPassword() throws Exception {
		// given
		AppUser user = AppUser.builder().email("test@example.com").password(passwordEncoder.encode("password123"))
				.name("테스트 사용자").role(UserRole.USER).build();
		userRepository.save(user);

		LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

		// when & then
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
	}

	@Test
	@DisplayName("로그인 실패 - 존재하지 않는 이메일")
	@Transactional
	void login_EmailNotFound() throws Exception {
		// given
		LoginRequest request = new LoginRequest("notfound@example.com", "password123");

		// when & then
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
	}

	@Test
	@DisplayName("회원 탈퇴 성공")
	void withdraw_Success() throws Exception {
		// given - 테스트 사용자 생성
		AppUser user = AppUser.builder().email("test@example.com").password(passwordEncoder.encode("password123"))
				.name("테스트 사용자").role(UserRole.USER).build();
		AppUser savedUser = userRepository.save(user);

		// JWT 토큰 생성
		String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail());

		// when & then - JWT 토큰으로 인증
		mockMvc.perform(delete("/auth/withdraw").header("Authorization", "Bearer " + token)).andDo(print())
				.andExpect(status().isNoContent());

		// DB 확인 - Soft Delete 되었는지 확인
		AppUser deletedUser = userRepository.findById(savedUser.getId()).orElseThrow();
		assertThat(deletedUser.getDeletedAt()).isNotNull();
		assertThat(deletedUser.isDeleted()).isTrue();
	}

	@Test
	@DisplayName("회원 탈퇴 실패 - 인증 없음")
	@Transactional
	void withdraw_Unauthorized() throws Exception {
		// when & then
		mockMvc.perform(delete("/auth/withdraw")).andDo(print()).andExpect(status().isForbidden()); // Spring Security는
																									// 인증 없으면 403
	}

	@Test
	@WithMockCustomUser(userId = 999L)
	@DisplayName("회원 탈퇴 실패 - 존재하지 않는 사용자")
	@Transactional
	void withdraw_UserNotFound() throws Exception {
		// when & then (사용자가 존재하지 않음)
		mockMvc.perform(delete("/auth/withdraw")).andDo(print()).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	@DisplayName("회원가입 → 로그인 → 탈퇴 전체 플로우")
	@Transactional
	void fullAuthFlow() throws Exception {
		// 1. 회원가입
		SignupRequest signupRequest = new SignupRequest("flow@example.com", "password123", "플로우 테스트");

		String signupResponse = mockMvc
				.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(signupRequest)))
				.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists()).andReturn()
				.getResponse().getContentAsString();

		String token = objectMapper.readTree(signupResponse).get("accessToken").asText();
		Long userId = objectMapper.readTree(signupResponse).get("userId").asLong();

		// 2. 로그인
		LoginRequest loginRequest = new LoginRequest("flow@example.com", "password123");

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").exists());

		// 3. 회원 탈퇴 (JWT 토큰 사용)
		mockMvc.perform(delete("/auth/withdraw").header("Authorization", "Bearer " + token)).andDo(print())
				.andExpect(status().isNoContent());

		// 4. DB 확인
		AppUser deletedUser = userRepository.findById(userId).orElseThrow();
		assertThat(deletedUser.getDeletedAt()).isNotNull();
		assertThat(deletedUser.isDeleted()).isTrue();

		// 5. 탈퇴 후 로그인 시도 (실패해야 함)
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andDo(print())
				.andExpect(status().isUnauthorized());
	}
}
