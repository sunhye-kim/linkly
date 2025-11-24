package com.linkly.category.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkly.category.CategoryRepository;
import com.linkly.category.dto.CreateCategoryRequest;
import com.linkly.category.dto.UpdateCategoryRequest;
import com.linkly.domain.AppUser;
import com.linkly.domain.Category;
import com.linkly.global.security.WithMockCustomUser;
import com.linkly.user.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Category 통합 테스트")
class CategoryIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private AppUserRepository userRepository;

	private AppUser testUser;

	@BeforeEach
	void setUp() {
		testUser = AppUser.builder().email("integration@example.com").password("password123").name("통합테스트 사용자").build();
		testUser = userRepository.save(testUser);

		// 저장된 testUser로 SecurityContext 설정
		org.springframework.security.authentication.UsernamePasswordAuthenticationToken authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
				testUser, null, java.util.Collections.emptyList());
		org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@AfterEach
	void tearDown() {
		categoryRepository.deleteAll();
		userRepository.deleteAll();
		org.springframework.security.core.context.SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("카테고리 생성 → 조회 → 수정 → 삭제 전체 플로우")
	@Transactional
	void categoryFullLifecycle() throws Exception {
		// 1. 카테고리 생성
		CreateCategoryRequest createRequest = CreateCategoryRequest.builder().name("개발").description("개발 관련 북마크")
				.build();

		String createResponse = mockMvc
				.perform(post("/categories").contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createRequest)))
				.andDo(print()).andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.name").value("개발")).andReturn().getResponse().getContentAsString();

		Long categoryId = objectMapper.readTree(createResponse).get("data").get("id").asLong();

		// 2. 카테고리 조회
		mockMvc.perform(get("/categories/{id}", categoryId)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.id").value(categoryId))
				.andExpect(jsonPath("$.data.name").value("개발"));

		// 3. 카테고리 수정
		UpdateCategoryRequest updateRequest = UpdateCategoryRequest.builder().name("프로그래밍").description("프로그래밍 관련 북마크")
				.build();

		mockMvc.perform(put("/categories/{id}", categoryId).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.name").value("프로그래밍"));

		// 4. 수정 확인
		mockMvc.perform(get("/categories/{id}", categoryId)).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("프로그래밍"));

		// 5. 카테고리 삭제
		mockMvc.perform(delete("/categories/{id}", categoryId)).andDo(print()).andExpect(status().isNoContent());

		// 6. 삭제 확인 (조회 시 404)
		mockMvc.perform(get("/categories/{id}", categoryId)).andDo(print()).andExpect(status().isNotFound());

		// 7. DB에서 Soft Delete 확인
		Category deletedCategory = categoryRepository.findById(categoryId).orElseThrow();
		assertThat(deletedCategory.getDeletedAt()).isNotNull();
		assertThat(deletedCategory.isDeleted()).isTrue();
	}

	@Test
	@DisplayName("같은 사용자는 중복된 이름의 카테고리를 만들 수 없음")
	@Transactional
	void duplicateCategoryNameForSameUser() throws Exception {
		// given - 첫 번째 카테고리 생성
		CreateCategoryRequest request1 = CreateCategoryRequest.builder().name("개발").build();

		mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request1))).andExpect(status().isCreated());

		// when & then - 같은 이름으로 두 번째 카테고리 생성 시도
		CreateCategoryRequest request2 = CreateCategoryRequest.builder().name("개발").build();

		mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request2))).andDo(print()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.message").value("이미 사용 중인 카테고리 이름입니다"));
	}

	@Test
	@DisplayName("다른 사용자는 같은 이름의 카테고리를 만들 수 있음")
	@Transactional
	void differentUsersSameCategoryName() throws Exception {
		// given - 두 번째 사용자 생성
		final AppUser anotherUser = userRepository
				.save(AppUser.builder().email("another@example.com").password("password123").name("다른 사용자").build());

		// when - 두 사용자가 같은 이름의 카테고리 생성
		CreateCategoryRequest request1 = CreateCategoryRequest.builder().name("개발").build();
		CreateCategoryRequest request2 = CreateCategoryRequest.builder().name("개발").build();

		// then - 둘 다 성공
		mockMvc.perform(post("/categories")
				.with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
						.authentication(
								new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
										testUser, null, java.util.Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request1)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/categories")
				.with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
						.authentication(
								new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
										anotherUser, null, java.util.Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request2)))
				.andExpect(status().isCreated());
	}

	@Test
	@DisplayName("사용자의 카테고리 목록 조회 - 삭제된 카테고리 제외")
	@Transactional
	void getCategoriesByUserId_ExcludeDeletedCategories() throws Exception {
		// given - 3개의 카테고리 생성 (1개는 삭제)
		Category category1 = Category.builder().appUser(testUser).name("개발").build();

		Category category2 = Category.builder().appUser(testUser).name("디자인").build();

		Category category3 = Category.builder().appUser(testUser).name("삭제됨").build();
		category3.softDelete();

		categoryRepository.save(category1);
		categoryRepository.save(category2);
		categoryRepository.save(category3);

		// when & then - 조회 시 삭제되지 않은 2개만 조회
		mockMvc.perform(get("/categories")).andDo(print()).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data", hasSize(2)))
				.andExpect(jsonPath("$.data[0].name").exists()).andExpect(jsonPath("$.data[1].name").exists());
	}

	@Test
	@DisplayName("권한 없이 카테고리 수정 시도")
	@Transactional
	void updateCategory_Unauthorized() throws Exception {
		// given - 다른 사용자의 카테고리
		AppUser owner = AppUser.builder().email("owner@example.com").password("password123").name("소유자").build();
		owner = userRepository.save(owner);

		Category category = Category.builder().appUser(owner).name("개발").build();
		category = categoryRepository.save(category);

		UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("수정 시도").build();

		// when & then - 다른 사용자(testUser)가 수정 시도하면 실패
		mockMvc.perform(put("/categories/{id}", category.getId()).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.message").value("해당 카테고리를 수정할 권한이 없습니다"));
	}

	@Test
	@WithMockCustomUser(userId = 1L, email = "integration@example.com")
	@DisplayName("권한 없이 카테고리 삭제 시도")
	@Transactional
	void deleteCategory_Unauthorized() throws Exception {
		// given - 다른 사용자의 카테고리
		AppUser owner = AppUser.builder().email("owner2@example.com").password("password123").name("소유자2").build();
		owner = userRepository.save(owner);

		Category category = Category.builder().appUser(owner).name("개발").build();
		category = categoryRepository.save(category);

		// when & then - 다른 사용자(testUser)가 삭제 시도하면 실패
		mockMvc.perform(delete("/categories/{id}", category.getId())).andDo(print()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.message").value("해당 카테고리를 삭제할 권한이 없습니다"));
	}

	@Test
	@WithMockCustomUser(userId = 1L, email = "integration@example.com")
	@DisplayName("유효성 검증 - 이름 길이 초과")
	@Transactional
	void validation_NameTooLong() throws Exception {
		// given - 이름이 50자 초과
		String longName = "a".repeat(51);
		CreateCategoryRequest request = CreateCategoryRequest.builder().name(longName).build();

		// when & then
		mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}
}
