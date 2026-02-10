import apiClient from './client';

// 임시로 하드코딩된 userId (나중에 인증 기능 추가시 변경)
const TEMP_USER_ID = 1;

export const bookmarkApi = {
  // 사용자의 북마크 목록 조회
  getBookmarksByUserId: async (userId = TEMP_USER_ID) => {
    const response = await apiClient.get('/bookmarks', {
      params: { userId }
    });
    return response.data.data;
  },

  // 특정 북마크 조회
  getBookmarkById: async (bookmarkId) => {
    const response = await apiClient.get(`/bookmarks/${bookmarkId}`);
    return response.data.data;
  },

  // 새 북마크 생성
  createBookmark: async (bookmarkData, userId = TEMP_USER_ID) => {
    const response = await apiClient.post('/bookmarks', {
      ...bookmarkData,
      userId
    });
    return response.data.data;
  },

  // 북마크 수정
  updateBookmark: async (bookmarkId, bookmarkData, userId = TEMP_USER_ID) => {
    const response = await apiClient.put(`/bookmarks/${bookmarkId}`, bookmarkData, {
      params: { userId }
    });
    return response.data.data;
  },

  // 북마크 삭제
  deleteBookmark: async (bookmarkId, userId = TEMP_USER_ID) => {
    await apiClient.delete(`/bookmarks/${bookmarkId}`, {
      params: { userId }
    });
  },

  // 키워드로 북마크 검색 (카테고리 필터와 AND 조합 가능)
  searchBookmarks: async (keyword, categoryId = null) => {
    const params = { keyword };
    if (categoryId !== null) params.categoryId = categoryId;
    const response = await apiClient.get('/bookmarks/search', { params });
    return response.data.data;
  },
};