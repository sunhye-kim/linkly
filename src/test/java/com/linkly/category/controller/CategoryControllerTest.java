package com.linkly.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkly.category.CategoryController;
import com.linkly.category.CategoryService;
import com.linkly.category.dto.CategoryResponse;
import com.linkly.category.dto.CreateCategoryRequest;
import com.linkly.category.dto.UpdateCategoryRequest;
import com.linkly.global.exception.InvalidRequestException;
import com.linkly.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController 테스트")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("POST /api/categories - 카테고리 생성 성공")
    void createCategory_Success() throws Exception {
        // given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .userId(1L)
                .name("개발")
                .description("개발 관련 북마크")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(1L)
                .userId(1L)
                .name("개발")
                .description("개발 관련 북마크")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(categoryService.createCategory(eq(1L), any(CreateCategoryRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("개발"))
                .andExpect(jsonPath("$.data.userId").value(1L));

        then(categoryService).should(times(1)).createCategory(eq(1L), any(CreateCategoryRequest.class));
    }

    @Test
    @DisplayName("POST /api/categories - 유효성 검증 실패 (userId 없음)")
    void createCategory_MissingUserId() throws Exception {
        // given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("개발")
                .build();

        // when & then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/categories - 이름 중복")
    void createCategory_DuplicateName() throws Exception {
        // given
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .userId(1L)
                .name("개발")
                .build();

        given(categoryService.createCategory(eq(1L), any(CreateCategoryRequest.class)))
                .willThrow(new InvalidRequestException("이미 사용 중인 카테고리 이름입니다", "name=개발"));

        // when & then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("이미 사용 중인 카테고리 이름입니다"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - 카테고리 조회 성공")
    void getCategoryById_Success() throws Exception {
        // given
        Long categoryId = 1L;
        CategoryResponse response = CategoryResponse.builder()
                .id(categoryId)
                .userId(1L)
                .name("개발")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(categoryService.getCategoryById(categoryId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(categoryId))
                .andExpect(jsonPath("$.data.name").value("개발"));

        then(categoryService).should(times(1)).getCategoryById(categoryId);
    }

    @Test
    @DisplayName("GET /api/categories/{id} - 카테고리 없음 (404)")
    void getCategoryById_NotFound() throws Exception {
        // given
        Long categoryId = 999L;
        given(categoryService.getCategoryById(categoryId))
                .willThrow(new ResourceNotFoundException("Category", categoryId));

        // when & then
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/categories?userId=1 - 사용자의 카테고리 목록 조회")
    void getCategoriesByUserId() throws Exception {
        // given
        Long userId = 1L;
        CategoryResponse category1 = CategoryResponse.builder()
                .id(1L)
                .userId(userId)
                .name("개발")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CategoryResponse category2 = CategoryResponse.builder()
                .id(2L)
                .userId(userId)
                .name("디자인")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<CategoryResponse> responses = Arrays.asList(category1, category2);
        given(categoryService.getCategoriesByUserId(userId)).willReturn(responses);

        // when & then
        mockMvc.perform(get("/api/categories")
                        .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name").value("개발"))
                .andExpect(jsonPath("$.data[1].name").value("디자인"));

        then(categoryService).should(times(1)).getCategoriesByUserId(userId);
    }

    @Test
    @DisplayName("PUT /api/categories/{id}?userId=1 - 카테고리 수정 성공")
    void updateCategory_Success() throws Exception {
        // given
        Long categoryId = 1L;
        Long userId = 1L;
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("프로그래밍")
                .description("프로그래밍 관련")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .id(categoryId)
                .userId(userId)
                .name("프로그래밍")
                .description("프로그래밍 관련")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(categoryService.updateCategory(eq(categoryId), eq(userId), any(UpdateCategoryRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("프로그래밍"));

        then(categoryService).should(times(1)).updateCategory(eq(categoryId), eq(userId), any(UpdateCategoryRequest.class));
    }

    @Test
    @DisplayName("PUT /api/categories/{id}?userId=2 - 권한 없음 (400)")
    void updateCategory_Unauthorized() throws Exception {
        // given
        Long categoryId = 1L;
        Long userId = 2L;
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("프로그래밍")
                .build();

        given(categoryService.updateCategory(eq(categoryId), eq(userId), any(UpdateCategoryRequest.class)))
                .willThrow(new InvalidRequestException("해당 카테고리를 수정할 권한이 없습니다", "categoryId=" + categoryId));

        // when & then
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("해당 카테고리를 수정할 권한이 없습니다"));
    }

    @Test
    @DisplayName("DELETE /api/categories/{id}?userId=1 - 카테고리 삭제 성공")
    void deleteCategory_Success() throws Exception {
        // given
        Long categoryId = 1L;
        Long userId = 1L;

        // when & then
        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(categoryService).should(times(1)).deleteCategory(categoryId, userId);
    }

    @Test
    @DisplayName("DELETE /api/categories/{id}?userId=2 - 권한 없음 (400)")
    void deleteCategory_Unauthorized() throws Exception {
        // given
        Long categoryId = 1L;
        Long userId = 2L;

        // void 메서드는 doThrow 사용
        org.mockito.Mockito.doThrow(new InvalidRequestException("해당 카테고리를 삭제할 권한이 없습니다", "categoryId=" + categoryId))
                .when(categoryService).deleteCategory(categoryId, userId);

        // when & then
        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}