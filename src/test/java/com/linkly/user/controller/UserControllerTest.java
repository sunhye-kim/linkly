package com.linkly.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import com.linkly.user.UserController;
import com.linkly.user.UserService;
import com.linkly.user.dto.CreateUserRequest;
import com.linkly.user.dto.UpdateUserRequest;
import com.linkly.user.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller 계층 테스트
 * Django의 View 테스트와 유사
 *
 * @WebMvcTest: Spring MVC 계층만 로드 (빠른 테스트)
 * - Controller, ControllerAdvice, Filter 등만 로드
 * - Service는 Mock으로 대체
 * - MockMvc를 사용한 HTTP 요청/응답 테스트
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController 테스트")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/users - 회원 가입 성공")
    void createUser_Success() throws Exception {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트 사용자")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userService.createUser(any(CreateUserRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트 사용자"));

        then(userService).should(times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    @DisplayName("POST /api/users - 유효성 검증 실패 (이메일 형식 오류)")
    void createUser_InvalidEmail() throws Exception {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("invalid-email")  // 잘못된 이메일 형식
                .password("password123")
                .name("테스트 사용자")
                .build();

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/users - 이메일 중복")
    void createUser_DuplicateEmail() throws Exception {
        // given
        CreateUserRequest request = CreateUserRequest.builder()
                .email("duplicate@example.com")
                .password("password123")
                .name("테스트 사용자")
                .build();

        given(userService.createUser(any(CreateUserRequest.class)))
                .willThrow(new InvalidRequestException("이미 사용 중인 이메일입니다", "email=" + request.getEmail()));

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("이미 사용 중인 이메일입니다"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 회원 조회 성공")
    void getUserById_Success() throws Exception {
        // given
        Long userId = 1L;
        UserResponse response = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("테스트 사용자")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userService.getUserById(userId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        then(userService).should(times(1)).getUserById(userId);
    }

    @Test
    @DisplayName("GET /api/users/{id} - 회원 없음 (404)")
    void getUserById_NotFound() throws Exception {
        // given
        Long userId = 999L;
        given(userService.getUserById(userId))
                .willThrow(new ResourceNotFoundException("User", userId));

        // when & then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/users - 전체 회원 조회")
    void getAllUsers() throws Exception {
        // given
        UserResponse user1 = UserResponse.builder()
                .id(1L)
                .email("user1@example.com")
                .name("사용자1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserResponse user2 = UserResponse.builder()
                .id(2L)
                .email("user2@example.com")
                .name("사용자2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<UserResponse> responses = Arrays.asList(user1, user2);
        given(userService.getAllUsers()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$.data[1].email").value("user2@example.com"));

        then(userService).should(times(1)).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/users?email=xxx - 이메일로 회원 검색")
    void getUserByEmail() throws Exception {
        // given
        String email = "test@example.com";
        UserResponse response = UserResponse.builder()
                .id(1L)
                .email(email)
                .name("테스트 사용자")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userService.getUserByEmail(email)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users")
                        .param("email", email))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email));

        then(userService).should(times(1)).getUserByEmail(email);
    }

    @Test
    @DisplayName("PUT /api/users/{id} - 회원 정보 수정 성공")
    void updateUser_Success() throws Exception {
        // given
        Long userId = 1L;
        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("수정된 이름")
                .build();

        UserResponse response = UserResponse.builder()
                .id(userId)
                .email("test@example.com")
                .name("수정된 이름")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(userService.updateUser(eq(userId), any(UpdateUserRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("수정된 이름"));

        then(userService).should(times(1)).updateUser(eq(userId), any(UpdateUserRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - 회원 삭제 성공")
    void deleteUser_Success() throws Exception {
        // given
        Long userId = 1L;

        // when & then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(userService).should(times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - 회원 없음 (404)")
    void deleteUser_NotFound() throws Exception {
        // given
        Long userId = 999L;
        given(userService.getUserById(userId))
                .willThrow(new ResourceNotFoundException("User", userId));

        // when & then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}