import apiClient from './client';

// 임시로 하드코딩된 userId (나중에 인증 기능 추가시 변경)
const TEMP_USER_ID = 1;

export const categoryApi = {
  // 사용자의 카테고리 목록 조회
  getCategoriesByUserId: async (userId = TEMP_USER_ID) => {
    const response = await apiClient.get('/categories', {
      params: { userId }
    });
    return response.data.data;
  },

  // 특정 카테고리 조회
  getCategoryById: async (categoryId) => {
    const response = await apiClient.get(`/categories/${categoryId}`);
    return response.data.data;
  },

  // 새 카테고리 생성
  createCategory: async (categoryData, userId = TEMP_USER_ID) => {
    const response = await apiClient.post('/categories', {
      ...categoryData,
      userId
    });
    return response.data.data;
  },

  // 카테고리 수정
  updateCategory: async (categoryId, categoryData, userId = TEMP_USER_ID) => {
    const response = await apiClient.put(`/categories/${categoryId}`, categoryData, {
      params: { userId }
    });
    return response.data.data;
  },

  // 카테고리 삭제
  deleteCategory: async (categoryId, userId = TEMP_USER_ID) => {
    await apiClient.delete(`/categories/${categoryId}`, {
      params: { userId }
    });
  },
};