import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// 요청 인터셉터 - JWT 토큰 자동 추가
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // 에러 메시지 추출: error.response.data.error.message 또는 error.response.data.message
    const errorMessage =
      error.response?.data?.error?.message ||
      error.response?.data?.message ||
      error.message ||
      '알 수 없는 오류가 발생했습니다.';

    // error.message에 서버 에러 메시지 할당
    error.message = errorMessage;

    if (error.response) {
      console.error('API Error:', errorMessage);
    } else if (error.request) {
      console.error('Network Error:', errorMessage);
    } else {
      console.error('Error:', errorMessage);
    }
    return Promise.reject(error);
  }
);

export default apiClient;
