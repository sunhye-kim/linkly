import apiClient from './client';

// 회원가입
export const signup = async (email, password, name) => {
  const response = await apiClient.post('/auth/signup', {
    email,
    password,
    name,
  });
  return response.data;
};

// 로그인
export const login = async (email, password) => {
  const response = await apiClient.post('/auth/login', {
    email,
    password,
  });
  return response.data;
};