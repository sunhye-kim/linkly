package com.linkly.user.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkly.domain.AppUser;
import com.linkly.global.security.WithMockCustomUser;
import com.linkly.user.AppUserRepository;
import com.linkly.user.dto.UpdateUserRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 테스트 (Integration Test) Django의 E2E 테스트와 유사 @SpringBootTest: 전체 애플리케이션 컨텍스트
 * 로드 - 실제 DB 사용 (H2 인메모리) - 모든 계층(Controller → Service → Repository → DB)이 함께
 * 동작 - 실제 환경과 가장 유사한 테스트 @Transactional: 각 테스트마다 자동 롤백
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("User 통합 테스트")
class UserIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AppUserRepository userRepository;

	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}

	@Test
	@WithMockCustomUser(userId = 1L, role = "ADMIN")
	@DisplayName("회원 조회 → 수정 → 삭제 전체 플로우")
	@Transactional
	void userFullLifecycle() throws Exception {
		// 1. 테스트 데이터 생성
		AppUser user = AppUser.builder().email("integration@example.com").password("password123").name("통합테스트 사용자")
				.build();
		AppUser savedUser = userRepository.save(user);
		Long userId = savedUser.getId();

		// 2. 회원 조회
		mockMvc.perform(get("/users/{id}", userId)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.id").value(userId))
				.andExpect(jsonPath("$.data.name").value("통합테스트 사용자"));

		// 3. 회원 정보 수정
		UpdateUserRequest updateRequest = UpdateUserRequest.builder().name("수정된 사용자").build();

		mockMvc.perform(put("/users/{id}", userId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.name").value("수정된 사용자"));

		// 4. 수정 확인
		mockMvc.perform(get("/users/{id}", userId)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("수정된 사용자"));

		// 5. 회원 삭제 (Soft Delete)
		mockMvc.perform(delete("/users/{id}", userId)).andDo(print()).andExpect(status().isNoContent());

		// 6. 삭제 확인 (조회 시 404)
		mockMvc.perform(get("/users/{id}", userId)).andDo(print()).andExpect(status().isNotFound());

		// 7. DB에서 Soft Delete 확인
		AppUser deletedUser = userRepository.findById(userId).orElseThrow();
		assertThat(deletedUser.getDeletedAt()).isNotNull();
		assertThat(deletedUser.isDeleted()).isTrue();
	}

	@Test
	@WithMockCustomUser(userId = 1L)
	@DisplayName("전체 회원 조회 - 삭제된 회원 제외")
	@Transactional
	void getAllUsers_ExcludeDeletedUsers() throws Exception {
		// given - 3명의 회원 생성 (1명은 삭제)
		AppUser user1 = AppUser.builder().email("user1@example.com").password("password123").name("사용자1").build();

		AppUser user2 = AppUser.builder().email("user2@example.com").password("password123").name("사용자2").build();

		AppUser user3 = AppUser.builder().email("user3@example.com").password("password123").name("사용자3").build();
		user3.softDelete(); // 삭제 처리

		userRepository.save(user1);
		userRepository.save(user2);
		userRepository.save(user3);

		// when & then - 전체 조회 시 삭제되지 않은 2명만 조회
		mockMvc.perform(get("/users")).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].email").exists()).andExpect(jsonPath("$.data[1].email").exists());
	}

	@Test
	@WithMockCustomUser(userId = 1L)
	@DisplayName("이메일로 회원 검색")
	@Transactional
	void searchUserByEmail() throws Exception {
		// given
		AppUser user = AppUser.builder().email("search@example.com").password("password123").name("검색 테스트").build();
		userRepository.save(user);

		// when & then
		mockMvc.perform(get("/users").param("email", "search@example.com")).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.email").value("search@example.com"))
				.andExpect(jsonPath("$.data.name").value("검색 테스트"));
	}

	@Test
	@WithMockCustomUser(userId = 1L)
	@DisplayName("존재하지 않는 이메일 검색 시 404")
	@Transactional
	void searchNonExistentEmail() throws Exception {
		// when & then
		mockMvc.perform(get("/users").param("email", "notfound@example.com")).andDo(print())
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false));
	}
}
