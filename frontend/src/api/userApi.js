import apiClient from './client';

export const userApi = {
  // 전체 유저 목록 조회
  getAllUsers: async () => {
    const response = await apiClient.get('/users');
    return response.data.data;
  },

  // 특정 유저 조회
  getUserById: async (id) => {
    const response = await apiClient.get(`/users/${id}`);
    return response.data.data;
  },

  // 이메일로 유저 검색
  getUserByEmail: async (email) => {
    const response = await apiClient.get('/users', {
      params: { email }
    });
    return response.data.data;
  },

  // 새 유저 생성
  createUser: async (userData) => {
    const response = await apiClient.post('/users', userData);
    return response.data.data;
  },

  // 유저 정보 수정
  updateUser: async (id, userData) => {
    const response = await apiClient.put(`/users/${id}`, userData);
    return response.data.data;
  },

  // 유저 삭제
  deleteUser: async (id) => {
    await apiClient.delete(`/users/${id}`);
  },
};