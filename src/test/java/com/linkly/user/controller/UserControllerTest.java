package com.linkly.user.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.user.UserController;
import com.linkly.user.UserService;
import com.linkly.user.dto.UpdateUserRequest;
import com.linkly.user.dto.UserResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller 계층 테스트 Django의 View 테스트와 유사 @WebMvcTest: Spring MVC 계층만 로드 (빠른
 * 테스트) - Controller, ControllerAdvice, Filter 등만 로드 - Service는 Mock으로 대체 -
 * MockMvc를 사용한 HTTP 요청/응답 테스트
 */
@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private com.linkly.global.config.JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private com.linkly.auth.CustomUserDetailsService customUserDetailsService;

	@Test
	@DisplayName("GET /users/{id} - 회원 조회 성공")
	void getUserById_Success() throws Exception {
		// given
		Long userId = 1L;
		UserResponse response = createUserResponse(userId, "test@example.com", "테스트 사용자");

		given(userService.getUserById(userId)).willReturn(response);

		// when & then
		mockMvc.perform(get("/users/{id}", userId)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.id").value(userId))
				.andExpect(jsonPath("$.data.email").value("test@example.com"));

		then(userService).should(times(1)).getUserById(userId);
	}

	@Test
	@DisplayName("GET /users/{id} - 회원 없음 (404)")
	void getUserById_NotFound() throws Exception {
		// given
		Long userId = 999L;
		given(userService.getUserById(userId)).willThrow(new ResourceNotFoundException("User", userId));

		// when & then
		mockMvc.perform(get("/users/{id}", userId)).andDo(print()).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	@DisplayName("GET /users - 전체 회원 조회")
	void getAllUsers() throws Exception {
		// given
		UserResponse user1 = createUserResponse(1L, "user1@example.com", "사용자1");
		UserResponse user2 = createUserResponse(2L, "user2@example.com", "사용자2");

		List<UserResponse> responses = Arrays.asList(user1, user2);
		given(userService.getAllUsers()).willReturn(responses);

		// when & then
		mockMvc.perform(get("/users")).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].email").value("user1@example.com"))
				.andExpect(jsonPath("$.data[1].email").value("user2@example.com"));

		then(userService).should(times(1)).getAllUsers();
	}

	@Test
	@DisplayName("GET /users?email=xxx - 이메일로 회원 검색")
	void getUserByEmail() throws Exception {
		// given
		String email = "test@example.com";
		UserResponse response = createUserResponse(1L, email, "테스트 사용자");

		given(userService.getUserByEmail(email)).willReturn(response);

		// when & then
		mockMvc.perform(get("/users").param("email", email)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.email").value(email));

		then(userService).should(times(1)).getUserByEmail(email);
	}

	@Test
	@DisplayName("PUT /users/{id} - 회원 정보 수정 성공")
	void updateUser_Success() throws Exception {
		// given
		Long userId = 1L;
		UpdateUserRequest request = UpdateUserRequest.builder().name("수정된 이름").build();

		UserResponse response = createUserResponse(userId, "test@example.com", "수정된 이름");

		given(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(put("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.name").value("수정된 이름"));

		then(userService).should(times(1)).updateUser(eq(userId), any(UpdateUserRequest.class));
	}

	@Test
	@DisplayName("DELETE /users/{id} - 회원 삭제 성공")
	void deleteUser_Success() throws Exception {
		// given
		Long userId = 1L;

		// when & then
		mockMvc.perform(delete("/users/{id}", userId)).andDo(print()).andExpect(status().isNoContent());

		then(userService).should(times(1)).deleteUser(userId);
	}

	@Test
	@DisplayName("DELETE /users/{id} - 회원 없음 (404)")
	void deleteUser_NotFound() throws Exception {
		// given
		Long userId = 999L;
		given(userService.getUserById(userId)).willThrow(new ResourceNotFoundException("User", userId));

		// when & then
		mockMvc.perform(get("/users/{id}", userId)).andDo(print()).andExpect(status().isNotFound());
	}
	private UserResponse createUserResponse(Long id, String email, String name) {
		LocalDateTime now = LocalDateTime.now();
		return new UserResponse(id, email, name, null, now, now);
	}
}
