# Linkly Frontend

React + Vite 기반 북마크 관리 프론트엔드

## 기술 스택

- **React 18** - UI 라이브러리
- **Vite** - 빌드 도구 (빠른 개발 서버)
- **JavaScript/JSX** - 프로그래밍 언어

## 사전 요구사항

- Node.js 16 이상
- npm 또는 yarn

## 설치

```bash
# 의존성 설치
npm install
```

## 실행 방법

### 개발 모드

```bash
npm run dev
```

개발 서버가 시작됩니다: http://localhost:3000

**주의:** 백엔드 서버가 `http://localhost:8080`에서 실행 중이어야 합니다.

### 백엔드와 함께 실행

**터미널 1 - 백엔드:**
```bash
cd ..
./gradlew bootRun
```

**터미널 2 - 프론트엔드:**
```bash
npm run dev
```

## 빌드

### 프로덕션 빌드

```bash
npm run build
```

빌드 결과물이 `../src/main/resources/static/`에 생성됩니다.

### 빌드 결과 미리보기

```bash
npm run preview
```

## 프로젝트 구조

```
frontend/
├── src/
│   ├── App.jsx          # 메인 컴포넌트
│   ├── App.css          # 스타일
│   ├── main.jsx         # 진입점
│   └── assets/          # 이미지, 아이콘 등
├── public/              # 정적 파일
├── index.html           # HTML 템플릿
├── package.json         # 의존성 관리
├── vite.config.js       # Vite 설정
└── README.md
```

## 설정 파일

### vite.config.js

```javascript
export default defineConfig({
  plugins: [react()],

  // 빌드 결과물을 Spring Boot static 폴더로 출력
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },

  // 개발 서버 설정
  server: {
    port: 3000,
    // Spring Boot API로 프록시
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

## API 호출 예시

### Fetch API 사용

```javascript
// 북마크 목록 조회
fetch('/api/bookmarks/user/1')
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));

// 북마크 생성
fetch('/api/bookmarks', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    userId: 1,
    title: '새 북마크',
    url: 'https://example.com',
    description: '설명',
  }),
})
  .then(response => response.json())
  .then(data => console.log('Success:', data))
  .catch(error => console.error('Error:', error));
```

### Axios 사용 (권장)

```bash
# Axios 설치
npm install axios
```

```javascript
import axios from 'axios';

// API 베이스 설정
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// 북마크 목록 조회
const bookmarks = await api.get('/bookmarks/user/1');

// 북마크 생성
const newBookmark = await api.post('/bookmarks', {
  userId: 1,
  title: '새 북마크',
  url: 'https://example.com',
});
```

## 추천 라이브러리

### UI/스타일링
```bash
# Tailwind CSS
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p

# Material-UI
npm install @mui/material @emotion/react @emotion/styled

# Ant Design
npm install antd
```

### 라우팅
```bash
npm install react-router-dom
```

### 상태 관리
```bash
# Context API (내장)
# 또는 Zustand (간단)
npm install zustand

# 또는 Redux Toolkit
npm install @reduxjs/toolkit react-redux
```

### HTTP 클라이언트
```bash
npm install axios
```

### 폼 관리
```bash
npm install react-hook-form
```

## 개발 팁

### Hot Module Replacement (HMR)

Vite는 자동으로 HMR을 지원합니다. 코드를 수정하면 브라우저가 자동으로 새로고침됩니다.

### 환경 변수

`.env` 파일 생성:
```
VITE_API_URL=http://localhost:8080
```

사용:
```javascript
const apiUrl = import.meta.env.VITE_API_URL;
```

### 디버깅

React DevTools 브라우저 확장 프로그램 설치를 권장합니다.

## 빌드 & 배포

### 로컬 빌드 테스트

```bash
# 빌드
npm run build

# 미리보기
npm run preview
```

### Spring Boot와 통합 배포

```bash
# 1. 프론트엔드 빌드
npm run build

# 2. 백엔드 빌드 (루트 디렉토리에서)
cd ..
./gradlew build

# 3. JAR 실행
java -jar build/libs/Linkly-0.0.1-SNAPSHOT.jar
```

## 문제 해결

### 포트가 이미 사용 중인 경우

```bash
# vite.config.js에서 포트 변경
server: {
  port: 3001,  // 다른 포트로 변경
}
```

### API 요청이 실패하는 경우

1. 백엔드 서버가 실행 중인지 확인
2. `vite.config.js`의 프록시 설정 확인
3. 브라우저 콘솔에서 에러 메시지 확인

### npm 캐시 문제

```bash
# 캐시 정리
npm cache clean --force

# node_modules 재설치
rm -rf node_modules package-lock.json
npm install
```

## 추가 리소스

- [React 공식 문서](https://react.dev/)
- [Vite 공식 문서](https://vitejs.dev/)
- [Linkly API 문서](http://localhost:8080/swagger-ui.html)