import apiClient from './client';

export const linkHealthApi = {
  // 내 북마크 전체 최신 헬스 상태 목록
  getMyResults: async () => {
    const response = await apiClient.get('/link-health');
    return response.data.data;
  },

  // 특정 북마크 즉시 헬스체크
  checkNow: async (bookmarkId) => {
    const response = await apiClient.post(`/link-health/${bookmarkId}/check`);
    return response.data.data;
  },
};
